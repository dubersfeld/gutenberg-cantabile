package com.dub.gutenberg;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.dub.gutenberg.domain.Address;
import com.dub.gutenberg.domain.AddressOperations;
import com.dub.gutenberg.domain.MyUser;
import com.dub.gutenberg.domain.PaymentMethod;
import com.dub.gutenberg.domain.PaymentOperations;
import com.dub.gutenberg.domain.Primary;
import com.dub.gutenberg.domain.ProfileOperations;
import com.dub.gutenberg.web.UserHandler;
import com.dub.gutenberg.web.UserRouter;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@SpringBootTest
public class UserHandlerTests {
	
	@Value("${baseUsersUrl}")
	private String baseUsersURL;
	

	@Autowired
	UserRouter userRouter;
	
	@Autowired
	UserHandler userHandler;
	
	private MyUser newUser = new MyUser();
	private MyUser conflictUser = new MyUser();
	
	private Address newAddress = new Address();
	private PaymentMethod newPayment = new PaymentMethod();
	
	private Address delAddress = new Address();
	private PaymentMethod delPayment = new PaymentMethod();
	

	@PostConstruct
	private void init() {
		newUser.setUsername("Nelson");
		newUser.setHashedPassword("{bcrypt}$2a$10$Ip8KBSorI9R39m.KQBk3nu/WhjekgPSmfmpnmnf5yCL3aL9y.ITVW");
		
		conflictUser.setUsername("Albert");
		conflictUser.setHashedPassword("{bcrypt}$2a$10$Ip8KBSorI9R39m.KQBk3nu/WhjekgPSmfmpnmnf5yCL3aL9y.ITVW");
	
		newAddress.setCity("London");
		newAddress.setCountry("United Kingdom");
		newAddress.setStreet("10 Downing Street");
		newAddress.setZip("SW1A 2AA");
		
		newPayment.setCardNumber("1111222255558888");
		newPayment.setName("Jeff Bezos");
		
		delAddress.setCity("Paris");
		delAddress.setCountry("FR");
		delAddress.setStreet("42 rue Amélie Nothomb");
		delAddress.setZip("75018");
		
		delPayment.setCardNumber("6789432167891234");
		delPayment.setName("Alice Carrol");
	}
	
	private Predicate<MyUser> setPrimaryAddressPred =
			user -> user.getMainShippingAddress() == 1;
			
	private Predicate<MyUser> setPrimaryPaymentPred =
			user -> user.getMainPayMeth() == 1;

	private Predicate<MyUser> addAddressPred = 				
			user -> matchAddress(user.getAddresses(), newAddress);
			
	private Predicate<MyUser> addPaymentPred = 				
			user -> matchPayment(user.getPaymentMethods(), newPayment);
			
	private Predicate<MyUser> deleteAddressPred = 				
			user -> !matchAddress(user.getAddresses(), delAddress);
			
	private Predicate<MyUser> deletePaymentPred = 			
			user -> !matchPayment(user.getPaymentMethods(), delPayment);
			
	private Predicate<MyUser> findByIdPred = 
			user -> ("Alice".equals(user.getUsername()));				
			
	private Predicate<MyUser> findByUsernamePred = 
			user -> ("4".equals(user.getId()));				
			
					
			
	@Test
	void createUserTest() {
		HttpHeaders headers = WebTestClient
		.bindToRouterFunction(userRouter.route(userHandler))
		.build()
		.method(HttpMethod.POST)
		.uri("/createUser")
		.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
		.body(Mono.just(newUser), MyUser.class)
		.exchange()
		.expectStatus().isCreated()
		.returnResult(String.class)
		.getResponseHeaders();// HttpHeaders
		
		
		
		String location = headers.get("location").get(0);
		String patternString = "^" + baseUsersURL + "/findById/[A-Za-z0-9_-]*$";  
	  
		
		
		Pattern pattern = Pattern.compile(patternString);
		Matcher matcher = pattern.matcher(location);
		assertEquals(true, matcher.matches());
	}	
	
	@Test
	void createUserConflictTest() {
		WebTestClient
		.bindToRouterFunction(userRouter.route(userHandler))
		.build()
		.method(HttpMethod.POST)
		.uri("/createUser")
		.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
		.body(Mono.just(conflictUser), MyUser.class)
		.exchange()
		.expectStatus().isEqualTo(HttpStatus.CONFLICT);
	}	
	
	@Test
	void setPrimaryAddressTest() {
		Primary primary = new Primary();
		primary.setUsername("Alice");
		primary.setIndex(1);
		
		WebTestClient
		.bindToRouterFunction(userRouter.route(userHandler))
		.build()
		.method(HttpMethod.POST)
		.uri("/primaryAddress")
		.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
		.body(Mono.just(primary), Primary.class)
		.exchange()
		.expectStatus().isOk()
		.returnResult(MyUser.class)
		.getResponseBody()// it is a Flux<MyUser>
		.as(StepVerifier::create)
		.expectNextMatches(setPrimaryAddressPred)
		.expectComplete()
		.verify();	
	}	
	
	@Test
	void setPrimaryPaymentTest() {
		Primary primary = new Primary();
		primary.setUsername("Alice");
		primary.setIndex(1);
		
		WebTestClient
		.bindToRouterFunction(userRouter.route(userHandler))
		.build()
		.method(HttpMethod.POST)
		.uri("/primaryPayment")
		.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
		.body(Mono.just(primary), Primary.class)
		.exchange()
		.expectStatus().isOk()
		.returnResult(MyUser.class)
		.getResponseBody()// it is a Flux<MyUser>
		.as(StepVerifier::create)
		.expectNextMatches(setPrimaryPaymentPred)
		.expectComplete()
		.verify();	
	}	
	
	@Test
	void addAddressTest() {
		AddressOperations addOp = new AddressOperations();
		addOp.setAddress(newAddress);
		addOp.setOp(ProfileOperations.ADD);
		addOp.setUserId("1");
		
		WebTestClient
		.bindToRouterFunction(userRouter.route(userHandler))
		.build()
		.method(HttpMethod.POST)
		.uri("/addAddress")
		.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
		.body(Mono.just(addOp), AddressOperations.class)
		.exchange()
		.expectStatus().isOk()
		.returnResult(MyUser.class)
		.getResponseBody()// it is a Flux<MyUser>
		.as(StepVerifier::create)
		.expectNextMatches(addAddressPred)
		.expectComplete()
		.verify();	
	}	
	
	@Test
	void addPaymentMethodTest() {
		PaymentOperations payOp = new PaymentOperations();
		
		payOp.setPaymentMethod(newPayment);
		payOp.setOp(ProfileOperations.ADD);
		payOp.setUserId("4");
		
		WebTestClient
		.bindToRouterFunction(userRouter.route(userHandler))
		.build()
		.method(HttpMethod.POST)
		.uri("/addPaymentMethod")
		.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
		.body(Mono.just(payOp), PaymentOperations.class)
		.exchange()
		.expectStatus().isOk()
		.returnResult(MyUser.class)
		.getResponseBody()// it is a Flux<MyUser>
		.as(StepVerifier::create)
		.expectNextMatches(addPaymentPred)
		.expectComplete()
		.verify();	
	}	
	
	@Test
	void deleteAddressTest() {
		AddressOperations addOp = new AddressOperations();
		
		addOp.setAddress(delAddress);
		addOp.setOp(ProfileOperations.DELETE);
		addOp.setUserId("4");
		
		WebTestClient
		.bindToRouterFunction(userRouter.route(userHandler))
		.build()
		.method(HttpMethod.POST)
		.uri("/deleteAddress")
		.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
		.body(Mono.just(addOp), AddressOperations.class)
		.exchange()
		.expectStatus().isOk()
		.returnResult(MyUser.class)
		.getResponseBody()// it is a Flux<MyUser>
		.as(StepVerifier::create)
		.expectNextMatches(deleteAddressPred)
		.expectComplete()
		.verify();	
	}	
	
	@Test
	void deletePaymentTest() {
		PaymentOperations payOp = new PaymentOperations();
		
		payOp.setPaymentMethod(delPayment);
		payOp.setOp(ProfileOperations.DELETE);
		payOp.setUserId("4");
		
		WebTestClient
		.bindToRouterFunction(userRouter.route(userHandler))
		.build()
		.method(HttpMethod.POST)
		.uri("/deletePaymentMethod")
		.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
		.body(Mono.just(payOp), PaymentOperations.class)
		.exchange()
		.expectStatus().isOk()
		.returnResult(MyUser.class)
		.getResponseBody()// it is a Flux<MyUser>
		.as(StepVerifier::create)
		.expectNextMatches(deletePaymentPred)
		.expectComplete()
		.verify();	
	}	
	
	@Test
	void findByIdTest() {
		String userId = "4";
		WebTestClient
		.bindToRouterFunction(userRouter.route(userHandler))
		.build()
		.method(HttpMethod.POST)
		.uri("/findById")
		.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
		.body(Mono.just(userId), String.class)
		.exchange()
		.expectStatus().isOk()
		.returnResult(MyUser.class)
		.getResponseBody()// it is a Flux<MyUser>
		.as(StepVerifier::create)
		.expectNextMatches(findByIdPred)
		.expectComplete()
		.verify();	
	}	
	
	@Test
	void findByUsernameTest() {
		String username = "Alice";
		WebTestClient
		.bindToRouterFunction(userRouter.route(userHandler))
		.build()
		.method(HttpMethod.GET)
		.uri("/userByName/" + username)
		.exchange()
		.expectStatus().isOk()
		.returnResult(MyUser.class)
		.getResponseBody()// it is a Flux<MyUser>
		.as(StepVerifier::create)
		.expectNextMatches(findByUsernamePred)
		.expectComplete()
		.verify();	
	}	
	

	private boolean matchAddress(List<Address> list, Address check) {
		boolean match = false;
		for (Address address : list) {
			if (address.equals(check)) {
				match = true;
				break;
			}
		}
		return match;
	}
	
	private boolean matchPayment(List<PaymentMethod> list, PaymentMethod check) {
		boolean match = false;
		for (PaymentMethod payMeth : list) {
			if (payMeth.equals(check)) {
				match = true;
				break;
			}
		}
		return match;
	}
}
