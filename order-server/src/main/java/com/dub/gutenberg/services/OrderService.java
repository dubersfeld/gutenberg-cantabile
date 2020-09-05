package com.dub.gutenberg.services;

import java.io.IOException;
import java.text.ParseException;

import com.dub.gutenberg.domain.EditCart;
import com.dub.gutenberg.domain.Order;
import com.dub.gutenberg.domain.OrderState;
import com.dub.gutenberg.domain.UserAndReviewedBooks;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Clean this stuff later, 
 * revert to initial naming convention 
 * */
public interface OrderService {
	
	Flux<String> getBooksNotReviewed(UserAndReviewedBooks userAndReviewedBooks) 
											throws ParseException, IOException;
	
	Mono<Order> saveOrder(Order order, boolean creation);	
		
	Mono<Order> getOrderById(String orderId);

	Mono<Order> getActiveOrder(String userId) throws IOException;

	Mono<Order> addBookToOrder(String orderId, String bookId) throws IOException;
	
	Mono<Order> editCart(EditCart editCart) throws IOException;	
	
	Mono<Order> setOrderState(String orderId, OrderState state) throws IOException;
		
	Mono<Order> checkoutOrder(String orderId) throws IOException;			
}
