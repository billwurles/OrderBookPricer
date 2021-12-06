package burl.es.orderbook.model;

import burl.es.orderbook.model.engine.OrderBook;
import burl.es.orderbook.model.exceptions.OrderNotFoundException;
import burl.es.orderbook.model.exceptions.OrderParseException;
import burl.es.orderbook.model.exceptions.OrderTimestampMalformedException;
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
	public Order addOrder(@RequestBody OrderSnapshot snap) throws OrderNotFoundException {
		book.addOrder(new Order(parseOrderTimeStamp(snap.getTimestamp()), snap.getOrderId(), snap.getSide(), new BigDecimal(snap.getPrice()), new BigDecimal(snap.getSize())));
		return book.getOrderById(snap.getOrderId());
	}

	@GetMapping(path = "/orders", produces = "application/json")
	public SortedSet<Order> getAllOrders(){
		return book.getOrders();
	}

	@GetMapping(path = "/open", produces = "application/json")
	public ArrayList<Order> getOpenOrders(){ //TODO: pagination
		return book.getOpenOrders();
	}

	@GetMapping(path = "/filled", produces = "application/json")
	public SortedSet<Order> getFilledOrders(){
		return book.getFilledOrders();
	}

	@GetMapping(path = "/sells", produces = "application/json")
	public SortedSet<Order> getAllSells(){
		return book.getSellOrders();
	}

	@GetMapping(path = "/buys", produces = "application/json")
	public SortedSet<Order> getAllBuys(){
		return book.getBuyOrders();
	}


	public long maxHashTime = 0;
	public long maxBubbleTime = 0;

	@PostMapping(path = "/reduce", produces = "application/json")
	public Order reduceOrder(@RequestBody ReduceSnapshot snap) throws OrderParseException, OrderNotFoundException {
//		log.debug("Reducing order: {}", snap);
		book.reduce(new Reduce(parseOrderTimeStamp(snap.getTimestamp()), "redId", snap.getOrderId(), new BigDecimal(snap.getSize())));

//		Order order;
//		long startTime = System.nanoTime();
//		try {
//			order = book.getOrderById(snap.getOrderId());
//		} catch (OrderNotFoundException e){
//		}
//		long endTime = System.nanoTime();
//		long durationBubble = (endTime - startTime);  //divide by 1000000 to get milliseconds.
//		startTime = System.nanoTime();
//		order = book.getOrderByHash(snap.getOrderId());
//		endTime = System.nanoTime();
//		long durationHash = (endTime - startTime);  //divide by 1000000 to get milliseconds.
//		if(durationHash > maxHashTime) maxHashTime = durationHash;
//		if(durationBubble > maxBubbleTime) maxBubbleTime = durationBubble;
//		log.debug("getOrderById execution time \t hash: {}ns \tbinary: {}ns",durationHash,durationBubble);

		return book.getOrderById(snap.getOrderId());
	}

	@GetMapping(value = "/order/{id}")
	public Order findById(@PathVariable("id") String id) throws OrderNotFoundException {
		return book.getOrderById(id);
	}

	private static ZonedDateTime parseOrderTimeStamp(long millisSinceMidnight) throws OrderTimestampMalformedException {
		try {
			LocalDateTime todayMidnight = LocalDateTime.of(LocalDate.now(), LocalTime.MIDNIGHT);
			ZonedDateTime zdt = todayMidnight.atZone(ZoneId.of("Europe/London"));
			long midnightEpoch = zdt.toInstant().toEpochMilli();
			return ZonedDateTime.ofInstant(Instant.ofEpochMilli(midnightEpoch + millisSinceMidnight), ZoneId.of("Europe/London"));
		} catch(Exception e){
			throw new OrderTimestampMalformedException("Timestamp '"+millisSinceMidnight+"' is invalid");
		}
	}
}
