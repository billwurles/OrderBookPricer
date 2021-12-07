package burl.es.orderbook.model;

import burl.es.orderbook.model.engine.OrderBook;
import burl.es.orderbook.model.exceptions.OrderNotFoundException;
import burl.es.orderbook.model.exceptions.OrderParseException;
import burl.es.orderbook.model.order.*;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;
import java.time.*;
import java.util.ArrayList;
import java.util.SortedSet;

@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
@RestController
@RequestMapping(path = "/orderbook")
public class OrderBookController {

	//TODO: add database access

	@NonNull
	private static final OrderBook book = new OrderBook();

	@PostMapping(path = "/order", produces = "application/json")
	public Order addOrder(@RequestBody OrderSnapshot snap) throws OrderNotFoundException { //TODO: input validation - or does spring just do it?
		book.addOrder(new Order(ZonedDateTime.now(), snap.getOrderId(), snap.getSide(), new BigDecimal(snap.getPrice()), new BigDecimal(snap.getSize())));
		return book.getOrderById(snap.getOrderId());
	}

	@GetMapping(path = "/orders", produces = "application/json")
	public ArrayList<Order> getAllOrders(){
		return book.getOrders();
	}

	@GetMapping(path = "/open", produces = "application/json")
	public ArrayList<Order> getOpenOrders(){ //TODO: pagination
		return book.getOpenOrders();
	}

	@GetMapping(path = "/filled", produces = "application/json")
	public ArrayList<Order> getFilledOrders(){
		return book.getFilledOrders();
	}

	@GetMapping(path = "/sells", produces = "application/json")
	public ArrayList<Order> getAllSells(){
		return book.getSellOrders();
	}

	@GetMapping(path = "/buys", produces = "application/json")
	public ArrayList<Order> getAllBuys(){
		return book.getBuyOrders();
	}

	@PostMapping(path = "/reduce", produces = "application/json")
	public Order reduceOrder(@RequestBody ReduceSnapshot snap) throws OrderNotFoundException {
		book.reduce(new Reduce(ZonedDateTime.now(), "redId", snap.getOrderId(), new BigDecimal(snap.getSize())));
		return book.getOrderById(snap.getOrderId());
	}

	@GetMapping(value = "/order/{id}")
	public Order findById(@PathVariable("id") String id) throws OrderNotFoundException {
		return book.getOrderById(id);
	}
}
