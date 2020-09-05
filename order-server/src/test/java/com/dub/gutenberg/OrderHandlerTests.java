package com.dub.gutenberg;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.dub.gutenberg.domain.EditCart;
import com.dub.gutenberg.domain.Item;
import com.dub.gutenberg.domain.Order;
import com.dub.gutenberg.domain.OrderAndBook;
import com.dub.gutenberg.domain.OrderAndState;
import com.dub.gutenberg.domain.OrderState;

import com.dub.gutenberg.services.OrderService;
import com.dub.gutenberg.web.OrderHandler;
import com.dub.gutenberg.web.OrderRouter;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;


@SpringBootTest
public class OrderHandlerTests {

	@Autowired
	OrderRouter orderRouter;
	
	@Autowired
	OrderHandler orderHandler;
	
	Order newOrder = new Order();
	
	DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
	
	private Predicate<Order> createOrderPred = 
			checkOrder -> ("CART".equals(checkOrder.getState().toString()));

	
	private Predicate<Order> orderByIdPred = 
			order -> "7".equals(order.getUserId()) &&
					OrderState.SHIPPED.equals(order.getState());
		
	private Predicate<Order> editCartPred =
					checkOrder -> {
						System.err.println(checkOrder.getLineItems().size());
						return this.matches(checkOrder.getLineItems(), new Item("10",3));				
	};
	
	private Predicate<Order> getActiveOrderPred =
			checkOrder -> {
				//System.err.println(checkOrder.getLineItems().size());
				return OrderState.CART.equals(checkOrder.getState())
						&& "10".equals(checkOrder.getUserId());				
	};
	
	private Predicate<Order> setOrderStatePred =
			checkOrder -> {
				//System.err.println(checkOrder.getLineItems().size());
				return OrderState.PRE_SHIPPING.equals(checkOrder.getState())
						&& "3".equals(checkOrder.getUserId());				
	};
	
	private Predicate<Order> checkoutOrderPred =
			checkOrder -> {
				System.err.println(checkOrder.getState());
				return OrderState.PRE_AUTHORIZE.equals(checkOrder.getState())
						&& "9".equals(checkOrder.getUserId());				
	};
	
	private Predicate<Order> addBookToOrderPred =
			checkOrder -> {
				System.err.println(checkOrder.getState());
				return ("CART".equals(checkOrder.getState().toString()))
						&& "9".equals(checkOrder.getUserId())
						&& this.matches(checkOrder.getLineItems(), new Item("8",1));
		};
		
	
	@Test
	void orderByIdTest() {
		String orderId = "1";
		WebTestClient
		.bindToRouterFunction(orderRouter.route(orderHandler))
		.build()
		.method(HttpMethod.GET)
		.uri("/orderById/" + orderId)
		.exchange()
		.expectStatus().isOk()
		.returnResult(Order.class)
		.getResponseBody()// it is a Flux<Order>	
		.as(StepVerifier::create)
		.expectNextMatches(orderByIdPred)
		.expectComplete()
		.verify();	
	}
	
	@Test
	void createOrderTest() {
		Order newOrder = new Order();
		newOrder.setDate(LocalDateTime.now());
		newOrder.setState(OrderState.CART);
		newOrder.setUserId("6");
	
		WebTestClient
		.bindToRouterFunction(orderRouter.route(orderHandler))
		.build()
		.method(HttpMethod.POST)
		.uri("/createOrder")
		.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
		.body(Mono.just(newOrder), Order.class)
		.exchange()
		.expectStatus().isOk()
		.returnResult(Order.class)
		.getResponseBody()// it is a Flux<Order>	
		.as(StepVerifier::create)
		.expectNextMatches(createOrderPred)
		.expectComplete()
		.verify();	
	}
	
	
	@Test
	void editCartTest() {
		
		List<Item> items = new ArrayList<Item>();
		items.add(new Item("10", 3));
		EditCart editCart = new EditCart();
		editCart.setOrderId("22");
		editCart.setItems(items);
		
		WebTestClient
		.bindToRouterFunction(orderRouter.route(orderHandler))
		.build()
		.method(HttpMethod.POST)
		.uri("/editCart")
		.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
		.body(Mono.just(editCart), EditCart.class)
		.exchange()
		.expectStatus().isOk()
		.returnResult(Order.class)
		.getResponseBody()// it is a Flux<Order>	
		.as(StepVerifier::create)
		.expectNextMatches(editCartPred)
		.expectComplete()
		.verify();	
	}
	

	@Test
	void getActiveOrderTest() {
		String userId = "10";	
		
		WebTestClient
		.bindToRouterFunction(orderRouter.route(orderHandler))
		.build()
		.method(HttpMethod.POST)
		.uri("/getActiveOrder")
		.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
		.body(Mono.just(userId), String.class)
		.exchange()
		.expectStatus().isOk()
		.returnResult(Order.class)
		.getResponseBody()// it is a Flux<Order>	
		.as(StepVerifier::create)
		.expectNextMatches(getActiveOrderPred)
		.expectComplete()
		.verify();	
	}
	
	@Test
	void setOrderStateTest() {
		OrderAndState orderAndState = new OrderAndState();
		orderAndState.setOrderId("24");
		orderAndState.setState(OrderState.PRE_SHIPPING);
				
		WebTestClient
		.bindToRouterFunction(orderRouter.route(orderHandler))
		.build()
		.method(HttpMethod.POST)
		.uri("/setOrderState")
		.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
		.body(Mono.just(orderAndState), OrderAndState.class)
		.exchange()
		.expectStatus().isOk()
		.returnResult(Order.class)
		.getResponseBody()// it is a Flux<Order>	
		.as(StepVerifier::create)
		.expectNextMatches(setOrderStatePred)
		.expectComplete()
		.verify();	
	}

	
	@Test
	void checkoutOrderTest() {		
		String orderId = "26";
		
		WebTestClient
		.bindToRouterFunction(orderRouter.route(orderHandler))
		.build()
		.method(HttpMethod.POST)
		.uri("/checkoutOrder")
		.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
		.body(Mono.just(orderId), String.class)
		.exchange()
		.expectStatus().isOk()
		.returnResult(Order.class)
		.getResponseBody()// it is a Flux<Order>	
		.as(StepVerifier::create)
		.expectNextMatches(checkoutOrderPred)
		.expectComplete()
		.verify();	
	}
	
	
	@Test
	void addBookToOrderTest() {
			
		OrderAndBook orderAndBook = new OrderAndBook();
		orderAndBook.setOrderId("28");
		orderAndBook.setBookId("8");
			
		WebTestClient
		.bindToRouterFunction(orderRouter.route(orderHandler))
		.build()
		.method(HttpMethod.POST)
		.uri("/addBookToOrder")
		.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
		.body(Mono.just(orderAndBook), OrderAndBook.class)
		.exchange()
		.expectStatus().isOk()
		.returnResult(Order.class)
		.getResponseBody()// it is a Flux<Order>	
		.as(StepVerifier::create)
		.expectNextMatches(addBookToOrderPred)
		.expectComplete()
		.verify();	
	}
	
	
	
	private boolean matches(List<Item> list, Item check) {
		boolean match = false;
		for (Item item : list) {
			if (item.getBookId().equals(check.getBookId()) &&
					item.getQuantity() == check.getQuantity()) {
				match = true;
				break;
			}
		}
		return match;
	}
	
	
}
