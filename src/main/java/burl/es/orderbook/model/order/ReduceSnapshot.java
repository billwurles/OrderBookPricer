package burl.es.orderbook.model.order;

import lombok.Data;

@Data
public class ReduceSnapshot {
	final long timestamp;
	final String orderId;
	final String size;
}
