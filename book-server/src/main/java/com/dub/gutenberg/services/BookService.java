package com.dub.gutenberg.services;

import com.dub.gutenberg.domain.Book;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface BookService {
		
	Mono<Book> getBookBySlug(String slug);
	
	Mono<Book> getBookById(String bookId);
		
	Flux<Book> allBooksByCategory(String categorySlug, String sortBy);
	
	Flux<Book> getBooksBoughtWith(String bookId, int limit);

}