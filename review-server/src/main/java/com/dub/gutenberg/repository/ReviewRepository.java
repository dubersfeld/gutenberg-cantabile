package com.dub.gutenberg.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import com.dub.gutenberg.domain.Review;

import reactor.core.publisher.Flux;

public interface ReviewRepository extends ReactiveCrudRepository<Review, String> {
	
	public Flux<Review> findByUserId(String userId);
	
	public Flux<Review> findByBookId(String userId);

}
