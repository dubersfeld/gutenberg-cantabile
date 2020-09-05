package com.dub.gutenberg;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.function.Function;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.dub.gutenberg.domain.Review;
import com.dub.gutenberg.services.ReviewService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;


@SpringBootTest
public class ReviewServiceTests {
	
	@Autowired
	ReviewService reviewService;
				
	private Predicate<Review> testReviewById = 
					review -> "3".equals(review.getBookId()) &&
							"8".equals(review.getUserId());
			
	private Predicate<Double> testBookRating = 
					rating -> (rating == 4.0);
									
							
	/** 
	 * Here I use classical tests, not StepVerifier
	 * */
	@Test
	void testCreateReview() {	     
		Review review = new Review();
		review.setBookId("1");
		review.setUserId("6");
		review.setText("Lorem ipsum");
		review.setRating(3);
		
		// actual creation
		Mono<String> reviewId = this.reviewService.createReview(review);
		
		Mono<Review> check = reviewId.flatMap(transformById);
		Review checkReview = check.block();
		
		assertEquals("1", checkReview.getBookId());
		assertEquals("6", checkReview.getUserId());
		assertEquals("Lorem ipsum", checkReview.getText());
		assertEquals(3, checkReview.getRating());
	}
	
					
	@Test
	void testReviewById() {
		String reviewId = "3";
		Mono<Review> review = this.reviewService.getReviewById(reviewId);
		StepVerifier.create(review.log())
		.expectNextMatches(testReviewById)
		.verifyComplete();
	}
	
	@Test
	void testReviewsByUserId() {
		String userId = "8";
		Flux<Review> reviews = this.reviewService.getReviewsByUserId(userId);
		StepVerifier.create(reviews.log())
		.expectNextCount(4)
		.verifyComplete();
		
	
	}
	
	@Test
	void testReviewsByBookId() {
		String bookId = "7";
		Flux<Review> reviews;
		try {
			reviews = this.reviewService.getReviewsByBookId(bookId, "rating");
			StepVerifier.create(reviews.log())
			.expectNextCount(3)
			.verifyComplete();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	void testBookRating() {
		String bookId = "2";
		Mono<Double> rating;
		try {
			rating = this.reviewService.getBookRating(bookId);
			StepVerifier.create(rating.log())
			.expectNextMatches(testBookRating)
			.verifyComplete();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	void testBookRating2() {
		String bookId = "42";
		Mono<Double> rating;
		try {
			rating = this.reviewService.getBookRating(bookId);
			StepVerifier.create(rating.log())
			.verifyComplete();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	
	/** 
	 * The same test is executed twice with different arguments
	 * The first result should be OK, the second error
	 * */
	
	@Test
	void testVoteHelpful() {
		String reviewId = "2";
		String userId = "6";
		try {
			Mono<Boolean> status = this.reviewService.voteHelpful(reviewId, userId, true);
			StepVerifier.create(status.log())
			.expectNextMatches(s -> (s == true))
			.verifyComplete();
		
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	@Test
	void testVoteHelpful2() {
		String reviewId = "5";
		String userId = "6";
		try {
			Mono<Boolean> status = this.reviewService.voteHelpful(reviewId, userId, true);
			StepVerifier.create(status.log())
			.expectNextMatches(s -> (s == false))
			.verifyComplete();
		
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	
	// all helper functions
	Function<String, Mono<Review>> transformById =
			reviewId -> {
				return this.reviewService.getReviewById(reviewId);
	};
	
	

}
