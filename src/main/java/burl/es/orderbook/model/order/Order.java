package burl.es.orderbook.model.order;

import burl.es.orderbook.model.exceptions.IllegalOrderException;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZonedDateTime;
import java.util.ArrayList;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Getter
@Slf4j
public class Order extends BaseOrder {

	@NonNull
	final BigDecimal price;
	private BigDecimal cost = new BigDecimal("0.00");
	private BigDecimal fill  = new BigDecimal("0.00");

	public Order(@NonNull ZonedDateTime timestamp, @NonNull String orderId, @NonNull Side side, @NonNull BigDecimal price, @NonNull BigDecimal size) {
		super(timestamp,orderId,side,size);
		if(price.compareTo(BigDecimal.ZERO) < 0){
			throw new IllegalArgumentException("Price negative - "+price);
		} else if(size.compareTo(BigDecimal.ZERO) < 1){
			throw new IllegalArgumentException("Size negative or zero - "+size);
		}
		this.price = price;
	}

	public void fill(@NonNull Order order) {
		isOrderValid(order);
		if(price.compareTo(order.price) <= 0){
			completeFill(order);
		} else throw new IllegalOrderException(String.format("%s - %s price too high %s < %s", orderId, order.orderId, price, order.price));
	}

	private void isOrderValid(@NonNull Order order){
		if(isFilled() || order.isFilled()){
			throw new IllegalOrderException(String.format("Order is filled: \n%s\n%s", this, order));
		} else if(getConsumedOrders().contains(order) || order.getConsumedOrders().contains(this)){
			throw new IllegalOrderException("Order is consumed");
		} else if(order == this) {
			throw new IllegalOrderException("Order cannot fill itself");
		} else if(side != Side.SELL){
			throw new IllegalOrderException("Only sells can fill");
		} else if(order.getSide() != Side.BUY){
			throw new IllegalOrderException("Only buys can be filled");
		}
	}

	private void completeFill(@NonNull Order order){
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
		addConsumedOrder(order);
		log.info("Filled {}\tfor {},\t {} shares @ {}\t {}% filled", side, orderId, amountToFill, buyPrice, getFillPercentage());
	}

	@Override
	public boolean isFilled(){
		return getSize().compareTo(fill) <= 0;
	}

	protected BigDecimal getFillRemainder(){
		return getSize().subtract(fill);
	}

	public BigDecimal getFillPercentage(){
		return fill.divide(getSize(), RoundingMode.HALF_DOWN).multiply(BigDecimal.valueOf(100L));
	}

	@Override
	public Order clone() {
		try {
			Order clone = (Order) super.clone();
			// TODO: copy mutable state here, so the clone can't change the internals of the original
			return clone;
		} catch (CloneNotSupportedException e) {
			throw new  IllegalOrderException("Error occurred when cloning order");
		}
	}
}