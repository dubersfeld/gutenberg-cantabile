package com.dub.gutenberg.services;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import javax.annotation.PostConstruct;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.client.reactive.ReactiveElasticsearchClient;
import org.springframework.stereotype.Service;

import com.dub.gutenberg.domain.Book;
import com.dub.gutenberg.domain.EditCart;
import com.dub.gutenberg.domain.Item;
import com.dub.gutenberg.domain.Order;
import com.dub.gutenberg.domain.OrderState;
import com.dub.gutenberg.domain.UserAndReviewedBooks;
import com.dub.gutenberg.exceptions.OrderException;
import com.dub.gutenberg.exceptions.OrderNotFoundException;
import com.dub.gutenberg.repository.BookRepository;
import com.dub.gutenberg.repository.OrderRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import com.dub.gutenberg.exceptions.*;

/** To be continued */

@Service
public class OrderServiceImpl implements OrderService {

	@Autowired
	private ObjectMapper objectMapper;

	public static final String BOOKS = "gutenberg-books";
	public static final String ORDERS = "gutenberg-orders";
	public static final String TYPE = "_doc";
	public static final String RECENT = "2013-01-01T00:00:00.000";
	public static final String DATE_FORMAT = "uuuu-MM-dd'T'HH:mm:ss.SSS";
	public static final String SHIPPED = "SHIPPED";
	public static final String PRE_SHIPPING = "PRE_SHIPPING";
	
	@Autowired
	private OrderRepository orderRepository;
	
	@Autowired
	private BookRepository bookRepository;
	
	/** 
	 * This DateFormat is supported by Elasticsearch 
	 * but not by default Spring message converters 
	 * */
	DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	
	@Autowired
	private RestHighLevelClient client;
	
	@Autowired
	private ReactiveElasticsearchClient reactiveClient;
		
	//@Autowired
	//private BookRepositoryOld bookRepo;
	
	@PostConstruct
	public void init() {
		objectMapper.setDateFormat(sdf);
	}
	
	
	@Override
	public Flux<String> getBooksNotReviewed(
			UserAndReviewedBooks userAndReviewedBooks) throws ParseException, IOException {
		
		
		String userId = userAndReviewedBooks.getUserId();
				
		// find all recent orders for userIds that have already been shipped
		SearchRequest searchRequest = new SearchRequest();
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder(); 
		
		RangeQueryBuilder date 
				= QueryBuilders
							.rangeQuery("date")
							.gte(RECENT)
							.format(DATE_FORMAT);
		
		searchSourceBuilder.query(
				QueryBuilders.boolQuery()
				.must(QueryBuilders.termQuery("userId.keyword", userId))
				.must(QueryBuilders.termQuery("state.keyword", SHIPPED))
				.must(date)		
		);
		
		searchRequest.source(searchSourceBuilder); 
		searchRequest.indices(ORDERS);
			
		
		
		Flux<SearchHit> response 
			= reactiveClient.search(searchRequest);
	
		//reactiveClient.g
		
		
		Flux<Order> orders = response.map(mapper);
	
		
		
		Flux<Item> items = orders.concatMap(order -> Flux.fromIterable(order.getLineItems()));
		Flux<String> purchasedBooks = items.map(item -> item.getBookId());
		
		Flux<String> booksNotReviewed = purchasedBooks
				.filter(bookId -> !userAndReviewedBooks.getReviewedBookIds().contains(bookId))
				.distinct();
		
		
		return booksNotReviewed;
	}
	
	@Override
	public Mono<Order> saveOrder(Order order, boolean creation) {
		
		if (creation) {
			// create a new Order
			
			return this.orderRepository.save(order);
			
		} else {
			//check for presence if not creation
			Mono<Order> checkOrder = this.orderRepository.findById(order.getId());
			
			Mono<Boolean> hasElement = checkOrder.hasElement();
			
			return hasElement.flatMap(
					b-> {// needed here
						if (b) {
							// update Order ES document
							try {
								// updateBoughtWith should be called only if orderState is changed to PRE_SHIPPING
								if (OrderState.PRE_SHIPPING.equals(order.getState())) {
									this.updateBoughtWith(order);
								}
								return this.orderRepository.save(order);
			
							} catch (IOException e) {
								return Mono.error(new RuntimeException());
							}
						} else {
							return Mono.error(new OrderNotFoundException());
						}
			});
	          	 
		}
	}
	
	@Override
	public Mono<Order> getOrderById(String orderId) {	
	
	 	Mono<Order> order = this.orderRepository.findById(orderId);
	 	
		Mono<Boolean> isPresent = order.hasElement();
		
		return isPresent.flatMap(b -> {
			if (b) {
				return order;
			} else {
				
				return Mono.error(new OrderNotFoundException());
			}
		});
		//return this.orderRepository.findById(orderId);
	}

	@Override
	public Mono<Order> getActiveOrder(String userId) throws IOException {
	
		SearchRequest searchRequest = new SearchRequest();
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder(); 
		
		searchSourceBuilder.query(
				QueryBuilders.boolQuery()
					.must(QueryBuilders.termQuery("userId.keyword", userId))
					.mustNot(QueryBuilders.termQuery("state.keyword", PRE_SHIPPING))
					.mustNot(QueryBuilders.termQuery("state.keyword", SHIPPED))
				);
		
		searchRequest.source(searchSourceBuilder); 
		searchRequest.indices(ORDERS);
		
		Flux<SearchHit> response 
				= reactiveClient.search(searchRequest);
		
		Flux<Order> orders = response.map(mapper);
		
		Mono<Order> order = orders.next();
		
		Mono<Boolean> isPresent = order.hasElement();
		
		return isPresent.flatMap(b -> {
			if (b) {
				return order;
			} else {
				return Mono.error(new OrderNotFoundException());
			}
		});
	}
	
	@Override
	public Mono<Order> addBookToOrder(String orderId, String bookId) throws IOException {
			
	
		
		Mono<Order> oldOrder = this.orderRepository.findById(orderId);
		
	
		
		Mono<Order> upOrder = oldOrder.map(ord -> {
			List<Item> items = ord.getLineItems();
			
			
			// check if bookId already present
			boolean present = false;
			
			for (Item item : items) {
				if (item.getBookId().equals(bookId)) {
					present = true;
					item.setQuantity(item.getQuantity()+1);
				}
			}
			
			if (!present)  {
				
				// add a new Item
				items.add(new Item(bookId, 1));
			}
			
			ord.setLineItems(items);
			
			
			return ord;
		});
		
		// then recalculate
		Mono<Order> upOrder2 = recalculateTotalAlt2(upOrder);
			
		// finally save order
		return this.orderRepository.saveAll(upOrder2).next();
	}
	
		
	@Override
	public Mono<Order> editCart(EditCart editCart) throws IOException {
		
		
		
		List<Item> items = editCart.getItems();
		
		String orderId = editCart.getOrderId();
				
		
			
		Mono<Order> order = this.orderRepository.findById(orderId);
		
		Mono<Order> upOrder = order.map(ord -> {
			ord.setLineItems(items);
			return ord; 
		});
		
		Mono<Order> upOrder2 = this.recalculateTotalAlt2(upOrder); 
		
		return orderRepository.saveAll(upOrder2).next();
			
	}
	
	@Override
	public Mono<Order> setOrderState(String orderId, OrderState state) throws IOException {
		
		Mono<Order> oldOrder = orderRepository.findById(orderId);
		
		Mono<Order> upOrder = oldOrder.flatMap(ord -> {
			OrderState oldState = ord.getState();
			// legal transitions only
			switch (state) {
			case CART:
				if (oldState.equals(OrderState.SHIPPED) ||
						oldState.equals(OrderState.PRE_SHIPPING)) {
					return Mono.error(new OrderException());
				}
				break;
			case PRE_AUTHORIZE:
				if (oldState.equals(OrderState.SHIPPED) ||
						oldState.equals(OrderState.PRE_SHIPPING)) {
					return Mono.error(new OrderException());
				}
				break;
			case PRE_SHIPPING:
				if (oldState.equals(OrderState.SHIPPED)) {
					return Mono.error(new OrderException());
				}
				break;
			default:
				// should not be here
				return Mono.error(new OrderException());
			}
			
			ord.setState(state);
			
			return Mono.just(ord); 
		});
		
		return this.orderRepository.saveAll(upOrder).next();
			
	}
		
	@Override
	public Mono<Order> checkoutOrder(String orderId) throws IOException {
		
		// retrieve order by Id
		Mono<Order> order = orderRepository.findById(orderId);
		
		Mono<Order> upOrder = order.map(ord -> {
			ord.setState(OrderState.PRE_AUTHORIZE);
			return ord;
		});
		
		return this.orderRepository.saveAll(upOrder).next();
	}
	
	/** Rewrite more compact before next release */
	private Mono<Order> recalculateTotalAlt2(Mono<Order> order) {
		
		Mono<Integer> total = order.flatMap(ord -> {
			Flux<Item> items = Flux.fromIterable(ord.getLineItems());
			
			Flux<String> bookIds = items.map(it -> {
				
				return it.getBookId();
			}); 
			
			Flux<Book> books = bookRepository.findAllById(bookIds);
			 
			Flux<Integer> prices = books.map(book -> {
				return book.getPrice();});

			Flux<Integer> quantities = items.map(it -> {
				return it.getQuantity();});

			Flux<Tuple2<Integer, Integer>> grunges = Flux.zip(prices, quantities);

			Flux<Integer> groubles = grunges.map(gr -> gr.getT1() * gr.getT2());
					
			return groubles.reduce(0, (x1, x2) -> x1 + x2);
			
		});
		
		Mono<Tuple2<Order,Integer>> fourbi = Mono.zip(order, total);
		
		return fourbi.map(t -> {
			Order ord = t.getT1();
			ord.setSubtotal(t.getT2());
			
			
			return ord;
		});		
	}
	

	/** 
	 * This method is called to update all boughtWith fields
	 * in gutenberg-books index
	 * @throws IOException 
	 */
	private void updateBoughtWith(Order order) throws IOException {
		
		/**
		 * An order may have multiple bookIds
		 * */
		
		
		Set<String> presentOrderBookIds = new HashSet<>();
		List<Item> items = order.getLineItems();
		
		for (Item item : items) {
			presentOrderBookIds.add(item.getBookId());
		}
		
		
			
		/** 
		 * Step 1: retrieve all past orders from this user 
		 * that have already been placed (state PRE_SHIPPING or SHIPPED)
		 * */
		String userId = order.getUserId();
		
		// use shouldQuery to implement a Boolean OR
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		searchSourceBuilder.query(
				QueryBuilders.boolQuery()
				.must(QueryBuilders.termQuery("userId.keyword", userId))
				.should(QueryBuilders.termQuery("state.keyword", PRE_SHIPPING))
				.should(QueryBuilders.termQuery("state.keyword", SHIPPED))
				.minimumShouldMatch(1));// required
		
		SearchRequest searchRequest = new SearchRequest();	
		searchRequest.source(searchSourceBuilder); 
		searchRequest.indices(ORDERS);
			
		// using ReactiveElasticsearchClient
		Flux<SearchHit> grunges = reactiveClient.search(searchRequest);
		
		Flux<Order> orders = grunges.map(mapper);
		
		// iterate on past orders 
		Flux<Set<String>> groubles = orders.map(ord -> {
			
			
		
			List<Item> pastItems = ord.getLineItems();
			
			
			Set<String> pastBookIds = new HashSet<>();
			
			if (!ord.getId().equals(order.getId())) {	
				System.out.println("PLATYPUS " + order.getId());
				for (Item item : pastItems) {
					// add unique
					System.out.println("add unique " 
								+ item.getBookId());
					pastBookIds.add(item.getBookId());
				}
			}
			
			return pastBookIds;
		});
		
		// extract all past books as a Flux
		Mono<Set<String>> pastBookIds = groubles.reduce(new HashSet<String>(), (x1, x2) -> {x1.addAll(x2); return x1;});
		
		Flux<Book> pastBooks = pastBookIds.flatMapMany(ids -> {
			Flux<Book> bks = this.bookRepository.findAllById(ids);
			return bks;
		});
		
		// extract all present books as a Flux
		Flux<Book> presentBooks = this.bookRepository.findAllById(presentOrderBookIds);
		
		
		/** 
		 * Step 2. Update the boughtWith field in each Book in the pastBookIds set
		 * 
		 * */
		
		Flux<Book> pastBooks2 = pastBooks.map(b -> {
			
			List<Item> pastItems = b.getBoughtWith();
			
			for (String presentBookId : presentOrderBookIds) {
				// check if bookId already present
				boolean present = false;
				if (pastItems != null) {
					for (Item pastItem : pastItems) {		
						if (pastItem.getBookId().equals(presentBookId)) {
							present = true;
							pastItem.setQuantity(pastItem.getQuantity()+1);
						}
					}
				}
				if (!present)  {
					// first check if pastItems exists
					if (pastItems == null) {
						pastItems = new ArrayList<>();
					}
					// add a new Item
					pastItems.add(new Item(presentBookId, 1));
				}		
			}
			
			System.out.println("ANDOUILLE pastItems " + pastItems
					+ " " + b.getId());
			
			b.setBoughtWith(pastItems);
		
			return b;
		});
			
		// then save the updated Flux
		this.bookRepository.saveAll(pastBooks2).subscribe();
		
		Mono<Tuple2<Flux<Book>, Set<String>>> enclume = Mono.zip(Mono.just(presentBooks), pastBookIds);
		
		enclume.subscribe(t -> {
			
			System.out.println(t.getT2());
			
			Flux<Book> presentBooks2 = t.getT1().map(b -> {
				List<Item> presentItems = b.getBoughtWith();
				for (String pastBookId : t.getT2()) {
					// check if bookId already present
					boolean present = false;
					if (presentItems != null) {
						for (Item presentItem : presentItems) {		
							if (presentItem.getBookId().equals(pastBookId)) {
								present = true;
								presentItem.setQuantity(presentItem.getQuantity()+1);
							}
						}
					}
					if (!present)  {
						// first check if pastItems exists
						if (presentItems == null) {
							presentItems = new ArrayList<>();
						}
						// add a new Item
						presentItems.add(new Item(pastBookId, 1));
					}	
				}// for
				b.setBoughtWith(presentItems);
				return b;
			});
			
			// then save the updated Flux
			this.bookRepository.saveAll(presentBooks2).subscribe();	
		});
		
	}
		
	private Function<SearchHit, Order> mapper =
			hit -> {
				Map<String, Object> sourceAsMap = hit.getSourceAsMap();
		
				Order order = objectMapper.convertValue(sourceAsMap, Order.class);
				order.setId(hit.getId());
				return order;
	};	
}
