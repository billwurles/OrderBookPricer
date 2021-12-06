package burl.es.orderbook.model.exceptions;

public class UnknownOrderTypeException extends OrderParseException {

	public UnknownOrderTypeException(String message) {
		super(message);
	}

}
