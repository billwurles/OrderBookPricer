package burl.es.orderbook.model.engine;

import burl.es.orderbook.model.exceptions.IllegalOrderException;
import burl.es.orderbook.model.exceptions.OrderNotFoundException;
import burl.es.orderbook.model.order.*;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import java.util.*;

@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
@Repository
public class OrderBook {

//	@NonNull
	final String assetSymbol;
//	@NonNull
	final int targetSize;

//	private final List<Order> orders; // for getting by id - could use a hashmap for this - vs binary searching?
	private final Map<String, Order> orders;
	private final TreeSet<Order> filledOrders; // treeSet is O(log(N))
	private final TreeSet<Order> orderBookBuy;
	private final TreeSet<Order> orderBookSell;

//	public OrderBook(String assetSymbol, int targetSize){
	public OrderBook(){
		this.assetSymbol = "ETH-USD";
		this.targetSize = 200;
		orderBookBuy = new TreeSet<>(new Comparator<Order>() {
			@Override
			public int compare(Order o1, Order o2) {
				if(o1.getPrice().equals(o2.getPrice()))
					if(o1.getSide() != o2.getSide())
						return 0;
					else return o1.getTimestamp().compareTo(o2.getTimestamp());
				return o2.getPrice().compareTo(o1.getPrice());
			}
		});
		orderBookSell = new TreeSet<>(new Comparator<Order>() {
			@Override
			public int compare(Order o1, Order o2) {
				if(o1.getPrice().equals(o2.getPrice()))
					if(o1.getSide() != o2.getSide())
						return 0;
					else return o1.getTimestamp().compareTo(o2.getTimestamp());
				return o1.getPrice().compareTo(o2.getPrice());
			}
		});
		filledOrders = new TreeSet<>(new OrderComparator()); //Sorted by id
		orders = new HashMap<>();
		//TODO: maybe just use hashset or arraylist? at least implement new comparator for visual purposes
	}

// Insertion to book execution time 	 map: 198ns / list: 130ns / set: 172ns
	public Order addOrder(Order order){
		orders.put(order.getOrderId(), order);
		switch (order.getSide()){
			case SELL -> orderBookSell.add(order);
			case BUY -> orderBookBuy.add(order);
		}
		checkForFills();

//		long startTime = System.nanoTime();

//		long endTime = System.nanoTime();
//		long durationSet = (endTime - startTime);  //divide by 1000000 to get milliseconds.
//
//		startTime = System.nanoTime(); //TODO: ensure ids are unique - why are ids external anyway?
//		ordersHash.put(order.getOrderId(), order);
//		endTime = System.nanoTime();
//		long durationMap = (endTime - startTime);  //divide by 1000000 to get milliseconds.
//
//		startTime = System.nanoTime();
//		orders.add(order);
//		endTime = System.nanoTime();
//		long durationList = (endTime - startTime);  //divide by 1000000 to get milliseconds.

//		log.debug("Insertion to book execution time \t map: {}ns \tlist: {}ns\t set: {}ns",durationMap,durationList,durationSet);
		log.info("Inserting {}\t '{}'\t for {} shares @ {} - {}",order.getSide(),order.getOrderId(),order.getSize(),order.getPrice(),order.getTimestamp());
//		checkForFills();
		return order.copy();
	}

	public Order reduce(Reduce reduce) throws OrderNotFoundException {
		log.info("Inserting REDUCE\t '{}'\t for order '{}' shares @ {} - {}",reduce.reduceId, reduce.getOrderId(),reduce.getSize(),reduce.getTimestamp());
		Order order = getOrderById(reduce.getOrderId());
		try {
			reduce.fill(order);
		}  catch (IllegalOrderException e){
			log.error("Can't reduce that order, probs already filled");
		}
		removeIfFullyFilled(order);
		return order.copy();
	}

	public String checkForFills() { //TODO: run this on separate thread to insertions
		boolean checkedSells, checkedBuys;
		SortedSet<Order> sellBook = orderBookSell;//, buyBook;
//		SortedSet<Order> sellBook = orderBookSell.headSet(orderBookBuy.first()), buyBook;
		do {
			checkedSells = true;
			sell:
			for(Order sell : sellBook) {
				do {
					SortedSet<Order> buyBook = orderBookBuy.headSet(sell, true); //Next sell will be more expensive so we do not need lower buys
					checkedBuys = true;
					buy:
					for (Order buy : buyBook) {
						sell.fill(buy);
						if(removeIfFullyFilled(sell)){
							sellBook = orderBookSell.tailSet(sell, true);
							checkedSells = false;
						}
						if(removeIfFullyFilled(buy)){
							checkedBuys = false;
						}
						if(!checkedSells) break sell;
						if(!checkedBuys) break buy;
					}
				} while (!checkedBuys);
			}
		} while(!checkedSells);
		return ""; //TODO: Implement some kind of return string, or not
	}

	private boolean removeIfFullyFilled(Order order){
		if (order.isFilled()) {
			switch (order.getSide()){
				case SELL -> orderBookSell.remove(order);
				case BUY -> orderBookBuy.remove(order);
			}
			log.info("{}\t'{}' fully filled - £{} - {} shares @ {}",order.getSide(), order.getOrderId(), order.getCost(), order.getSize(), order.getPrice());
			filledOrders.add(order);
			return true;
		}
		return false;
	}

	public SortedSet<Order> getOrders() {
		SortedSet<Order> orders = filledOrders.tailSet(filledOrders.first());
		orders.addAll(orderBookSell);
		orders.addAll(orderBookBuy);
		return orders;
	}

	/*Total time spent getting order by ID per algorithm:
			hash: 23457ns
			binary search: 4699574ns
	//Max individual times: */
	public Order getOrderById(String orderId) throws OrderNotFoundException {
		Order order = orders.get(orderId);
		if(order == null) throw new OrderNotFoundException(orderId);
		return order;
	}

//	private Order getOrderByReduce(Reduce reduce) throws OrderNotFoundException {
//		int i = Collections.binarySearch(orders,
//				reduce,
//				new Comparator<Order>() {
//					@Override
//					public int compare(Order o1, Order o2) {
//						return o1.getOrderId().compareTo(o2.getOrderId());
//					}
//				});
//		if(i < 0) throw new OrderNotFoundException(reduce.getOrderId());
//		return orders.get(i);
//	}
//
//	public Order getOrderById(String id) throws OrderNotFoundException {
//		log.debug("Getting order for ID '{}'",id);
//		return return getOrderByReduce(new Reduce(ZonedDateTime.now(), "", id, BigDecimal.ZERO)); //TODO: need an id generator
//	}

	public SortedSet<Order> getFilledOrders() {
		return filledOrders;
	}

	public ArrayList<Order> getOpenOrders() {
		ArrayList<Order> list = new ArrayList<Order>(orderBookSell);
		list.addAll(orderBookBuy);
		list.sort(new OrderComparator());
		return list;
	}

	public SortedSet<Order> getSellOrders() {
		return orderBookSell; //TODO: make all these return immutable versions
	}

	public SortedSet<Order> getBuyOrders() {
		return orderBookBuy;
	}
}
