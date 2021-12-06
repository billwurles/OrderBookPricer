package burl.es.orderbook.model.order;

import burl.es.orderbook.model.exceptions.IllegalOrderException;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZonedDateTime;
import java.util.ArrayList;

@ToString
@Getter
//@RequiredArgsConstructor
@EqualsAndHashCode
@FieldDefaults(level = AccessLevel.PROTECTED)
@Slf4j
public class Order {

	@NonNull
	final ZonedDateTime timestamp;
	@NonNull
	final String orderId;
	@NonNull
	final Side side;
	@NonNull
	final BigDecimal price;
	@NonNull
	@Setter
	private BigDecimal size;
	private BigDecimal cost = new BigDecimal("0.00");
	private BigDecimal fill  = new BigDecimal("0.00");
	@ToString.Exclude
	@JsonIgnore // do you like infinite loops?
	private final ArrayList<Order> consumedOrders = new ArrayList<>();

	public Order(@NonNull ZonedDateTime timestamp, @NonNull String orderId, @NonNull Side side, @NonNull BigDecimal price, @NonNull BigDecimal size) {
		if(price.compareTo(BigDecimal.ZERO) < 0){
			throw new IllegalArgumentException("Price negative - "+price);
		} else if(size.compareTo(BigDecimal.ZERO) < 1){
			throw new IllegalArgumentException("Size negative or zero - "+size);
		}
		this.timestamp = timestamp;
		this.orderId = orderId;
		this.side = side;
		this.price = price;
		this.size = size;
	}

	public void fill(Order order) {
		canBuyBeFilled(order);
		completeFill(order);
	}

//	public void reduceOrder(Reduce reduce){
//		if(!isFilled()){
//			BigDecimal remainder = getFillRemainder();
//			if(reduce.getSize().compareTo(remainder) > -1){
//				log.debug("Reduce size is larger or equal to fill remainder, order is cancelled");
//				size  = fill;
//			} else {
//				log.debug("Reduce size is smaller than remainder, reducing by {}",reduce.getSize());
//				size = size.subtract(reduce.getSize());
//			}
//			consumedOrders.add(reduce);
//		}
//	}

	protected void isOrderValid(Order order){
		if(isFilled() || order.isFilled()){
			throw new IllegalOrderException("Order is filled");
		} else if(consumedOrders.contains(order) || order.getConsumedOrders().contains(this)){
			throw new IllegalOrderException("Order is consumed");
		} else if(order == this) {
			throw new IllegalOrderException("Order cannot fill itself");
		}
	}

	private void canBuyBeFilled(Order order) {
		isOrderValid(order);
		if(side != Side.SELL){
			throw new IllegalOrderException("Only sells can fill");
		} else if(order.getSide() != Side.BUY){
			throw new IllegalOrderException("Only buys can be filled");
		} else if(price.compareTo(order.price) > 0){
			throw new IllegalOrderException(String.format("{} - {} price too high {} < {}", orderId, order.orderId, price, order.price));
		}
	}

	private void completeFill(Order order){
		BigDecimal orderRemainder = order.getFillRemainder();
		BigDecimal fillRemaining = getFillRemainder();
		BigDecimal amountToFill;
		int comparison = fillRemaining.compareTo(orderRemainder);
		if(comparison <= 0){
			amountToFill = fillRemaining;
		} else {
			amountToFill = orderRemainder;
		}

		BigDecimal buyPrice;
		if(side == Side.SELL) {
			buyPrice = order.price;
			order.completeFill(this);
		} else {
			buyPrice = this.price;
		}

		fill = fill.add(amountToFill);
		cost = cost.add(amountToFill.multiply(buyPrice));
		consumedOrders.add(order);
		log.info("Completed {}\tfor {},\t {} shares @ {}\t {}% filled", side, orderId, amountToFill, buyPrice, getFillPercentage());
	}

	public boolean isFilled(){
		return size.compareTo(fill) <= 0;
	}

	protected BigDecimal getFillRemainder(){
		return size.subtract(fill);
	}

	public BigDecimal getFillPercentage(){
		if(size.compareTo(BigDecimal.ZERO) < 1)
			return new BigDecimal(100L);
		return fill.divide(size, RoundingMode.HALF_DOWN).multiply(BigDecimal.valueOf(100L));
	}
}