package com.dub.gutenberg.domain;

import java.util.List;

// wrapper class
public class CategoryWebList {

	List<Category> categories;
	
	// shallow copy
	public CategoryWebList(List<Category> categories) {
			this.categories = categories;			
	}
		
	public CategoryWebList() { }

	public List<Category> getCategories() {
		return categories;
	}

	public void setCategories(List<Category> categories) {
		this.categories = categories;
	}
	

	
}
