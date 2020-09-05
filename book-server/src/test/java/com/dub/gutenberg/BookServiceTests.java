package com.dub.gutenberg;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.dub.gutenberg.domain.Book;
import com.dub.gutenberg.domain.BookSearch;
import com.dub.gutenberg.exceptions.BookNotFoundException;
import com.dub.gutenberg.services.BookService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@SpringBootTest
public class BookServiceTests {


	@Autowired
	BookService bookService;
	
					
	private Predicate<Book> testById = 
			book -> "emerald-ultimate-421".equals(book.getSlug());
			
	private Predicate<Book> testBySlug = 
			book -> "3".equals(book.getId());		
			
	
	
	@Test
	void testBookBySlug() {	     
		Mono<Book> book = bookService.getBookBySlug("emerald-ultimate-421");
		
		StepVerifier.create(book.log())
					.expectNextMatches(testBySlug)
					.verifyComplete();
	}
	
	
	@Test
	void testBookBySlugFail() {	     
		Mono<Book> book = bookService.getBookBySlug("emerold-ultimate-421");
		
		StepVerifier.create(book.log())
					.expectError(BookNotFoundException.class)
					.verify();
	}
	
	
	@Test
	void testBookById() {	     
		Mono<Book> book = bookService.getBookById("3");
		
		StepVerifier.create(book.log())
					.expectNextMatches(testById)
					.verifyComplete();
	}
	
	
	@Test
	void testBookByIdFail() {	     
		Mono<Book> book = bookService.getBookById("666");
		
		StepVerifier.create(book.log())
					.expectError(BookNotFoundException.class)
					.verify();
	}
		
		
	@Test
	void testAllBooksByCategory() {
	     Flux<Book> books = bookService.allBooksByCategory("fiction", "ASC");
	         
	     StepVerifier.create(books.log())
	     			.expectNextCount(5)
	     			.verifyComplete();

	}
	

	@Test
	void testBooksBoughtWith() {
		Flux<Book> books = bookService.getBooksBoughtWith("2", 10);
		Flux<String> bookIds = books.map(b -> b.getId());
			
		List<String> grunge = bookIds.collectList().block();
		
		System.err.println("grunge " + grunge);
		
		assertTrue(grunge.contains("5") && grunge.contains("10"));
	
	}
	

	@Test
	void testBooksBoughtWithNull() {
		Flux<Book> books = bookService.getBooksBoughtWith("12", 10);//.getCategory("fiction");
	
		StepVerifier.create(books.log())
					.verifyComplete();
	}	
		
}
