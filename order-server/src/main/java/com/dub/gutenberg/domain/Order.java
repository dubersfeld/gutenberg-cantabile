package com.dub.gutenberg.domain;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;

@Document(indexName = "gutenberg-orders")
public class Order {
	
	@Id
	private String id;
	
	private String userId;
	private OrderState state;
	private List<Item> lineItems;
	private Address shippingAddress;
	private PaymentMethod paymentMethod;
	private int subtotal;
	
	//@Field
	//@JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss")
	
	@Field(type = FieldType.Date, store = true,
			format = DateFormat.custom, pattern = "uuuu-MM-dd'T'HH:mm:ss.SSS")// used for create and read
	//@JsonFormat(shape = JsonFormat.Shape.STRING, pattern ="yyyy-MM-dd'T'HH:mm:ss.SSS")
	
	private LocalDateTime date;
	
	public Order() {
		this.lineItems = new ArrayList<>();
		this.shippingAddress = new Address();
		this.paymentMethod = new PaymentMethod();
	}
	
	public Order(Order that) {
		this.id = that.id;
		this.state = that.state;
		this.lineItems = that.lineItems;
		this.shippingAddress = that.shippingAddress;
		this.paymentMethod = that.paymentMethod;
		this.subtotal = that.subtotal;
	}

	
	public int getSubtotal() {
		return subtotal;
	}

	public void setSubtotal(int subtotal) {
		this.subtotal = subtotal;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setState(OrderState state) {
		this.state = state;
	}

	public OrderState getState() {
		return state;
	}


	public List<Item> getLineItems() {
		return lineItems;
	}


	public void setLineItems(List<Item> lineItems) {
		this.lineItems = lineItems;
	}

	public Address getShippingAddress() {
		return shippingAddress;
	}

	public void setShippingAddress(Address shippingAddress) {
		this.shippingAddress = shippingAddress;
	}

	public PaymentMethod getPaymentMethod() {
		return paymentMethod;
	}

	public void setPaymentMethod(PaymentMethod paymentMethod) {
		this.paymentMethod = paymentMethod;
	}

	public LocalDateTime getDate() {
		return date;
	}

	public void setDate(LocalDateTime date) {
		this.date = date;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}
}
