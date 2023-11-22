package com.orderbook.entity;

import java.math.BigDecimal;
import java.sql.Time;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;


@Entity
@Table(name="last_saleprice")
public class LastSalePrice {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="sale_id")
	private Long sale_Id;
	
    private Long trade_id;
	
	private Date time;
	
	private BigDecimal sale_price;

	public LastSalePrice(Long trade_id, Date time, BigDecimal sale_price) {
		super();
		this.trade_id = trade_id;
		this.time = time;
		this.sale_price = sale_price;
	}

	public LastSalePrice() {
		super();
	}

	public Long getSale_Id() {
		return sale_Id;
	}

	public void setSale_Id(Long sale_Id) {
		this.sale_Id = sale_Id;
	}

	public Long getTrade_id() {
		return trade_id;
	}

	public void setTrade_id(Long trade_id) {
		this.trade_id = trade_id;
	}

	public Date getTime() {
		return time;
	}

	public void setTime(Time time) {
		this.time = time;
	}

	public BigDecimal getSale_price() {
		return sale_price;
	}

	public void setSale_price(BigDecimal sale_price) {
		this.sale_price = sale_price;
	}
	



}
