package com.dub.gutenberg.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import com.dub.gutenberg.domain.Book;
import reactor.core.publisher.Flux;


public interface BookRepository extends ReactiveCrudRepository<Book, String> {

	public Flux<Book> findBySlug(String slug);
	
}
