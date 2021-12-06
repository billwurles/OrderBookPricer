package burl.es.orderbook.model.order;

import burl.es.orderbook.model.exceptions.IllegalOrderException;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Slf4j
public class Reduce extends Order {

	public final String reduceId;

//todo: orderIds technically not unique when reducer are  involved
	public Reduce(@NonNull ZonedDateTime timestamp, @NonNull String reduceId, @NonNull String orderId, @NonNull BigDecimal size) {
		super(timestamp, orderId, Side.REDUCE, BigDecimal.ZERO, size);
		this.reduceId = reduceId;
	}

	@Override
	public void fill(Order order){
		canReduceOrder(order);
		BigDecimal remainder = order.getFillRemainder();
		if(getSize().compareTo(remainder) > -1){
			log.debug("Reduce size is larger or equal to fill remainder, order is cancelled");
			order.setSize(order.getFill());
		} else {
			log.debug("Reduce size is smaller than remainder, reducing by {}",getSize());
			order.setSize(order.getSize().subtract(getSize()));
		}
		order.getConsumedOrders().add(this);
		getConsumedOrders().add(order);
	}

	private void canReduceOrder(Order order){
		isOrderValid(order);
		if(order.getSide() == Side.REDUCE){
			throw new IllegalOrderException("Cannot reduce a reduce");
		}
	}

}
