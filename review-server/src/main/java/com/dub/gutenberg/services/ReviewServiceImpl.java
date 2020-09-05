package com.dub.gutenberg.services;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.metrics.Avg;
import org.elasticsearch.search.aggregations.metrics.AvgAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.client.reactive.ReactiveElasticsearchClient;
import org.springframework.stereotype.Service;

import com.dub.gutenberg.domain.Review;
import com.dub.gutenberg.repository.ReviewRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;


@Service
public class ReviewServiceImpl implements ReviewService{

	public static final String REVIEWS = "gutenberg-reviews";
	
	@Autowired 
	private ObjectMapper objectMapper;// provided
	
	@Autowired
	private ReactiveElasticsearchClient reactiveClient;
	
	@Autowired
	private ReviewRepository reviewRepository;

	@Override
	public Mono<String> createReview(Review review) {
		
		Mono<Review> newReview = this.reviewRepository.save(review);
		
		return newReview.flatMap(r -> {
			
			return Mono.just(r.getId());
		});
	}

	@Override
	public Mono<Review> getReviewById(String reviewId) {
		
		return this.reviewRepository.findById(reviewId);
	}

	@Override
	public Flux<Review> getReviewsByUserId(String userId) {
		
		return this.reviewRepository.findByUserId(userId);
	}

	@Override
	public Flux<Review> getReviewsByBookId(String bookId, String sortBy) throws IOException {
	
		/**
		 * Here with SortBy a ReactiveClient is still needed
		 * */
		
		SearchRequest searchRequest = new SearchRequest();
		
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder(); 
		searchSourceBuilder
			.query(QueryBuilders.termQuery("bookId.keyword", bookId))
			.sort(sortBy, SortOrder.DESC);
		searchRequest.source(searchSourceBuilder); 
		searchRequest.indices(REVIEWS);

		Flux<SearchHit> response 
			= reactiveClient.search(searchRequest);

	
				
		return response.map(mapper);
	}

	
	@Override
	public Mono<Double> getBookRating(String bookId) throws IOException {
		 
		
	 
		SearchRequest searchRequest = new SearchRequest();
					
		AvgAggregationBuilder aggregation 
				= AggregationBuilders
					.avg("book_rating")
					.field("rating");
				
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder(); 
		searchSourceBuilder
				.query(QueryBuilders.termQuery("bookId.keyword", bookId))
				.aggregation(aggregation);
		
		searchRequest.source(searchSourceBuilder); 
		searchRequest.indices(REVIEWS);
	
		
		Flux<Aggregation> response = reactiveClient.aggregate(searchRequest);
		Mono<Aggregation> aggreg = response.next();
		Mono<Double> avg = aggreg.flatMap(avgTransform);
		return avg;// may be empty
	}
	
	
	@Override
	public Mono<Boolean> voteHelpful(String reviewId, String userId, boolean helpful) throws IOException {
		
		
		Mono<Review> review = this.doGetReactiveReviewById(reviewId);
		
		
		Mono<Tuple2<Review, Boolean>> voteAttempt = review.flatMap(r -> {
			
			r.setHelpfulVotes(r.getHelpfulVotes() + (helpful? 1 : 0));
			
			
			Set<String> voterIds = r.getVoterIds();
			boolean success = voterIds.add(userId);
			
			
			return Mono.zip(Mono.just(r), Mono.just(success));
		});
		
		Mono<Boolean> success = voteAttempt.flatMap(g -> {
			if (g.getT2()) {
				
				Mono<Review> newReview = this.reviewRepository.save(g.getT1());
				newReview.subscribe();// needed to force the actual save				
			}
			return Mono.just(g.getT2());
		});
		
		return success;
	}

	private Mono<Review> doGetReactiveReviewById(String reviewId) {
		return this.reviewRepository.findById(reviewId);
	}
	
	private Function<SearchHit, Review> mapper =
			hit -> {
				System.out.println(hit);
				Map<String, Object> sourceAsMap = hit.getSourceAsMap();
				
				Review review = objectMapper.convertValue(sourceAsMap, Review.class);
				review.setId(hit.getId());
				return review;
	};	
	

	private Function<Aggregation, Mono<Double>> avgTransform =
			aggreg -> {
				Double rating = null;
				Avg avg = (Avg)aggreg;
				System.out.println(avg.getValue());
				if (avg.getValue() <= 5.0) {
					rating = avg.getValue();
				}
				return Mono.justOrEmpty(rating);
			};
}
