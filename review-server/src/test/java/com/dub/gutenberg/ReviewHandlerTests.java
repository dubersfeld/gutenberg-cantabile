package com.dub.gutenberg;


import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.dub.gutenberg.domain.Review;
import com.dub.gutenberg.web.ReviewHandler;
import com.dub.gutenberg.web.ReviewRouter;
import com.dub.gutenberg.web.ReviewVote;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@SpringBootTest
public class ReviewHandlerTests {

	@Autowired
	ReviewRouter reviewRouter;
	
	@Autowired
	ReviewHandler reviewHandler;
	
	private Predicate<Review> reviewByIdPred = 
			review -> "3".equals(review.getBookId()) &&
					"8".equals(review.getUserId());

	private Predicate<Double> bookRatingPred = 
					rating -> (rating == 4.0);
			
	@Test
	void reviewsByUserId() {
		String userId = "6";
		WebTestClient
		.bindToRouterFunction(reviewRouter.route(reviewHandler))
		.build()
		.method(HttpMethod.POST)
		.uri("/reviewsByUserId")
		.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
		.body(Mono.just(userId), String.class)
		.exchange()
		.expectStatus().isOk()
		.returnResult(Review.class)
		.getResponseBody()// it is a Flux<Review>
		.as(StepVerifier::create)
		.expectNextCount(5)
		.expectComplete()
		.verify();	
	}	
	
	@Test
	void reviewsByUserId2() {
		String userId = "42";
		WebTestClient
		.bindToRouterFunction(reviewRouter.route(reviewHandler))
		.build()
		.method(HttpMethod.POST)
		.uri("/reviewsByUserId")
		.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
		.body(Mono.just(userId), String.class)
		.exchange()
		.expectStatus().isOk()
		.returnResult(Review.class)
		.getResponseBody()// it is a Flux<Review>
		.as(StepVerifier::create)
		.expectComplete()// no reviews found with this userId
		.verify();	
	}	
	
	@Test
	void testByBookId() {
		WebTestClient
		.bindToRouterFunction(reviewRouter.route(reviewHandler))
		.build()
		.method(HttpMethod.GET)
		.uri("/reviewsByBookId/7/sort/rating")
		.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
		.exchange()
		.expectStatus().isOk()
		.returnResult(Review.class)
		.getResponseBody()// it is a Flux<Review>
		.as(StepVerifier::create)
		.expectNextCount(3)
		.expectComplete()
		.verify();	
	}	
	
	@Test
	void testCreateReview() {
		Review review = new Review();
		review.setBookId("3");
		review.setUserId("7");
		review.setText("Lorem ipsum");
		review.setRating(3);
		
		HttpHeaders headers = WebTestClient
		.bindToRouterFunction(reviewRouter.route(reviewHandler))
		.build()
		.method(HttpMethod.POST)
		.uri("/createReview")
		.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
		.body(Mono.just(review), Review.class)
		.exchange()
		.expectStatus().isCreated()
		.returnResult(String.class)
		.getResponseHeaders();// HttpHeaders
		
		
		
		String location = headers.get("location").get(0);
		String patternString = "^http://localhost:8082/reviewById/[A-Za-z0-9_-]*$";  
		Pattern pattern = Pattern.compile(patternString);
		Matcher matcher = pattern.matcher(location);
		assertEquals(true, matcher.matches());
	}	
	
	@Test
	void testVoteHelpful() {
		String reviewId = "3";
		String userId = "8";
		ReviewVote reviewVote = new ReviewVote();
		reviewVote.setHelpful(true);
		reviewVote.setUserId(userId);
			
		WebTestClient
		.bindToRouterFunction(reviewRouter.route(reviewHandler))
		.build()
		.method(HttpMethod.POST)
		.uri("/addVote/" + reviewId)
		.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
		.body(Mono.just(reviewVote), ReviewVote.class)
		.exchange()
		.expectStatus().isOk();
	}	
	
	@Test
	void testVoteHelpful2() {
		String reviewId = "5";
		String userId = "6";
		ReviewVote reviewVote = new ReviewVote();
		reviewVote.setHelpful(true);
		reviewVote.setUserId(userId);
			
		WebTestClient
		.bindToRouterFunction(reviewRouter.route(reviewHandler))
		.build()
		.method(HttpMethod.POST)
		.uri("/addVote/" + reviewId)
		.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
		.body(Mono.just(reviewVote), ReviewVote.class)
		.exchange()
		.expectStatus().isEqualTo(HttpStatus.CONFLICT);// userId has already voted
	
	}	
	
	@Test
	void testById() {
		WebTestClient
		.bindToRouterFunction(reviewRouter.route(reviewHandler))
		.build()
		.method(HttpMethod.GET)
		.uri("/reviewById/3")
		.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
		.exchange()
		.expectStatus().isOk()
		.returnResult(Review.class)
		.getResponseBody()// it is a Flux<Review>
		.as(StepVerifier::create)
		.expectNextMatches(reviewByIdPred)
		.expectComplete()
		.verify();	
	}	
	
	@Test
	void testBookRating() {
		WebTestClient
		.bindToRouterFunction(reviewRouter.route(reviewHandler))
		.build()
		.method(HttpMethod.GET)
		.uri("/bookRating/2")
		.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
		.exchange()
		.expectStatus().isOk()
		.returnResult(Double.class)
		.getResponseBody()// it is a Flux<Double>
		.as(StepVerifier::create)
		.expectNextMatches(bookRatingPred)
		.expectComplete()
		.verify();	
	}	
	
	@Test
	void testBookRating2() {
		WebTestClient
		.bindToRouterFunction(reviewRouter.route(reviewHandler))
		.build()
		.method(HttpMethod.GET)
		.uri("/bookRating/42")
		.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
		.exchange()
		.expectStatus().isOk()
		.returnResult(Double.class)
		.getResponseBody()// it is a Flux<Double>
		.as(StepVerifier::create)
		.expectComplete()// rating not available
		.verify();	
	}	
}
