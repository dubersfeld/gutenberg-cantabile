package com.dub.gutenberg;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.List;
import java.util.function.Predicate;

import org.assertj.core.util.Arrays;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.dub.gutenberg.domain.Book;
import com.dub.gutenberg.domain.BookSearch;
import com.dub.gutenberg.exceptions.BookNotFoundException;
import com.dub.gutenberg.services.BookService;
import com.dub.gutenberg.services.SearchService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@SpringBootTest
public class SearchServiceTests {

	@Autowired
	SearchService searchService;
	
	@Test
	void testSearchByTitle() {
		String searchString = "Apes";
		try {
			Flux<Book> books = searchService.searchByTitle(searchString);
			List<String> bookIds = books.map(b -> b.getId()).collectList().block();
			assertTrue(bookIds.size() == 1 && bookIds.contains("15"));
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
		
	@Test
		void testSearchByTags() {
			String searchString = "medicine";
			try {
				Flux<Book> books = searchService.searchByTags(searchString);
				List<String> bookIds = books.map(b -> b.getId()).collectList().block();				
				assertTrue(bookIds.size() == 1 && bookIds.contains("10"));
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
		
	@Test
	void testSearchByDescription() {
		String searchString = "quantum";
		try {
					Flux<Book> books = searchService.searchByDescription(searchString);
					List<String> bookIds = books.map(b -> b.getId()).collectList().block();
					assertTrue(bookIds.size() == 2 && bookIds.containsAll(Arrays.asList(new String[] {"14", "7"})));
						
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			
		
		
	}	
	
}
