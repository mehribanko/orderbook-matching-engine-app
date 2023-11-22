package com.orderbook.controller;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import com.orderbook.enumerator.Action;
import com.orderbook.pojo.AddOrderRequest;
import com.orderbook.service.OrderBook;


@Controller
public class HomeController {
	
	private OrderBook orderBook;
	
	@Autowired
	public HomeController(OrderBook theOrderBook) {
		orderBook=theOrderBook;
	}
	
	@GetMapping(value = "/")
	public String getHomePage(Model model) {
		
		model.addAttribute("saleprice", orderBook.getLastSalePrice());
		model.addAttribute("sellorders",orderBook.getAllOrdersBySide(true, Action.SELL));
		model.addAttribute("buyorders",orderBook.getAllOrdersBySide(true, Action.BUY));
		return "homepage";
	}
	
	@PostMapping("/addbuyorder")
	public String addBuyOrder(AddOrderRequest addOrderRequest) throws Exception {
		
		orderBook.processOrder(addOrderRequest);
		return "redirect:/";
	}
	
	
	@PostMapping("/addsellorder")
	public String addSellOrder(AddOrderRequest addOrderRequest) throws Exception {
		
		orderBook.processOrder(addOrderRequest);
		return "redirect:/";
	}
		
}
