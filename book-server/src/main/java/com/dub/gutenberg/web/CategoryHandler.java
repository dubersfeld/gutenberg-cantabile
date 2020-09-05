package com.dub.gutenberg.web;

import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.dub.gutenberg.domain.Category;
import com.dub.gutenberg.exceptions.CategoryNotFoundException;
import com.dub.gutenberg.services.CategoryService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class CategoryHandler {

	@Autowired
	private CategoryService categoryService;
	
	
	public Mono<ServerResponse> allCategories(ServerRequest request) {
		
		Mono<Flux<Category>> cats = getAllCategories();
		
		return cats
				.flatMap(searchSuccess)
				.onErrorResume(searchFallback);
	}
	
	public Mono<ServerResponse> categoryBySlug(ServerRequest request) {
		
		Mono<String> slug = Mono.just(request.pathVariable("slug"));
		
		return slug
				.flatMap(transformBySlug)
				.flatMap(categorySuccess)
				.onErrorResume(categoryFallback);
	}
	
	private Mono<Flux<Category>> getAllCategories() {
		try {
			return Mono.just(categoryService.findAllCategories());	
		} catch (Exception e) {
			// catch exception and return custom exception wrapped in a Mono
			return Mono.error(new RuntimeException("SATOR"));
		}
	}
	
	Function<String, Mono<Category>> transformBySlug = 
			slug -> {
				try {	
					Mono<Category> fourbi = categoryService.getCategory(slug);	
					return fourbi;	
				} catch (Exception e) {
					System.err.println("MORBUS GRAVIS " + e);
					throw new RuntimeException("SATOR");
				}
	};
	
	Function<Category, Mono<ServerResponse>> categorySuccess =
			s -> {
					System.out.println("AD LIBITUM");
					return ServerResponse.ok()
								.contentType(MediaType.APPLICATION_JSON)
								.body(Mono.just(s), Category.class);
	};		
	
	Function<Throwable, Mono<ServerResponse>> categoryFallback =
			e -> {
					if (e.getClass() == CategoryNotFoundException.class) {
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
	
	Function<Throwable, Mono<ServerResponse>> searchFallback = 
			s -> {
					return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
							.build();
	};
	
	Function<Flux<Category>, Mono<ServerResponse>> searchSuccess = 
			s -> {
					return ServerResponse.ok()
							.contentType(MediaType.APPLICATION_JSON)
							.body(s, Category.class);
	};
	
}
	
	
