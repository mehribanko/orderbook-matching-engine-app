package com.orderbook.repo;


import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.orderbook.entity.Order;
import com.orderbook.enumerator.Action;

import java.math.BigDecimal;
import java.util.List;

import javax.transaction.Transactional;

@Repository
public interface OrderBookRepo extends CrudRepository<Order, Long> {
	
	 @Query("Select ord from Order ord where ord.side = :side and ord.isActive = true order by ord.price asc")
	 List<Order> getAllSellOrdersSortedByPriceAndActive(@Param("side") Action side);

	 @Query("Select ord from Order ord where ord.side = :side and ord.isActive = true order by ord.price desc")
	 List<Order> getAllBuyOrdersSortedByPriceAndActive(@Param("side") Action side);

	 List<Order> findByIsActiveAndSide(Boolean isActive, Action side);
	 
	 @Query("Select distinct price from Order ord where ord.side= :side and ord.isActive = :isActive order by ord.price desc")
	 List<BigDecimal> getAllOrdersWithUniquePrice(@Param("side") Action side, @Param("isActive") Boolean isActive);
	 
	 @Query("Select count(price) from Order ord where ord.price= :price AND ord.side= :side AND ord.isActive= :isActive")
	 Integer getCountOfOrderPrices(@Param("price") BigDecimal price, @Param("side") Action side, @Param("isActive") Boolean isActive);
	 
	 @Query("Select ord from Order ord where ord.price= :price AND ord.side= :side AND ord.isActive= :isActive")
	 List<Order> getMultiPriceOrder(@Param("price") BigDecimal price, @Param("side") Action side, @Param("isActive") Boolean isActive);
	 
	 @Query("Select sum(size) from Order ord where ord.price= :price AND ord.side= :side AND ord.isActive= :isActive")
	 BigDecimal getAllOrderSize(@Param("price") BigDecimal price, @Param("side") Action side, @Param("isActive") Boolean isActive);

	@Transactional
	@Modifying
	@Query("Update Order o set o.size= :size, o.isActive= :isActive where o.orderID= :orderID ")
	void updateOrder(@Param(value="orderID") long id, @Param(value="size") BigDecimal size, @Param(value="isActive") Boolean isActive);
	
}





