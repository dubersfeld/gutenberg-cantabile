package com.dub.gutenberg.services;

import java.io.IOException;

import com.dub.gutenberg.domain.Book;

import reactor.core.publisher.Flux;

public interface SearchService {
	
	public Flux<Book> searchByDescription(String searchString) throws IOException;
	public Flux<Book> searchByTags(String searchString) throws IOException;
	public Flux<Book> searchByTitle(String searchString) throws IOException;


}
