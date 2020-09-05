package com.dub.gutenberg;


import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.dub.gutenberg.domain.EditCart;
import com.dub.gutenberg.domain.Item;
import com.dub.gutenberg.domain.Order;
import com.dub.gutenberg.domain.OrderState;
import com.dub.gutenberg.services.OrderService;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;


@SpringBootTest
class OrderServiceTests {
		
	Order newOrder = new Order();
	
	DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
	
	@Autowired
	private OrderService orderService;
	
	@BeforeEach
	public void setUp() {
		
		newOrder.setDate(LocalDateTime.now());
		newOrder.setState(OrderState.CART);
		newOrder.setUserId("6");
	}
	
	
	private Predicate<Order> orderByIdPred = 
			order -> ("SHIPPED".equals(order.getState().toString()));

	private Predicate<Order> createOrderPred = 
			checkOrder -> ("CART".equals(checkOrder.getState().toString()));

	private Predicate<Order> activeOrderPred = 
			checkOrder -> ("CART".equals(checkOrder.getState().toString()));

	private Predicate<Order> addBookToOrderPred = 
					checkOrder -> { 
						System.err.println(checkOrder.getLineItems().size());
						System.err.println(checkOrder.getLineItems().get(1));//.size());
						
						return ("CART".equals(checkOrder.getState().toString()))
					&& "10".equals(checkOrder.getUserId())
					&& this.matches(checkOrder.getLineItems(), new Item("6",1));
	};
	
	private Predicate<Order> editCartPred =
			checkOrder -> {
				System.err.println(checkOrder.getLineItems().size());
				return this.matches(checkOrder.getLineItems(), new Item("8",2));		
	};
	
	private Predicate<Order> setOrderStatePred =
			checkOrder -> {
				return OrderState.PRE_SHIPPING.equals(checkOrder.getState());			
	};
	
	private Predicate<Order> checkoutOrderPred =
			checkOrder -> {
				return OrderState.PRE_AUTHORIZE.equals(checkOrder.getState());				
	};
	
	/*
	private Predicate<Order> checkoutOrderPred =
			checkOrder -> {
				return OrderState.PRE_AUTHORIZE.equals(checkOrder.getState());				
	};
	*/
					
												
	@Test
	void testById() {
		String orderId = "1";
		Mono<Order> user = this.orderService.getOrderById(orderId);
		StepVerifier.create(user.log())
		.expectNextMatches(orderByIdPred)
		.verifyComplete();
	}
	

	@Test
	void testCreateOrder() {
			
		// actual creation
		Mono<Order> checkOrder = this.orderService.saveOrder(newOrder, true);
		StepVerifier.create(checkOrder.log())
		.expectNextMatches(createOrderPred)
		.verifyComplete();
	}
	
	@Test
	void testActiveOrder() {
		String userId = "10";	
		
		Mono<Order> activeOrder;
		try {
			activeOrder = this.orderService.getActiveOrder(userId);
			StepVerifier.create(activeOrder.log())
			.expectNextMatches(activeOrderPred)
			.verifyComplete();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	@Test
	void testAddBookToOrder() {
		String orderId = "4";
		String bookId = "6";
		
		Mono<Order> activeOrder;
		
		try {
			activeOrder = this.orderService.addBookToOrder(orderId, bookId);
			StepVerifier.create(activeOrder.log())
			.expectNextMatches(addBookToOrderPred)
			.verifyComplete();	
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	@Test
	void testEditCart() {
		List<Item> items = new ArrayList<Item>();
		items.add(new Item("8", 2));
		EditCart editCart = new EditCart();
		editCart.setOrderId("22");
		editCart.setItems(items);
			
		Mono<Order> activeOrder;
		try {
			activeOrder = this.orderService.editCart(editCart);
			StepVerifier.create(activeOrder.log())
			.expectNextMatches(editCartPred)
			.verifyComplete();	
		} catch (IOException e) {
			e.printStackTrace();
		}
	
	}
	
	@Test
	void testSetOrderState() {
		String orderId = "23";
		OrderState state = OrderState.PRE_SHIPPING;
		
		Mono<Order> checkOrder;
		try {
			checkOrder = this.orderService.setOrderState(orderId, state);
			StepVerifier.create(checkOrder.log())
			.expectNextMatches(setOrderStatePred)
			.verifyComplete();	
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	@Test
	void testCheckoutOrder() {
		String orderId = "27";
		OrderState state = OrderState.PRE_AUTHORIZE;
		
		Mono<Order> checkOrder;
		try {
			checkOrder = this.orderService.setOrderState(orderId, state);
			StepVerifier.create(checkOrder.log())
			.expectNextMatches(checkoutOrderPred)
			.verifyComplete();	
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/*
	@Test
	void testSaveOrder() {
		List<Item> items = new ArrayList<>();
		items.add(new Item("6", 1));
		Order order = new Order();
		order.setState(OrderState.PRE_SHIPPING);
		order.setLineItems(items);
		order.setUserId("6");
		
		Mono<Order> checkOrder;
		
		checkOrder = this.orderService.saveOrder(order, false);
		
		StepVerifier.create(checkOrder.log())
		.expectNextMatches(saveOrderPred)
		.verifyComplete();	

	}
	*/
	
	
	
	
	
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
