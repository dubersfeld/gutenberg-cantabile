package com.dub.gutenberg.services;

import com.dub.gutenberg.domain.Category;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


public interface CategoryService {

	public Flux<Category> findAllCategories();	
	
	public Mono<Category> getCategory(String categorySlug);
}
