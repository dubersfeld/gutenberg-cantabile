package com.dub.gutenberg.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import com.dub.gutenberg.domain.Book;

public interface BookRepository extends ReactiveCrudRepository<Book, String> {

}
