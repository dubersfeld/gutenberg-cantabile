package com.dub.gutenberg.services;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dub.gutenberg.domain.Address;
import com.dub.gutenberg.domain.MyUser;
import com.dub.gutenberg.domain.PaymentMethod;
import com.dub.gutenberg.exceptions.DuplicateUserException;
import com.dub.gutenberg.exceptions.UserNotFoundException;
import com.dub.gutenberg.repository.UserRepository;
import com.dub.gutenberg.repository.UserRepositoryOld;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
//import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.reactive.ReactiveElasticsearchClient;
import org.springframework.data.elasticsearch.client.reactive.ReactiveRestClients;
import org.springframework.web.reactive.function.client.ExchangeStrategies;

import com.dub.gutenberg.config.ElasticsearchConfig;


@Service
public class UserServiceImpl implements UserService {

	public static final String INDEX = "gutenberg-users";
	public static final String TYPE = "_doc";
	
	
	//@Autowired
	//private ReactiveElasticsearchClient reactiveClient;
	
	@Autowired
	private UserRepository userRepository;
	
	//@Autowired
	//private UserRepositoryOld userRepo;

	@Override
	public Mono<MyUser> findById(String userId) {
		// should never be null
		
		Mono<MyUser> user = userRepository.findById(userId);
		Mono<Boolean> hasElement = user.hasElement();
		
		return hasElement.flatMap(
				b -> {
					if (b) {
						System.err.println("SATOR");
						return user;
					} else {
						
						return Mono.error(new UserNotFoundException());			
		}});
	}
	
	@Override
	public Mono<MyUser> findByUsername(String username) {
	
	
		Flux<MyUser> userF = this.userRepository.findByUsername(username);
		Mono<MyUser> user = userF.next();
		Mono<Boolean> hasElement = user.hasElement();
		
		return hasElement.flatMap(
				b -> {
					if (b) {
						
						return user;
					} else {
						
						return Mono.error(new UserNotFoundException());			
		}});
	}

	@Override
	public Mono<MyUser> setPrimaryAddress(String userId, int index) {
	
		// first retrieve user by id
		Mono<MyUser> user = this.findById(userId);	
		
		Mono<MyUser> updatedUser = user.flatMap(u -> {
					u.setMainShippingAddress(index);
					return this.userRepository.save(u);
		});
				
		return updatedUser;	
	}
		
	@Override
	public Mono<MyUser> setPrimaryPayment(String userId, int index) {
	
		// first retrieve user by id
		Mono<MyUser> user = this.findById(userId);	
			
		Mono<MyUser> updatedUser = user.flatMap(u -> {
							u.setMainPayMeth(index);
							return this.userRepository.save(u);
		});
				
		return updatedUser;
	}
	
	/*
	
*/
	
	@Override
	public Mono<MyUser> addAddress(String userId, Address newAddress) {	
		// first retrieve user by id
		Mono<MyUser> user = this.findById(userId);	
				
		Mono<MyUser> grunge = user.flatMap(u -> {
					List<Address> addresses = u.getAddresses();
					addresses.add(newAddress);
					return this.userRepository.save(u);
		});
				
		return grunge;	
	}
	
	@Override
	public Mono<MyUser> addPaymentMethod(String userId, PaymentMethod newPayment) {
		// first retrieve user by id
		Mono<MyUser> user = this.findById(userId);	
						
		Mono<MyUser> updatedUser = user.flatMap(u -> {
					List<PaymentMethod> payMeths = u.getPaymentMethods();
					payMeths.add(newPayment);
					return this.userRepository.save(u);
		});
				
		return updatedUser;	
	}
	
	@Override
	public Mono<MyUser> deleteAddress(String userId, Address delAddress) {	
		
		// first retrieve user by id
		Mono<MyUser> user = this.findById(userId);	
		
		Mono<MyUser> updatedUser = user.flatMap(u -> {
					List<Address> addresses = u.getAddresses();
					addresses.remove(delAddress);
					return this.userRepository.save(u);
		});
				
		return updatedUser;
	
	}

	@Override
	public Mono<MyUser> deletePaymentMethod(String userId, PaymentMethod payMeth) {
		// first retrieve user by id
		Mono<MyUser> user = this.findById(userId);	
	
		Mono<MyUser> updatedUser = user.flatMap(u -> {
					List<PaymentMethod> payMeths = u.getPaymentMethods();
					payMeths.remove(payMeth);
					return this.userRepository.save(u);
		});
					
		return updatedUser;
	}

	@Override
	public Mono<String> createUser(MyUser user) {
		
		Mono<MyUser> newUser = this.userRepository.save(user);
		
		return newUser.map(u -> {
			return u.getId();
		});
	}	
}
