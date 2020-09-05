package com.dub.gutenberg.domain;

import java.util.List;

// wrapper class
public class BookWebList {

	List<Book> books;
	
	public BookWebList() { }
	
	public BookWebList(List<Book> books) {
		this.books = books;
	}

	public List<Book> getBooks() {
		return books;
	}

	public void setBooks(List<Book> books) {
		this.books = books;
	}
	
	
	
}
