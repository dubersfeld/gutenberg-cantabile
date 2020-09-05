package com.dub.gutenberg.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dub.gutenberg.domain.Category;
import com.dub.gutenberg.exceptions.CategoryNotFoundException;
import com.dub.gutenberg.repository.CategoryRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@Service
public class CategoryServiceImpl implements CategoryService {
	
	@Autowired
	private CategoryRepository categoryRepository;
	

	@Override
	public Mono<Category> getCategory(String categorySlug) {

		Flux<Category> flux = categoryRepository.findBySlug(categorySlug);
		
		Mono<Category> category = flux.next();
		
		/**
		 * Here hasElement() should always return true
		 * What I want: according to enclume return Mono.error(SomeException)
		 * */
		Mono<Boolean> hasElement = category.hasElement();
		
		return hasElement.flatMap(
				b -> {
					if (b) {
						return category;
					} else {
						return Mono.error(new CategoryNotFoundException());			
		}});
		
		
	}


	@Override
	public Flux<Category> findAllCategories() {
	
		return categoryRepository.findAll();
	}
	
}