package com.dub.gutenberg.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import com.dub.gutenberg.domain.Category;
import reactor.core.publisher.Flux;


public interface CategoryRepository extends ReactiveCrudRepository<Category, String> {

	public Flux<Category> findBySlug(String slug);
	
}
