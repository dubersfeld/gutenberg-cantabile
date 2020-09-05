package com.dub.gutenberg.web;


import java.net.URI;
import java.net.URISyntaxException;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.dub.gutenberg.domain.AddressOperations;
import com.dub.gutenberg.domain.MyUser;
import com.dub.gutenberg.domain.PaymentOperations;
import com.dub.gutenberg.domain.Primary;
import com.dub.gutenberg.exceptions.DuplicateUserException;
import com.dub.gutenberg.exceptions.UserNotFoundException;
import com.dub.gutenberg.services.UserService;

import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

@Component
public class UserHandler {
	
	@Autowired 
	private UserService userService;
	
	@Value("${baseUsersUrl}")
	private String baseUsersURL;
	
	public Mono<ServerResponse> createUser(ServerRequest request) {
		
		
		
		final Mono<MyUser> user = request.bodyToMono(MyUser.class);	
		
		/** First check if user is already present */	
		Mono<Tuple2<MyUser,Boolean>> tuple = user.flatMap(checkUserTransform);
		
		/** 
		 * Here Boolean true means that username already present
		 * Here one more time I need to use a Mono<Tuple2<MyUser, String>>
		*/
		Mono<URI> location = tuple.flatMap(t -> {
			if (!t.getT2()) {
				// accepted
				Mono<String> newUser = this.userService.createUser(t.getT1());
				return newUser.flatMap(transformCreate);
			} else {
				// rejected
				return Mono.error(new DuplicateUserException());
			}
		});
		
		
	

		return location
				.flatMap(createUserSuccess)
				.onErrorResume(createUserFallback);
	}


	public Mono<ServerResponse> primaryAddress(ServerRequest request) {
		
		Mono<Primary> primary = request.bodyToMono(Primary.class);
		
		/** here I use Mono.zip method */
		Mono<Tuple2<MyUser, Integer>> tuple = primary.flatMap(transformSetPrimaryAddress);
		
		Mono<MyUser> user = tuple.flatMap(transformSetPrimaryAddress2);
	
		return user
				.flatMap(primaryAddressSuccess)
				.onErrorResume(primaryAddressFallback);	
	}
	
	public Mono<ServerResponse> primaryPayment(ServerRequest request) {
		
		Mono<Primary> primary = request.bodyToMono(Primary.class);
		
		/** here I use Mono.zip method */
		Mono<Tuple2<MyUser, Integer>> tuple = primary.flatMap(transformSetPrimaryPayment);
		
		Mono<MyUser> user = tuple.flatMap(transformSetPrimaryPayment2);
	
		return user
				.flatMap(primaryPaymentSuccess)
				.onErrorResume(primaryPaymentFallback);
	}

	public Mono<ServerResponse> addAddress(ServerRequest request) {
	
		return request
				.bodyToMono(AddressOperations.class)
				.flatMap(transformAddAddress)
				.flatMap(userSuccess)
				.onErrorResume(userFallback);
	}
	
	public Mono<ServerResponse> addPaymentMethod(ServerRequest request) {
			
		return request
				.bodyToMono(PaymentOperations.class)
				.flatMap(transformAddPaymentMethod)	
				.flatMap(userSuccess)
				.onErrorResume(userFallback);
	}
	
	public Mono<ServerResponse> deleteAddress(ServerRequest request) {
			
		return request
				.bodyToMono(AddressOperations.class)
				.flatMap(transformDeleteAddress)	
				.flatMap(userSuccess)
				.onErrorResume(userFallback);
	}
	

	public Mono<ServerResponse> deletePaymentMethod(ServerRequest request) {
			
		return request
				.bodyToMono(PaymentOperations.class)
				.flatMap(transformDeletePaymentMethod)
				.flatMap(userSuccess)
				.onErrorResume(userFallback);
	}
	
	public Mono<ServerResponse> findById(ServerRequest request) {
		
		
		return request
				.bodyToMono(String.class)
				.flatMap(transformById)
				.flatMap(userSuccess)
				.onErrorResume(userFallback);
	}
	
	
	public Mono<ServerResponse> getUserByName(ServerRequest request) {
		Mono<String> username = Mono.just(request.pathVariable("username"));
		
		
		
		return username
				.flatMap(transformByUsername)
				.flatMap(userSuccess)
				.onErrorResume(userFallback);
	}

	
	// all utility functions
	
	Function<Primary, Mono<Tuple2<MyUser,Integer>>> transformSetPrimaryPayment =
			s -> {
					System.err.println("FORGE " + s.getUsername());
					try {
							Mono<MyUser> enclume = userService.findByUsername(s.getUsername());
							Mono<Integer> index = Mono.just(s.getIndex());
							return Mono.zip(enclume, index);
					} catch (Exception e) {
							e.printStackTrace();
							throw new RuntimeException("SATOR");
						}
					};		
	
	Function<Tuple2<MyUser, Integer>, Mono<MyUser>> transformSetPrimaryPayment2 =
			s -> {
					String userId = s.getT1().getId();
					int index = s.getT2();
						
					try {
						return userService.setPrimaryPayment(userId, index);
					} catch (Exception e) {
						e.printStackTrace();
						throw new RuntimeException("SATOR");
					}		
	};
	
	Function<URI, Mono<ServerResponse>> createUserSuccess =
			location -> {
				return ServerResponse.created(location).build();
	};
	
	Function<MyUser, Mono<ServerResponse>> primaryPaymentSuccess =
			s -> {
					return ServerResponse.ok()
								.contentType(MediaType.APPLICATION_JSON)
								.body(Mono.just(s), MyUser.class);
	};		
	
	Function<Throwable, Mono<ServerResponse>> primaryPaymentFallback =
			e -> {
					if (e.getClass() == UserNotFoundException.class) {
						System.err.println("LOREM IPSUM");
						return ServerResponse
								.status(HttpStatus.NOT_FOUND)
								.build();
					} else {
						return ServerResponse
								.status(HttpStatus.INTERNAL_SERVER_ERROR)
								.build();			
					}
	};

	Function<MyUser, Mono<ServerResponse>> primaryAddressSuccess =
			s -> {
					return ServerResponse.ok()
								.contentType(MediaType.APPLICATION_JSON)
								.body(Mono.just(s), MyUser.class);
	};		
	
	Function<Throwable, Mono<ServerResponse>> primaryAddressFallback =
			e -> {
					if (e.getClass() == UserNotFoundException.class) {
						System.err.println("LOREM IPSUM");
						return ServerResponse
								.status(HttpStatus.NOT_FOUND)
								.build();
					} else {
						return ServerResponse
								.status(HttpStatus.INTERNAL_SERVER_ERROR)
								.build();			
					}
	};
	
	Function<Tuple2<MyUser, Integer>, Mono<MyUser>> transformSetPrimaryAddress2 =
			s -> {
					String userId = s.getT1().getId();
					int index = s.getT2();
									
					try {
						return userService.setPrimaryAddress(userId, index);
					} catch (Exception e) {
						e.printStackTrace();
						throw new RuntimeException("SATOR");
					}		
	};
	
	Function<Primary, Mono<Tuple2<MyUser,Integer>>> transformSetPrimaryAddress =
			s -> {
					System.err.println("FORGE " + s.getUsername());
					try {
						Mono<MyUser> enclume = userService.findByUsername(s.getUsername());
						Mono<Integer> index = Mono.just(s.getIndex());
						return Mono.zip(enclume, index);
					} catch (Exception e) {
						e.printStackTrace();
						throw new RuntimeException("SATOR");
					}
	};	
	
	// quick and dirty implementation, try to clean later
	/*
	Function<MyUser, Mono<Tuple2<MyUser, String>>> checkUserTransform =
			user -> {
			
				Mono<MyUser> check = this.userService.findByUsername(user.getUsername());
			
				Mono<String> name = check.map(u -> {
							return u.getUsername();
						}).onErrorResume(e -> {return Mono.just("");});
					
				return Mono.zip(Mono.just(user), name);
				
	};
	*/
	
	Function<MyUser, Mono<Tuple2<MyUser, Boolean>>> checkUserTransform =
			user -> {
			
				Mono<MyUser> check = this.userService.findByUsername(user.getUsername());
			
				Mono<Boolean> conflict = check.map(u -> {
							return (!"".equals(u.getUsername()));
						}).onErrorResume(e -> {return Mono.just(false);});
					
				return Mono.zip(Mono.just(user), conflict);
				
	};
	
	Function<Throwable, Mono<ServerResponse>> createUserFallback =
			e -> {
					if (e.getClass() == DuplicateUserException.class) {
						System.err.println("LOREM IPSUM");
						return ServerResponse
									.status(HttpStatus.CONFLICT)
									.build();
					} else {
						return ServerResponse
								.status(HttpStatus.INTERNAL_SERVER_ERROR)
								.build();			
					}
	};
	
	Function<MyUser, Mono<ServerResponse>> userSuccess =
			s -> {
				
					return ServerResponse.ok()
								.contentType(MediaType.APPLICATION_JSON)
								.body(Mono.just(s), MyUser.class);
	};		
	
	Function<Throwable, Mono<ServerResponse>> userFallback =
			e -> {
					if (e.getClass() == UserNotFoundException.class) {
						System.err.println("LOREM IPSUM");
						return ServerResponse
								.status(HttpStatus.NOT_FOUND)
								.build();
					} else {
						return ServerResponse
								.status(HttpStatus.INTERNAL_SERVER_ERROR)
								.build();			
					}
	};
	
	Function<AddressOperations, Mono<MyUser>> transformAddAddress = 		
			s -> {
				try {			
					return userService.addAddress(s.getUserId(), s.getAddress());
				} catch (Exception e) {
					e.printStackTrace();
					throw new RuntimeException("SATOR");
				}
	};
			
	Function<PaymentOperations, Mono<MyUser>> transformAddPaymentMethod = 
			s -> {
				try {			
					return userService.addPaymentMethod(s.getUserId(), s.getPaymentMethod());
				} catch (Exception e) {
					e.printStackTrace();
					throw new RuntimeException("SATOR");
				}
	};
	
	Function<AddressOperations, Mono<MyUser>> transformDeleteAddress = 
			s -> {
				try {	
					System.out.println("FUTRE " + s.getUserId());
					return userService.deleteAddress(s.getUserId(), s.getAddress());
				} catch (Exception e) {
					e.printStackTrace();
					throw new RuntimeException("SATOR");
				}
	};
	
	Function<PaymentOperations, Mono<MyUser>> transformDeletePaymentMethod = 
			s -> {
				try {			
					return userService.deletePaymentMethod(s.getUserId(), s.getPaymentMethod());
				} catch (Exception e) {
					e.printStackTrace();
					throw new RuntimeException("SATOR");
				}
	};
	
	Function<String, Mono<MyUser>> transformById = 
			s -> {
				try {	
					
					return userService.findById(s);
				} catch (Exception e) {
					e.printStackTrace();
					throw new RuntimeException("SATOR");
				}
	};
	
	Function<String, Mono<MyUser>> transformByUsername = 
			s -> {
				try {		
					
					return userService.findByUsername(s);
				} catch (Exception e) {
					e.printStackTrace();
					throw new RuntimeException("SATOR");
				}
	};
	
	private Function<String, Mono<URI>> transformCreate =
			userId -> {
				
				try {
					return Mono.just(userId)
							.flatMap(s -> {
								try {
									return Mono.just(new URI(baseUsersURL + "/findById/" + s));
								} catch (URISyntaxException e) {
									e.printStackTrace();
									return Mono.error(new RuntimeException("SATOR"));
								}
							});
				} catch (Exception e) {
					// return custom exception wrapped in a Mono
					return Mono.error(new RuntimeException("SATOR"));
				}
	};
	
}
