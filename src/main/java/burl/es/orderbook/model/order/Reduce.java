package burl.es.orderbook.model.order;

import burl.es.orderbook.model.exceptions.IllegalOrderException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Slf4j
public class Reduce extends BaseOrder {
	@NonNull
	public final String orderToReduce;
	private boolean filled = false;

//todo: orderIds technically not unique when reducer are  involved
	public Reduce(@NonNull ZonedDateTime timestamp, @NonNull String reduceId, @NonNull String orderToReduce, @NonNull BigDecimal size) {
		super(timestamp, reduceId, Side.REDUCE, size);
		this.orderToReduce = orderToReduce;
	}

	public void reduce(Order order){
		isReduceValid(order);
		BigDecimal remainder = order.getFillRemainder();
		if(getSize().compareTo(remainder) > -1){
			log.debug("Reduce size is larger or equal to fill remainder, order is cancelled");
			order.setSize(order.getFill());
		} else {
			log.debug("Reduce size is smaller than remainder, reducing by {}",getSize());
			order.setSize(order.getSize().subtract(getSize()));
		}
		order.addConsumedOrder(this);
		addConsumedOrder(order);
		filled = true;
		setSize(BigDecimal.ZERO);
	}

	private void isReduceValid(@NonNull BaseOrder order) {
		if(order.getSide() == Side.REDUCE){
			throw new IllegalOrderException("Cannot reduce a reduce");
		}
	}

	@Override
	public boolean isFilled() {
		return filled;
	}
}
