package burl.es.orderbook.model.exceptions;

public class OrderNotFoundException extends Exception{

	public OrderNotFoundException(String message) {
		super("Order '"+message+"' not found");
	}
}
