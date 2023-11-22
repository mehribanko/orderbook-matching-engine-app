package com.orderbook.service;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.criterion.Distinct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.orderbook.entity.LastSalePrice;
import com.orderbook.entity.Order;
import com.orderbook.entity.Trade;
import com.orderbook.enumerator.Action;
import com.orderbook.pojo.AddOrderRequest;
import com.orderbook.repo.LastSalePriceRepo;
import com.orderbook.repo.OrderBookRepo;
import com.orderbook.repo.TradeBookRepo;

import ch.qos.logback.core.rolling.helper.IntegerTokenConverter;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;


@Service
public class OrderBook {
	
	private OrderBookRepo orderBookRepo;
	
	@Autowired
	public OrderBook(OrderBookRepo theOrderBookRepo) {
		orderBookRepo = theOrderBookRepo;		
	}
	
	@Autowired
	private TradeBookRepo tradeBookRepo;
	
	@Autowired
	private LastSalePriceRepo lastSalePriceRepo;
	
	private BigDecimal lastSalePrice; 	
	private  final String BUY = Action.BUY.toString();
	private  final String SELL = Action.SELL.toString();

	
	public Order processOrder(AddOrderRequest addOrderRequest) throws Exception {
		
		Multimap<BigDecimal, Order> mapOfOrders = ArrayListMultimap.create();
		String action = addOrderRequest.getSide();
		String side = action.equalsIgnoreCase(BUY) ? BUY : SELL;
		
		validateOrderRequest(addOrderRequest);
		   
		switch (side) {
			
			case "BUY":
				
				List<Order> sellOrders = orderBookRepo.getAllSellOrdersSortedByPriceAndActive(Action.SELL);
				mapOfOrders = sellOrders.isEmpty() ? ArrayListMultimap.create() : getOrderMap(sellOrders, SELL);
				Order buyOrderResponse = matchOrders(addOrderRequest, mapOfOrders, SELL);
				return buyOrderResponse;

			case "SELL":
				
				List<Order> buyOrders = orderBookRepo.getAllBuyOrdersSortedByPriceAndActive(Action.BUY);
				mapOfOrders = buyOrders.isEmpty() ? ArrayListMultimap.create() : getOrderMap(buyOrders, BUY);
				Order sellOrderResponse = matchOrders(addOrderRequest, mapOfOrders, BUY);
				return sellOrderResponse;
				
			default:
				
				throw new Exception("400");
		}
	}
	 
	 private Order matchOrders(AddOrderRequest req, Multimap<BigDecimal, Order> mapOfOrders, String side) {
		 
			AddOrderRequest originalReq = req;
			Date reqID=new Date();
			Order savedCompletedOrder = new Order();
		   			
			String action = req.getSide().equalsIgnoreCase("BUY") ? "SELL" : "BUY";
			
			ArrayList<BigDecimal> listOfSelectedOrders = reArrangeOrderByprice(req, mapOfOrders, action);
			Integer loopCounter = listOfSelectedOrders.size();
			Set<BigDecimal> listOfUniqueSelectedOrders = new HashSet<>(listOfSelectedOrders);
			
			if (listOfSelectedOrders.isEmpty()) {
				Order orderToSave = new Order();
				orderToSave.setUnique_order(reqID);
				orderToSave.setPrice(req.getPrice());
				orderToSave.setSide(Action.valueOf(req.getSide().toUpperCase()));
				orderToSave.setSize(req.getSize());
				orderToSave.setTime(req.getTime());
				Order savedOrder = orderBookRepo.save(orderToSave);
				return savedOrder;
			} else {
				Trade buyTrade = null;
				Trade sellTrade = null;
				
				if (side.equalsIgnoreCase(SELL)) {
					for (BigDecimal price : listOfUniqueSelectedOrders) {
															
						Collection<Order> ordersOfPrice = mapOfOrders.get(price);
						
						for (Order or : ordersOfPrice) {
							
							BigDecimal originalSellAmount = or.getSize(); 
							BigDecimal originalBuyAmount=req.getSize();
																			
							if (req.getSize().compareTo(BigDecimal.ZERO) > 0 && req.getSize().compareTo(or.getSize()) > 0) {	
								
								BigDecimal tradedAmount = req.getSize().subtract(or.getSize());
								req.setSize(tradedAmount);
								or.setIsActive(Boolean.FALSE);
								or.setSize(BigDecimal.ZERO);
								this.setLastSalePrice(or.getPrice());
								LastSalePrice lastSalePrice = new LastSalePrice(or.getOrderID(), new Date(), or.getPrice());

								buyTrade = new Trade(or.getUnique_order(), reqID, originalSellAmount, req.getPrice()); // half filled buy order trade
								sellTrade = new Trade(or.getUnique_order(), reqID, originalSellAmount, or.getPrice()); // completed sell order

								lastSalePriceRepo.save(lastSalePrice);
								orderBookRepo.updateOrder(or.getOrderID(), or.getSize(), or.getIsActive());
								tradeBookRepo.save(buyTrade);
								tradeBookRepo.save(sellTrade);
								loopCounter--;
								
										  
							} else if (req.getSize().compareTo(BigDecimal.ZERO) > 0 && (req.getSize().compareTo(or.getSize()) < 0)) {
							   							
								or.setSize(or.getSize().subtract(req.getSize()));								
								req.setSize(BigDecimal.ZERO);
																		
								this.setLastSalePrice(or.getPrice());								
								LastSalePrice lastSalePrice=new LastSalePrice(or.getOrderID(), new Date() , or.getPrice());															  								
								Order completedBuyOrder=new Order(reqID, req.getTime(), req.getSize(), req.getPrice(),  Action.BUY, Boolean.FALSE);  //completed buy order										  
								buyTrade =new Trade(or.getUnique_order(), reqID, originalBuyAmount, req.getPrice());	   //completed buy trade					 
								sellTrade = new Trade(or.getUnique_order(), reqID,  originalSellAmount, or.getPrice());	 //half filled sell trade	 								
								savedCompletedOrder = completedBuyOrder;	
								
								lastSalePriceRepo.save(lastSalePrice);
								orderBookRepo.updateOrder(or.getOrderID(), or.getSize(), or.getIsActive());
								orderBookRepo.save(completedBuyOrder);
								tradeBookRepo.save(buyTrade);						 
								tradeBookRepo.save(sellTrade);
								loopCounter--;
													
							} else if (req.getSize().compareTo(BigDecimal.ZERO) > 0
									&&  req.getSize().compareTo(or.getSize()) == 0) {
							   			 
								BigDecimal buyAmount=req.getSize();
								BigDecimal sellAmount= or.getSize();								
								BigDecimal tradedAmount = or.getSize().subtract(req.getSize());
								or.setSize(tradedAmount);
								or.setIsActive(Boolean.FALSE);
								req.setSize(BigDecimal.ZERO);							   
								
								
								if(tradedAmount.compareTo(BigDecimal.ZERO)==0) {
									
									Order completedBuyOrder = new Order(reqID, req.getTime(), req.getSize(), req.getPrice(), Action.BUY, Boolean.FALSE ); //completed buy order				  
									buyTrade = new Trade(or.getUnique_order(), reqID, buyAmount, originalReq.getPrice());	//completed buy trade 
									sellTrade = new Trade(or.getUnique_order(), reqID,  sellAmount, or.getPrice());	   //completed sell trade
									savedCompletedOrder = completedBuyOrder;										  
									this.setLastSalePrice(or.getPrice());									
									LastSalePrice lastSalePrice=new LastSalePrice(or.getOrderID(), new Date() , or.getPrice());
									
									lastSalePriceRepo.save(lastSalePrice);	
									orderBookRepo.updateOrder(or.getOrderID(), or.getSize(), or.getIsActive());								
									orderBookRepo.save(completedBuyOrder);						   
									tradeBookRepo.save(buyTrade);
									tradeBookRepo.save(sellTrade);		  
							  
								}
								
							   loopCounter--;
								
							}
						   
							if(req.getSize().compareTo(BigDecimal.ZERO) > 0 && loopCounter<=0) {
								Order halfFilledBuyOrder=new Order(reqID, req.getTime(), req.getSize(), req.getPrice(), Action.BUY, Boolean.TRUE );
								orderBookRepo.save(halfFilledBuyOrder);
								
							}													
						}  												
					}

					return savedCompletedOrder;
					
				} else {
		  	
					for (BigDecimal price : listOfUniqueSelectedOrders) {
												   
						Collection<Order> ordersOfPrice = mapOfOrders.get(price);
						
						for (Order or : ordersOfPrice) {
							
							BigDecimal originalBuyAmount = or.getSize(); 
						
							if (req.getSize().compareTo(BigDecimal.ZERO) > 0 && req.getSize().compareTo(or.getSize()) > 0) {	
								
								BigDecimal tradedAmount = req.getSize().subtract(or.getSize());								
								req.setSize(tradedAmount);		
								or.setIsActive(Boolean.FALSE);
								or.setSize(BigDecimal.ZERO);							   
								this.setLastSalePrice(or.getPrice());
								
								LastSalePrice lastSalePrice=new LastSalePrice(or.getOrderID(), new Date() , or.getPrice());																																							  
								sellTrade = new Trade(or.getUnique_order(), reqID, originalBuyAmount, req.getPrice()); //half filled sell trade	   							   
								buyTrade = new Trade(or.getUnique_order(), reqID, originalBuyAmount, or.getPrice()); //completed buy order trade
														 							
								lastSalePriceRepo.save(lastSalePrice);					 
								orderBookRepo.updateOrder(or.getOrderID(), or.getSize(), or.getIsActive());					  
								tradeBookRepo.save(sellTrade);					
								tradeBookRepo.save(buyTrade);
								loopCounter--;
																																  
							}  else  if  (req.getSize().compareTo(BigDecimal.ZERO) > 0 && (req.getSize().compareTo(or.getSize()) < 0))  {
								
								BigDecimal remainingSellAmount = req.getSize();
								or.setSize(or.getSize().subtract(req.getSize()));								
								req.setSize(BigDecimal.ZERO);
								this.setLastSalePrice(or.getPrice());						 
								
								LastSalePrice lastSalePrice=new LastSalePrice(or.getOrderID(), new Date() , or.getPrice());		
								Order completedSellOrder = new Order(reqID, req.getTime(), req.getSize(), req.getPrice(),  Action.SELL, Boolean.FALSE); //completed sell order										   
								sellTrade = new Trade(or.getUnique_order(), reqID, remainingSellAmount, req.getPrice()); //completed sell trade								
								buyTrade = new Trade(or.getUnique_order(), reqID,  originalBuyAmount, or.getPrice()); //half filled buy trade							   
								savedCompletedOrder = completedSellOrder;
																	   
													
								lastSalePriceRepo.save(lastSalePrice);							  							
								orderBookRepo.updateOrder(or.getOrderID(), or.getSize(), or.getIsActive());
								orderBookRepo.save(completedSellOrder);
								tradeBookRepo.save(sellTrade);
								tradeBookRepo.save(buyTrade);
								loopCounter--;
				
							 
							} else if (req.getSize().compareTo(BigDecimal.ZERO) > 0 &&  req.getSize().compareTo(or.getSize()) == 0) {
								 
								BigDecimal sellAmount=req.getSize();
								BigDecimal buyAmount= or.getSize();								
								BigDecimal tradedAmount = or.getSize().subtract(req.getSize());
								
								or.setSize(tradedAmount);
								or.setIsActive(Boolean.FALSE);								
								req.setSize(BigDecimal.ZERO);
							   
								
								if(tradedAmount.compareTo(BigDecimal.ZERO)==0) {
									
									this.setLastSalePrice(or.getPrice());								  
									LastSalePrice lastSalePrice=new LastSalePrice(or.getOrderID(), new Date() , or.getPrice());				
									Order completedSellOrder = new Order(reqID, req.getTime(), req.getSize(), req.getPrice(), Action.SELL, Boolean.FALSE );	  //completed sell order					
									sellTrade = new Trade(or.getUnique_order(), reqID, sellAmount, originalReq.getPrice());				 //completed sell trade
									buyTrade = new Trade(or.getUnique_order(), reqID,  buyAmount, or.getPrice());					//completed buy trade
									savedCompletedOrder = completedSellOrder;
																		   															
									lastSalePriceRepo.save(lastSalePrice);									
									orderBookRepo.updateOrder(or.getOrderID(), or.getSize(), or.getIsActive());
									orderBookRepo.save(completedSellOrder);  
									tradeBookRepo.save(sellTrade);								 
									tradeBookRepo.save(buyTrade);
	  							  
							}	
								
							   loopCounter--;
								
						}
							
							if(req.getSize().compareTo(BigDecimal.ZERO) > 0 && loopCounter==0) {
								Order halfFilledSellOrder=new Order(reqID, req.getTime(), req.getSize(), req.getPrice(), Action.SELL, Boolean.TRUE );
								orderBookRepo.save(halfFilledSellOrder);	
							}
													
						}
																	  						
					}

				}
				return savedCompletedOrder;
			}
		}

		public BigDecimal getLastSalePrice() {
		return lastSalePrice;
	}

	public void setLastSalePrice(BigDecimal lastSalePrice) {
		this.lastSalePrice = lastSalePrice;
	}

		private ArrayList<BigDecimal> reArrangeOrderByprice(AddOrderRequest req, Multimap<BigDecimal, Order> mapOfOrders, String action) {
			if (action.equalsIgnoreCase("SELL")) {
				return (ArrayList<BigDecimal>) mapOfOrders.keys().stream()
						.filter(key -> key.compareTo(req.getPrice()) <= 0)
						.sorted()
						.collect(Collectors.toList());
			} else {
				return (ArrayList<BigDecimal>) mapOfOrders.keys().stream()
						.filter(key -> key.compareTo(req.getPrice()) >= 0)
						.sorted(Comparator.reverseOrder())
						.collect(Collectors.toList());

			}
		}
		
		
//	  get orders listed and sorted according to price and time 
	 
		private Multimap<BigDecimal, Order> getOrderMap(List<Order> sellOrders, String side) throws Exception {
			Multimap<BigDecimal, Order> mapOfOrders = ArrayListMultimap.create();
			switch (side) {
				case "SELL":
					List<Order> sortedSellOrders = sellOrders.stream().sorted(Comparator.comparing(or -> or.getTime())).collect(Collectors.toList());
					sortedSellOrders.stream()
							.sorted((p1, p2) -> p1.getPrice().compareTo(p2.getPrice()))
							.forEach(or -> mapOfOrders.put(or.getPrice(), or));
					return mapOfOrders;
				case "BUY":
					List<Order> sortedBuyOrders = sellOrders.stream().sorted(Comparator.comparing(or -> or.getTime())).collect(Collectors.toList());
					sortedBuyOrders.stream()
							.sorted((p1, p2) -> p2.getPrice().compareTo(p1.getPrice()))
							.forEach(or -> mapOfOrders.put(or.getPrice(), or));
					return mapOfOrders;
				default:
					throw new Exception("402");
			}

		}
		
//		a simple validation wrapper for the add order request.
	   
	
		private void validateOrderRequest(AddOrderRequest req) throws Exception {
			if (StringUtils.isBlank(req.getSide()) || (req.getSide().equalsIgnoreCase("BUY")
					&& req.getSide().equalsIgnoreCase("SELL"))) {
				throw new Exception("403");
			} else if (req.getPrice() == null) {
				throw new Exception("404");
			} else if (req.getSize() == null) {
				throw new Exception("405");
			} else if (req.getTime() == null) {
				throw new Exception( "406");
			}
		}

		
//		get all orders sorted by side, price and sum of order amount

		public Map<BigDecimal, BigDecimal> getAllOrdersBySide(Boolean active, Action action) {
			Map<BigDecimal, BigDecimal> mappedOrders = new LinkedHashMap<>();
			List<BigDecimal> uniquePriceList = new ArrayList<>();
			BigDecimal sumAmount = BigDecimal.ZERO;
			uniquePriceList = orderBookRepo.getAllOrdersWithUniquePrice(action, active);
			
			for (BigDecimal price : uniquePriceList) {
				int count = orderBookRepo.getCountOfOrderPrices(price, action, active);
				if (count > 1) {
					sumAmount = orderBookRepo.getAllOrderSize(price, action, active);
					mappedOrders.put(price, sumAmount);
				} else if (count == 1) {
					sumAmount = orderBookRepo.getAllOrderSize(price, action, active);
					mappedOrders.put(price, sumAmount);
				}
			}
			return mappedOrders;
		}
	}		   		
		
// public Map<BigDecimal, BigDecimal> getAllOrdersBySide(Boolean active, Action action) {
//			
//			Map<BigDecimal, BigDecimal> mappedOrders = new HashMap<>();
//			BigDecimal sumAmount =BigDecimal.ZERO;
//			List<Order> orders = new ArrayList<>();
//			BigDecimal singleAmount= BigDecimal.ZERO;
//			BigDecimal singlePrice = BigDecimal.ZERO;
//			
//			
//			orders = orderBookRepo.findByIsActiveAndSide(active, action);
//			
//			List<BigDecimal> uniquePrices = orders.stream()
//					.map(Order::getPrice)
//					.distinct()
//					.collect(Collectors.toList());
//		   
//				for(BigDecimal price : uniquePrices) {
//						  
//					
//					int count= (int) orders.stream().filter(arg -> arg.getPrice().equals(price)).count();
//					
//					if(count>1) {
//																
//						List<Order> samePriceOrderList= orders.stream().filter(arg -> arg.getPrice().equals(price)).collect(Collectors.toList());
//												
//							for(Order or : samePriceOrderList) {
//								sumAmount=sumAmount.add(or.getSize());
//							}
//										   														
//							
//					 mappedOrders.put(price, sumAmount);
//					
//										
//					}else if(count==1) {
//						
//						for(Order order: orders) {
//							if(order.getPrice().equals(price)) {
//								
//								singleAmount =order.getSize();
//								singlePrice = order.getPrice();
//								
//							
//							}
//						}
//						
//						mappedOrders.put(singlePrice, singleAmount);
//											
//					}
//				}
//												 
//			return mappedOrders;
//		}
	 

