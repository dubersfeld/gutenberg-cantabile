package com.dub.gutenberg;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.dub.gutenberg.domain.Book;
import com.dub.gutenberg.domain.BookSearch;
import com.dub.gutenberg.web.BookHandler;
import com.dub.gutenberg.web.BookRouter;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@SpringBootTest
public class BookHandlerTests {
	
	@Autowired
	BookRouter bookRouter;
	
	@Autowired
	BookHandler bookHandler;
	
	
	@Test
	void testBookBySlug() {

		WebTestClient
		.bindToRouterFunction(bookRouter.route(bookHandler))		
		.build()
		.method(HttpMethod.GET)
		.uri("/books/emerald-ultimate-421")
		.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
		.exchange()
		.expectStatus().isOk()
		.expectBody()
		.jsonPath("$.id").isEqualTo("3")
		.jsonPath("$.title").isEqualTo("The Ultimate Emerald Reference");
	}
	
	@Test
	void testBookBySlugFail() {

		WebTestClient
		.bindToRouterFunction(bookRouter.route(bookHandler))		
		.build()
		.method(HttpMethod.GET)
		.uri("/books/xxxtttyyyuuu")
		.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
		.exchange()
		.expectStatus().isNotFound();
	}
	
	
	@Test
	void testAllBooksByCategory() {
		
		WebTestClient
		.bindToRouterFunction(bookRouter.route(bookHandler))
		.build()
		.method(HttpMethod.GET)
		.uri("/books/fiction/sort/ASC")
		.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
		.exchange()
		.expectStatus().isOk()
		.returnResult(Book.class)
		.getResponseBody()// it is a Flux<Book>
		.as(StepVerifier::create)
		.expectNextCount(5)
		.expectComplete()
		.verify();	
	}	
	
	@Test
	void testBookById() {
		WebTestClient
		.bindToRouterFunction(bookRouter.route(bookHandler))		
		.build()
		.method(HttpMethod.GET)
		.uri("/bookById/3")
		.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
		.exchange()
		.expectStatus().isOk()
		.expectBody()
		.jsonPath("$.slug").isEqualTo("emerald-ultimate-421")
		.jsonPath("$.title").isEqualTo("The Ultimate Emerald Reference");
	}
	
	@Test
	void testBookByIdFail() {
		WebTestClient
		.bindToRouterFunction(bookRouter.route(bookHandler))		
		.build()
		.method(HttpMethod.GET)
		.uri("/bookById/666")
		.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
		.exchange()
		.expectStatus().isNotFound();
	}
	
	@Test
	void testBooksBoughtWith() {
		
		WebTestClient
		.bindToRouterFunction(bookRouter.route(bookHandler))
		.build()
		.method(HttpMethod.GET)
		.uri("/booksBoughtWith/2/outLimit/10")
		.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
		.exchange()
		.expectStatus().isOk()
		.returnResult(Book.class)
		.getResponseBody()// it is a Flux<Book>
		.as(StepVerifier::create)
		.expectNextCount(2)
		.expectComplete()
		.verify();	
	}	
	
	
	@Test
	void searchByTitle() {
		BookSearch bookSearch = new BookSearch();
		bookSearch.setSearchString("Wrath");
		WebTestClient
		.bindToRouterFunction(bookRouter.route(bookHandler))
		.build()
		.method(HttpMethod.POST)
		.uri("/searchByTitle")
		.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
		.body(Mono.just(bookSearch), BookSearch.class)
		.exchange()
		.expectStatus().isOk()
		.returnResult(Book.class)
		.getResponseBody()// it is a Flux<Book>
		.as(StepVerifier::create)
		.expectNextCount(1)
		.expectComplete()
		.verify();	
	}	
	
	
	@Test
	void searchByDescription() {
		BookSearch bookSearch = new BookSearch();
		bookSearch.setSearchString("quantum");
		WebTestClient
		.bindToRouterFunction(bookRouter.route(bookHandler))
		.build()
		.method(HttpMethod.POST)
		.uri("/searchByDescription")
		.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
		.body(Mono.just(bookSearch), BookSearch.class)
		.exchange()
		.expectStatus().isOk()
		.returnResult(Book.class)
		.getResponseBody()// it is a Flux<Book>
		.as(StepVerifier::create)
		.expectNextCount(2)
		.expectComplete()
		.verify();	
	}	
	
	
	@Test
	void searchByTags() {
		BookSearch bookSearch = new BookSearch();
		bookSearch.setSearchString("system");
		WebTestClient
		.bindToRouterFunction(bookRouter.route(bookHandler))
		.build()
		.method(HttpMethod.POST)
		.uri("/searchByTags")
		.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
		.body(Mono.just(bookSearch), BookSearch.class)
		.exchange()
		.expectStatus().isOk()
		.returnResult(Book.class)
		.getResponseBody()// it is a Flux<Book>
		.as(StepVerifier::create)
		.expectNextCount(2)
		.expectComplete()
		.verify();	
	}	

}
