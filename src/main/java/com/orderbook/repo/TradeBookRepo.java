package com.orderbook.repo;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.orderbook.entity.Trade;

@Repository
public interface TradeBookRepo extends JpaRepository<Trade, Long> {
	

}
