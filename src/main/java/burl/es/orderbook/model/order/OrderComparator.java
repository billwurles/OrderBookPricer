package burl.es.orderbook.model.order;

import java.util.Comparator;

public class OrderComparator implements Comparator<Order> {

    @Override
    public int compare(Order o1, Order o2) {
        if(o1.getTimestamp().isEqual(o2.getTimestamp())) {
            int priceComparison = o1.getPrice().compareTo(o2.getPrice());
            if(priceComparison == 0){
                if(o1.getSide().equals(o2.getSide())){
                    return 0;
                } else if(o1.getSide().equals(Side.SELL)){
                    return 1;
                } else if(o1.getSide().equals(Side.BUY) && o2.getSide().equals(Side.REDUCE)){
                    return 1;
                } else return -1;
            }
            return priceComparison;
        } else if(o1.getTimestamp().isAfter(o2.getTimestamp())) return 1;
        else return -1;
    }
}
