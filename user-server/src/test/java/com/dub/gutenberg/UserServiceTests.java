package com.dub.gutenberg;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.annotation.PostConstruct;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.dub.gutenberg.domain.Address;
import com.dub.gutenberg.domain.MyUser;
import com.dub.gutenberg.domain.PaymentMethod;
import com.dub.gutenberg.services.UserService;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;


/**
 * Here I implement one test for each method of UserService interface
 * Note that the createUser conflict is tested in UserHandlerTest class, not here 
 * */
@SpringBootTest
public class UserServiceTests {

	@Autowired
	UserService userService;
	
	private Address newAddress = new Address();
	private PaymentMethod newPayment = new PaymentMethod();
	
	private Address delAddress = new Address();
	private PaymentMethod delPayment = new PaymentMethod();
	
				
	private Predicate<MyUser> testUserById = 
					user -> "Albert".equals(user.getUsername());
						
	private Predicate<MyUser> testUserByUsername = 
					user -> "2".equals(user.getId());
			
	private Predicate<MyUser> setPrimaryAddressPred = 
					user -> (user.getMainShippingAddress() == 1);				
				
	private Predicate<MyUser> setPrimaryPaymentPred = 
					user -> (user.getMainPayMeth() == 1);				
			
	private Predicate<MyUser> addAddressPred = 
					user -> (user.getAddresses().contains(newAddress));				
	
	private Predicate<MyUser> addPaymentPred = 
					user -> (user.getPaymentMethods().contains(newPayment));				
			
	private Predicate<MyUser> deleteAddressPred = 
					user -> (!user.getAddresses().contains(delAddress));				
					
	private Predicate<MyUser> deletePaymentPred = 
					user -> (!user.getPaymentMethods().contains(delPayment));				
							
	@PostConstruct
	private void init() {
		newAddress.setCity("London");
		newAddress.setCountry("United Kingdom");
		newAddress.setStreet("10 Downing Street");
		newAddress.setZip("SW1A 2AA");
		
		delAddress.setCity("Paris");
		delAddress.setCountry("France");
		delAddress.setStreet("31 rue du Louvre");
		delAddress.setZip("75001");
		
		newPayment.setCardNumber("1111222233336666");
		newPayment.setName("Mark Zuckerberg");
		
		delPayment.setCardNumber("8888777744441111");
	    delPayment.setName("Jean Castex");
	}
	
	/** 
	 * Here I use classical tests, not StepVerifier
	 * */
	@Test
	void testCreateUser() {
		List<Address> addresses = Arrays.asList(newAddress);
		List<PaymentMethod> payMeths = Arrays.asList(newPayment);
		MyUser user = new MyUser();
		user.setUsername("Boris");
		user.setHashedPassword("{bcrypt}$2a$10$Ip8KBSorI9R39m.KQBk3nu/WhjekgPSmfmpnmnf5yCL3aL9y.ITVW");
		user.setAddresses(addresses);
		user.setPaymentMethods(payMeths);
			
		// actual creation
		Mono<String> userId = this.userService.createUser(user);
		
		Mono<MyUser> check = userId.flatMap(transformById);
		MyUser checkUser = check.block();
		
		assertEquals("London", checkUser.getAddresses().get(0).getCity());
		assertEquals("Boris", checkUser.getUsername());
		
	}
	 
	@Test
	void testById() {
		String userId = "2";
		Mono<MyUser> user = this.userService.findById(userId);
		StepVerifier.create(user.log())
		.expectNextMatches(testUserById)
		.verifyComplete();
	}
	
	@Test
	void testByUsername() {
		String username = "Albert";
		Mono<MyUser> user = this.userService.findByUsername(username);
		StepVerifier.create(user.log())
		.expectNextMatches(testUserByUsername)
		.verifyComplete();	
	}
	
	@Test
	void testSetPrimaryAddress() {
		String userId = "4";
		int index = 1;
		Mono<MyUser> user = this.userService.setPrimaryAddress(userId, index);
		StepVerifier.create(user.log())
		.expectNextMatches(setPrimaryAddressPred)
		.verifyComplete();	
	}
	
	@Test
	void testSetPrimaryPayment() {
		String userId = "4";
		int index = 1;
		Mono<MyUser> user = this.userService.setPrimaryPayment(userId, index);
		StepVerifier.create(user.log())
		.expectNextMatches(setPrimaryPaymentPred)
		.verifyComplete();	
	}
	
	@Test
	void testAddAddress() {
		String userId = "4";
		// actual update
		Mono<MyUser> user = this.userService.addAddress(userId, newAddress);
		StepVerifier.create(user.log())
		.expectNextMatches(addAddressPred)
		.verifyComplete();	
	}
	
	@Test
	void testAddPayment() {
		String userId = "4";
		// actual update
		Mono<MyUser> user = this.userService.addPaymentMethod(userId, newPayment);
		StepVerifier.create(user.log())
		.expectNextMatches(addPaymentPred)
		.verifyComplete();	
	}
	
	@Test
	void testDeleteAddress() {
		String userId = "2";
		// actual update
		Mono<MyUser> user = this.userService.deleteAddress(userId, delAddress);
		StepVerifier.create(user.log())
		.expectNextMatches(deleteAddressPred)
		.verifyComplete();	
	}
	
	@Test
	void testDeletePayment() {
		String userId = "2";
		// actual update
		Mono<MyUser> user = this.userService.deletePaymentMethod(userId, delPayment);
		StepVerifier.create(user.log())
		.expectNextMatches(deletePaymentPred)
		.verifyComplete();	
	}
	
	
	// helper functions
	
	Function<String, Mono<MyUser>> transformById =
			userId -> {
			
				return this.userService.findById(userId);
	};
}
