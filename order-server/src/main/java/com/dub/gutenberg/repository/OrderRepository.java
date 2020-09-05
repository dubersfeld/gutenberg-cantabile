package com.dub.gutenberg.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import com.dub.gutenberg.domain.Order;

public interface OrderRepository extends ReactiveCrudRepository<Order, String> {

}
