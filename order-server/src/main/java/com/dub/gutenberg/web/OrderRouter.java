package com.dub.gutenberg.web;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class OrderRouter {
	
	@Bean
	public RouterFunction<ServerResponse> route(OrderHandler orderHandler) {

		// only one GET request all others are POST
		return RouterFunctions
		      .route(RequestPredicates.GET("/orderById/{orderId}")
		    		  .and(RequestPredicates.accept(MediaType.APPLICATION_JSON)), orderHandler::getOrderById)    
		      .andRoute(RequestPredicates.POST("/createOrder")
		    		  .and(RequestPredicates.accept(MediaType.APPLICATION_JSON)), orderHandler::createOrder)    
		      .andRoute(RequestPredicates.POST("/getActiveOrder")
		    		  .and(RequestPredicates.accept(MediaType.APPLICATION_JSON)), orderHandler::getActiveOrder)    
		      .andRoute(RequestPredicates.POST("/getActiveOrder")
		    		  .and(RequestPredicates.accept(MediaType.APPLICATION_JSON)), orderHandler::getActiveOrder)   
		      .andRoute(RequestPredicates.POST("/setOrderState")
		    		  .and(RequestPredicates.accept(MediaType.APPLICATION_JSON)), orderHandler::setOrderState)    
		      .andRoute(RequestPredicates.POST("/checkoutOrder")
		    		  .and(RequestPredicates.accept(MediaType.APPLICATION_JSON)), orderHandler::checkoutOrder)   
		      .andRoute(RequestPredicates.POST("/updateOrder")
		    		  .and(RequestPredicates.accept(MediaType.APPLICATION_JSON)), orderHandler::updateOrder)    
		      .andRoute(RequestPredicates.POST("/addBookToOrder")
		    		  .and(RequestPredicates.accept(MediaType.APPLICATION_JSON)), orderHandler::addBookToOrder) 
		      .andRoute(RequestPredicates.POST("/getBooksNotReviewed")
		    		  .and(RequestPredicates.accept(MediaType.APPLICATION_JSON)), orderHandler::getBooksNotReviewed)    
		      .andRoute(RequestPredicates.POST("/editCart")
		    		  .and(RequestPredicates.accept(MediaType.APPLICATION_JSON)), orderHandler::editCart);    
	}	
	
}
