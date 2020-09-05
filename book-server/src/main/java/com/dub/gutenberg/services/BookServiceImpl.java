package com.dub.gutenberg.services;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.client.reactive.ReactiveElasticsearchClient;
import org.springframework.stereotype.Service;

import com.dub.gutenberg.domain.Book;
import com.dub.gutenberg.domain.Category;
import com.dub.gutenberg.domain.Item;
import com.dub.gutenberg.exceptions.BookNotFoundException;
import com.dub.gutenberg.repository.BookRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;



@Service
public class BookServiceImpl implements BookService {

private static ObjectMapper objectMapper = new ObjectMapper();
	
	public static final String BOOKS = "gutenberg-books";
	public static final String ORDERS = "gutenberg-orders";
	public static final String TYPE = "_doc";
	
	@Autowired
	private ReactiveElasticsearchClient reactiveClient;
	
	@Autowired
	private CategoryService categoryService;
	
	@Autowired
	private BookRepository bookRepository;
	
	@Override
	public Mono<Book> getBookBySlug(String slug) {

		System.err.println("SATOR");
		Flux<Book> flux = bookRepository.findBySlug(slug);
		System.err.println("AREPO");
		
		Mono<Book> book = flux.next();
		
		Mono<Boolean> hasElement = book.hasElement();
		
		return hasElement.flatMap(
				b -> {
					if (b) {
						return book;
					} else {
			
						return Mono.error(new BookNotFoundException());			
		}});
	}
	
	@Override
	public Mono<Book> getBookById(String bookId) {
		
		Mono<Book> book = bookRepository.findById(bookId);
		Mono<Boolean> hasElement = book.hasElement();
		return hasElement.flatMap(
				b -> {
					if (b) {
						return book;
					} else {
						
						return Mono.error(new BookNotFoundException());			
		}});
	}


	@Override
	public Flux<Book> allBooksByCategory(String categorySlug, String sortBy) {
							
		Mono<Category> cat = categoryService.getCategory(categorySlug);
		
		return cat.flatMapMany(categoryTransform);
		
	}
	
	public Flux<Book> getBooksBoughtWith(String bookId, int outLimit) {
		
		Mono<Book> doc = this.getBookById(bookId);
		// doc should never be null 
		
		return doc.flatMapMany(transformBoughtWith);
		
	}
	
	
	private Function<SearchHit, Book> mapper =
			hit -> {
				Map<String, Object> sourceAsMap = hit.getSourceAsMap();
				
				Book book = objectMapper.convertValue(sourceAsMap, Book.class);
				book.setId(hit.getId());
				return book;
	};
	
	private Function<Item, Mono<Book>> itemTransform =
			item -> {	
				return this.getBookById(item.getBookId());
	};
	
	/** Note that in this implementation the argument sortBy is dismissed */
	private Function<Category, Flux<Book>> categoryTransform = 
			cat -> {
							
				SearchRequest searchRequest = new SearchRequest();
				SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder(); 
					
				searchSourceBuilder.query(QueryBuilders.termQuery("categoryId.keyword", cat.getId()));
				
				searchSourceBuilder.sort(new FieldSortBuilder("title.keyword").order(SortOrder.ASC));
				searchRequest.source(searchSourceBuilder); 
				searchRequest.indices(BOOKS);
									
				Flux<SearchHit> hits 
							= reactiveClient.search(searchRequest);
				
				//Mono<Boolean> grunge = hits.hasElements();
				
				//grunge.subscribe(System.out::println); 
							
				return hits.map(mapper);				
	};

	private Function<Book, Flux<Book>> transformBoughtWith =
			book -> {			
				List<Item> items = book.getBoughtWith();
	
				// sort items by decreasing quantity
				Collections.sort(items, new Comparator<Item>() {
		
					@Override
					public int compare(Item o1, Item o2) {
						if (o1.getQuantity() > o2.getQuantity()) {
							return -1;
						} else if (
					o1.getQuantity() < o2.getQuantity()) {
							return 1;
						} else {
							return 0;
						}
					}
	});
	
	return Flux.fromIterable(items)
				.flatMap(itemTransform);
	}; 

}