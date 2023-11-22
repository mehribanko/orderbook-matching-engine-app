package com.orderbook.entity;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="trades_book")
public class Trade {
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	@Column(name="trade_id")
	private Long tradeID;
	
	private Date takerOrderID;
	
	private Date makerOrderID;
	
	private BigDecimal size;
	
	private BigDecimal price;
	
	
	public Trade() {
		
	}


	public Trade(Date takerOrderID, Date makerOrderIDString, BigDecimal size, BigDecimal price) {
		super();
		this.takerOrderID = takerOrderID;
		this.makerOrderID = makerOrderIDString;
		this.size = size;
		this.price = price;
	}


	public Long getTradeID() {
		return tradeID;
	}


	public void setTradeID(Long tradeID) {
		this.tradeID = tradeID;
	}


	public Date getTakerOrderID() {
		return takerOrderID;
	}


	public void setTakerOrderID(Date takerOrderID) {
		this.takerOrderID = takerOrderID;
	}


	public Date getMakerOrderIDString() {
		return makerOrderID;
	}


	public void setMakerOrderIDString(Date makerOrderIDString) {
		this.makerOrderID = makerOrderIDString;
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
	

	

}
