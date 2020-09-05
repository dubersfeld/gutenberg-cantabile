package com.dub.gutenberg.services;

import java.io.IOException;

import com.dub.gutenberg.domain.Address;
import com.dub.gutenberg.domain.MyUser;
import com.dub.gutenberg.domain.PaymentMethod;

import reactor.core.publisher.Mono;

public interface UserService {
 
	Mono<MyUser> findById(String userId);	
		
	Mono<MyUser> findByUsername(String username);
	
	// all profile changes
	Mono<MyUser> setPrimaryAddress(String userId, int index);
		
	Mono<MyUser> setPrimaryPayment(String userId, int index);
	
	Mono<MyUser> addAddress(String userId, Address newAddress);
		
	Mono<MyUser> addPaymentMethod(String userId, PaymentMethod newPayment);
	
	Mono<MyUser> deleteAddress(String userId, Address delAddress);
		
	Mono<MyUser> deletePaymentMethod(String userId, PaymentMethod payMeth);
	
	
	
	// new user registration
	//String createUser(MyUser user) throws IOException;
	
	Mono<String> createUser(MyUser user);
	 
}