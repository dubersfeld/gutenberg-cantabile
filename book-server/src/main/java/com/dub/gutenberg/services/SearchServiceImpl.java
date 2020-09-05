package com.dub.gutenberg.services;

import java.io.IOException;
import java.util.Map;
import java.util.function.Function;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.client.reactive.ReactiveElasticsearchClient;
import org.springframework.stereotype.Service;

import com.dub.gutenberg.domain.Book;
import com.fasterxml.jackson.databind.ObjectMapper;

import reactor.core.publisher.Flux;


@Service
public class SearchServiceImpl implements SearchService {

	@Autowired private ObjectMapper objectMapper;// provided
	
	public static final String BOOKS = "gutenberg-books";
	public static final String ORDERS = "gutenberg-orders";
	public static final String TYPE = "_doc";
	
	
	@Autowired
	private ReactiveElasticsearchClient reactiveClient;
	
	@Override
	public Flux<Book> searchByTitle(String searchString) throws IOException {
	
		Flux<SearchHit> response =
				myReactiveResponseBuilder("title", searchString);	
			
		return doGetReactiveResponse(response);		
	}
	

	@Override
	public Flux<Book> searchByTags(String searchString) throws IOException {

		Flux<SearchHit> response 
		= myReactiveResponseBuilder("tags", searchString);
		
		return doGetReactiveResponse(response);	
	}
	
	
	@Override
	public Flux<Book> searchByDescription(String searchString) throws IOException {

		Flux<SearchHit> response 
		= myReactiveResponseBuilder("description", searchString);
		
		return doGetReactiveResponse(response);	
	}
	

	private Flux<Book> doGetReactiveResponse(Flux<SearchHit> response) {
		
		return response.map(hitMapper);
	}
	
	private Flux<SearchHit> myReactiveResponseBuilder(String field, String searchString) throws IOException {
		
		SearchRequest searchRequest = new SearchRequest();
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder(); 
			
		searchSourceBuilder.query(
				QueryBuilders.matchQuery(field, searchString));
		searchRequest.source(searchSourceBuilder); 
		searchRequest.indices(BOOKS);
		
		Flux<SearchHit> response 
			= reactiveClient.search(searchRequest);
		
		return response;
	}
	
	
	private Function<SearchHit, Book> hitMapper =
			hit -> {
				Map<String, Object> sourceAsMap = hit.getSourceAsMap();
				
				Book book = objectMapper.convertValue(sourceAsMap, Book.class);
				book.setId(hit.getId());
				return book;
	};
	
}
