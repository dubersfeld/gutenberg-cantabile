package com.dub.gutenberg.repository;


import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import com.dub.gutenberg.domain.MyUser;

import reactor.core.publisher.Flux;

public interface UserRepository extends ReactiveCrudRepository<MyUser, String> {

	public Flux<MyUser> findByUsername(String username);
	
}

