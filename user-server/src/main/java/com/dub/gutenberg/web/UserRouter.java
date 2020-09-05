package com.dub.gutenberg.web;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;


@Configuration
public class UserRouter {

	@Bean
	public RouterFunction<ServerResponse> route(UserHandler userHandler) {

	    return RouterFunctions
	      .route(RequestPredicates.POST("/createUser")
	    		  .and(RequestPredicates.accept(MediaType.APPLICATION_JSON)), userHandler::createUser)
	      .andRoute(RequestPredicates.POST("/primaryAddress")
	    		  .and(RequestPredicates.accept(MediaType.APPLICATION_JSON)), userHandler::primaryAddress)
	      .andRoute(RequestPredicates.POST("/primaryPayment")
	    		  .and(RequestPredicates.accept(MediaType.APPLICATION_JSON)), userHandler::primaryPayment)
	      .andRoute(RequestPredicates.POST("/addAddress")
	    		  .and(RequestPredicates.accept(MediaType.APPLICATION_JSON)), userHandler::addAddress)
	      .andRoute(RequestPredicates.POST("/addPaymentMethod")
	    		  .and(RequestPredicates.accept(MediaType.APPLICATION_JSON)), userHandler::addPaymentMethod)
	      .andRoute(RequestPredicates.POST("/deleteAddress")
	    		  .and(RequestPredicates.accept(MediaType.APPLICATION_JSON)), userHandler::deleteAddress)
	      .andRoute(RequestPredicates.POST("/deletePaymentMethod")
	    		  .and(RequestPredicates.accept(MediaType.APPLICATION_JSON)), userHandler::deletePaymentMethod)
	      .andRoute(RequestPredicates.POST("/findById")
	    		  .and(RequestPredicates.accept(MediaType.APPLICATION_JSON)), userHandler::findById)
	      .andRoute(RequestPredicates.GET("/userByName/{username}")
	    		  .and(RequestPredicates.accept(MediaType.APPLICATION_JSON)), userHandler::getUserByName);

	    
	}

	
	/*
	@Bean
	public RouterFunction<ServerResponse> setPrimaryAddress(UserHandler userHandler) {

	    return RouterFunctions
	      .route(RequestPredicates.POST("/primaryAddress")
	    		  .and(RequestPredicates.accept(MediaType.APPLICATION_JSON)), userHandler::primaryAddress);
	}
*/
	
	/*
	@Bean
	public RouterFunction<ServerResponse> setPrimaryPayment(UserHandler userHandler) {

	    return RouterFunctions
	      .route(RequestPredicates.POST("/primaryPayment")
	    		  .and(RequestPredicates.accept(MediaType.APPLICATION_JSON)), userHandler::primaryPayment);
	}
*/
	
	/*
	@Bean
	public RouterFunction<ServerResponse> addAddress(UserHandler userHandler) {

	    return RouterFunctions
	      .route(RequestPredicates.POST("/addAddress")
	    		  .and(RequestPredicates.accept(MediaType.APPLICATION_JSON)), userHandler::addAddress);
	}
	*/
	
	/*
	@Bean
	public RouterFunction<ServerResponse> addPaymentMethodRoute(UserHandler userHandler) {

	    return RouterFunctions
	      .route(RequestPredicates.POST("/addPaymentMethod")
	    		  .and(RequestPredicates.accept(MediaType.APPLICATION_JSON)), userHandler::addPaymentMethod);
	}
	*/
	
	
	/*
	@Bean
	public RouterFunction<ServerResponse> deleteAddressRoute(UserHandler userHandler) {

	    return RouterFunctions
	      .route(RequestPredicates.POST("/deleteAddress")
	    		  .and(RequestPredicates.accept(MediaType.APPLICATION_JSON)), userHandler::deleteAddress);
	}
	*/
	
	/*
	@Bean
	public RouterFunction<ServerResponse> deletePaymentMethodRoute(UserHandler userHandler) {

	    return RouterFunctions
	      .route(RequestPredicates.POST("/deletePaymentMethod")
	    		  .and(RequestPredicates.accept(MediaType.APPLICATION_JSON)), userHandler::deletePaymentMethod);
	}
	*/
	
	/*
	@Bean
	public RouterFunction<ServerResponse> findByIdRoute(UserHandler userHandler) {

	    return RouterFunctions
	      .route(RequestPredicates.POST("/findById")
	    		  .and(RequestPredicates.accept(MediaType.APPLICATION_JSON)), userHandler::findById);
	}
	*/
	
	/*
	@Bean
	public RouterFunction<ServerResponse> route(UserHandler userHandler) {

	    return RouterFunctions
	      .route(RequestPredicates.GET("/userByName/{username}")
	    		  .and(RequestPredicates.accept(MediaType.APPLICATION_JSON)), userHandler::getUserByName);
	}
	*/
	
	      /*
	      .andRoute(RequestPredicates.GET("/books/{categorySlug}/sort/{sortBy}")
	    		  .and(RequestPredicates.accept(MediaType.APPLICATION_JSON)), bookHandler::allBooksByCategory)  
	      .andRoute(RequestPredicates.GET("/bookById/{id}")
	  */
}
