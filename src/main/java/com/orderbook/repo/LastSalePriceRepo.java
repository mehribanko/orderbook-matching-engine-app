package com.orderbook.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.orderbook.entity.LastSalePrice;


public interface LastSalePriceRepo extends JpaRepository<LastSalePrice, Long> {
	
	

}
