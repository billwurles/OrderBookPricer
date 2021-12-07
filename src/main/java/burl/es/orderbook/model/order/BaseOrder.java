package burl.es.orderbook.model.order;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;

@RequiredArgsConstructor
@Getter
@EqualsAndHashCode
@ToString
public abstract class BaseOrder implements Cloneable { //todo: get a better name

    @NonNull
    final ZonedDateTime timestamp;
    @NonNull
    final String orderId;
    @NonNull
    final Side side;
    @NonNull
    @Setter
    private BigDecimal size;
    @ToString.Exclude
    @JsonIgnore // do you like infinite loops?
    private final ArrayList<BaseOrder> consumedOrders = new ArrayList<>();

    public abstract boolean isFilled();

    public void addConsumedOrder(BaseOrder order){
        if(order == this) throw new IllegalArgumentException("Cannot add order to it's own consumed order list");
        consumedOrders.add(order);
    }

}
