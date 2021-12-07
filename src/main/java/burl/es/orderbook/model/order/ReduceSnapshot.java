package burl.es.orderbook.model.order;

import lombok.Data;

@Data
public class ReduceSnapshot {
	final String orderId;
	final String size;
}
