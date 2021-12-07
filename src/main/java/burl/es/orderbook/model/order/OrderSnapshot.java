package burl.es.orderbook.model.order;

public record OrderSnapshot(String orderId, Side side, String price, String size) {

//	timestamp 	The time when this message was generated by the market, as milliseconds since midnight.
//	A 			literal string identifying this as an "Add Order to Book" message.
//			order-id 	A unique string that subsequent "Reduce Order" messages will use to modify this order.
//	side 		A 'B' if this is a buy order (a bid), and a 'S' if this is a sell order (an ask).
//	price 		The limit price of this order.
//	size 		The size in shares of this order, when it was initially sent to the market by some stock trader.

}
