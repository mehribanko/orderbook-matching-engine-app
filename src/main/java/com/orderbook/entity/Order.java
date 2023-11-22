package com.orderbook.entity;

import java.math.BigDecimal;
import java.sql.Time;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.orderbook.enumerator.*;

@Entity
@Table(name="order_book")
public class Order {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="order_id")
	private Long orderID;
	
	private Date unique_order;
	
	private Time time;
	
	private BigDecimal size;
	
	private BigDecimal price;
	
	@Enumerated(EnumType.STRING)
	@Column(name="side")
	private Action side = Action.BUY;
	
	@Column(name="is_active")
	private Boolean isActive = Boolean.TRUE;

	
	public Order() {

	}
	
	public Order(Date unique_order, Time time, BigDecimal size, BigDecimal price, Action side, Boolean isActive) {
		super();
		this.unique_order = unique_order;
		this.time = time;
		this.size = size;
		this.price = price;
		this.side = side;
		this.isActive = isActive;
	}



	public void setOrderID(Long orderID) {
		this.orderID = orderID;
	}


	public Date getUnique_order() {
		return unique_order;
	}


	public void setUnique_order(Date unique_order) {
		this.unique_order = unique_order;
	}

	public Time getTime() {
		return time;
	}


	public void setTime(Time time) {
		this.time = time;
	}


	public BigDecimal getSize() {
		return size;
	}


	public void setSize(BigDecimal size) {
		this.size = size;
	}


	public BigDecimal getPrice() {
		return price;
	}



	public void setPrice(BigDecimal price) {
		this.price = price;
	}


	public Action getSide() {
		return side;
	}


	public void setSide(Action side) {
		this.side = side;
	}


	public Boolean getIsActive() {
		return isActive;
	}


	public void setIsActive(Boolean isActive) {
		this.isActive = isActive;
	}


	public Long getOrderID() {
		
		return orderID;
	}


}
