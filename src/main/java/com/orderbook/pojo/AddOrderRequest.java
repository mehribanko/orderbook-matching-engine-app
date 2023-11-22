package com.orderbook.pojo;

import java.math.BigDecimal;
import java.sql.Time;


public class AddOrderRequest {
	
	private BigDecimal size ;
	private BigDecimal price ;
	private Time time ;
	private String side ;
	
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
	public Time getTime() {
		return time;
	}
	public void setTime(Time time) {
		this.time = time;
	}
	public String getSide() {
		return side;
	}
	public void setSide(String side) {
		this.side = side;
	}

	
}
