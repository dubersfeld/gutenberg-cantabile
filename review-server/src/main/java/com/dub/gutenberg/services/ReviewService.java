package com.dub.gutenberg.services;

import java.io.IOException;

import com.dub.gutenberg.domain.Review;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


public interface ReviewService {

	Mono<String> createReview(Review review);	
	
	Mono<Review> getReviewById(String reviewId);	

	Flux<Review> getReviewsByUserId(String userId);
	
	Flux<Review> getReviewsByBookId(
			String bookId, 
			String sortBy) throws IOException;
	
	Mono<Double> getBookRating(String bookId) throws IOException;
	
	Mono<Boolean> voteHelpful(String reviewId, String userId, boolean helpful) throws IOException;
	
}