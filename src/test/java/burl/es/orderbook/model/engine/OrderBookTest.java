package burl.es.orderbook.model.engine;

import burl.es.orderbook.model.exceptions.OrderNotFoundException;
import burl.es.orderbook.model.order.Order;
import burl.es.orderbook.model.order.Reduce;
import burl.es.orderbook.model.order.Side;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class OrderBookTest {

    OrderBook book;
    Order sell10;
    Order buy10;
    Order[] sell1;
    Order[] buy1;

    static ZonedDateTime NOW = ZonedDateTime.now();
    static BigDecimal ZERO = BigDecimal.ZERO;
    static BigDecimal ONE = BigDecimal.ONE;
    static BigDecimal TEN = BigDecimal.TEN;
    static BigDecimal ONE_HUNDRED = new BigDecimal("100.00");

    private void setUpFor(int len, int price, int size){
        setUpFor(len, price, size,false,false, true);
    }

    private void setUpFor(int len, int price, int size, boolean descendSells, boolean descendBuys){
        setUpFor(len,price,size,descendSells,descendBuys,false);
    }

    private void setUpFor(int len, int price, int size, boolean descendSells, boolean descendBuys, boolean samePrice){
        book = new OrderBook();
        sell1 = new Order[len];
        buy1 = new Order[len];
        for(int i = 0; i < len; i++) {
            int s = i+1, b = i+1;
            if(!samePrice) {
                if (descendSells) s = len - i;
                if (descendBuys) b = len - i;
            } else {
                s = 1;
                b = 1;
            }
            sell1[i] = new Order(ZonedDateTime.now(), "s" + i, Side.SELL, new BigDecimal(s*price), new BigDecimal(size));
            buy1[i] = new Order(ZonedDateTime.now(), "b" + i, Side.BUY, new BigDecimal(b*price), new BigDecimal(size));
        }
    }

    @Test
    void fillBigOrderInstantly() {
        int len = 10, price = 10, size = 1;
        setUpFor(len,price,size);
        OrderBook b1 = new OrderBook();
        OrderBook b2 = new OrderBook();
        sell10 = new Order(NOW, "s", Side.SELL, new BigDecimal(price), new BigDecimal("12.00"));
        buy10 = new Order(NOW, "b", Side.BUY, new BigDecimal(price), new BigDecimal("12.00"));
        for(int i = 0; i < len; i++){
            assert sell10.getFill().compareTo(ZERO) == 0;
            assert buy10.getFill().compareTo(ZERO) == 0;
            b1.addOrder(buy1[i]);
            b2.addOrder(sell1[i]);
            assert sell1[i].getFillPercentage().compareTo(ZERO) == 0;
            assert buy1[i].getFillPercentage().compareTo(ZERO) == 0;
            assert !sell10.isFilled();
            assert !buy10.isFilled();
        }
        b1.addOrder(sell10);
        b2.addOrder(buy10);
        for(int i = 0; i < len; i++){
            assert sell1[i].isFilled(); //add big orders to book with preexisting opposite sides will fill instantly
            assert buy1[i].isFilled();
        }
        BigDecimal eleven = new BigDecimal("11.00");
        Order b11 = new Order(NOW, "b11", Side.BUY, new BigDecimal(price), new BigDecimal("1.00"));
        Order s11 = new Order(NOW, "s11", Side.SELL, new BigDecimal(price), new BigDecimal("1.00"));
        b1.addOrder(b11);
        b2.addOrder(s11);
        assert !sell10.isFilled();
        assert !buy10.isFilled();
        assert sell10.getFill().compareTo(eleven) == 0;
        assert buy10.getFill().compareTo(eleven) == 0;
        assert b11.isFilled();
        assert s11.isFilled();
        Reduce rs1 = new Reduce(NOW, "rs1","s",new BigDecimal("2.00")); // A reduce with size 2 will only reduce 1
        Reduce rb1 = new Reduce(NOW, "rb1","b",new BigDecimal("2.00")); // as that is the remainder
        try {
            b1.reduce(rs1);
            b2.reduce(rb1);
        } catch (OrderNotFoundException e) {
            fail();
        }
        assert sell10.isFilled();
        assert buy10.isFilled();
        assert sell10.getFill().compareTo(eleven) == 0;
        assert buy10.getFill().compareTo(eleven) == 0;
        assert sell10.getSize().compareTo(eleven) == 0;
        assert buy10.getSize().compareTo(eleven) == 0;
        assert rs1.isFilled();
        assert rb1.isFilled();
    }

    @Test
    void fillBigOrderIteratively(){
        int len = 10, price = 10, size = 1;
        setUpFor(len,price,size);
        OrderBook b1 = new OrderBook();
        OrderBook b2 = new OrderBook();
        sell10 = new Order(NOW, "s", Side.SELL, new BigDecimal(price), new BigDecimal("12.00"));
        buy10 = new Order(NOW, "b", Side.BUY, new BigDecimal(price), new BigDecimal("12.00"));
        b1.addOrder(sell10);
        b2.addOrder(buy10);
        for(int i = 0; i < len; i++){
            assert sell10.getFill().compareTo(new BigDecimal(i * size)) == 0;
            assert buy10.getFill().compareTo(new BigDecimal(i * size)) == 0;
            b1.addOrder(buy1[i]);
            b2.addOrder(sell1[i]);

            assert sell1[i].getFillPercentage().compareTo(ONE_HUNDRED) == 0;
            assert buy1[i].getFillPercentage().compareTo(ONE_HUNDRED) == 0;
            assert !sell10.isFilled();
            assert !buy10.isFilled();
        }
        Reduce rs1 = new Reduce(NOW, "rs1","s",new BigDecimal("1.00"));
        Reduce rb1 = new Reduce(NOW, "rb1","b",new BigDecimal("1.00"));
        try {
            b1.reduce(rs1);
            b2.reduce(rb1);
        } catch (OrderNotFoundException e) {
            fail();
        }
        assert !sell10.isFilled();
        assert !buy10.isFilled();
        assert sell10.getFill().compareTo(TEN) == 0;
        assert buy10.getFill().compareTo(TEN) == 0;
        assert sell10.getSize().compareTo(new BigDecimal("11.00")) == 0;
        assert buy10.getSize().compareTo(new BigDecimal("11.00")) == 0;
        assert rs1.isFilled();
        assert rb1.isFilled();
        Order b11 = new Order(NOW, "b11", Side.BUY, new BigDecimal(price), new BigDecimal("2.00"));
        Order s11 = new Order(NOW, "s11", Side.SELL, new BigDecimal(price), new BigDecimal("2.00"));
        b1.addOrder(b11);
        b2.addOrder(s11);
        assert sell10.isFilled();
        assert buy10.isFilled();
        assert sell10.getFill().compareTo(new BigDecimal("11.00")) == 0;
        assert buy10.getFill().compareTo(new BigDecimal("11.00")) == 0;
        assert sell10.getSize().compareTo(new BigDecimal("11.00")) == 0;
        assert buy10.getSize().compareTo(new BigDecimal("11.00")) == 0;
        assert !b11.isFilled();
        assert !s11.isFilled();
        assert b11.getFill().compareTo(ONE) == 0;
        assert s11.getFill().compareTo(ONE) == 0;
    }

    @Test
    void sellLowBuyHigh(){
        int len = 10, price = 10, size = 1;
        setUpFor(len,price,size, false, true);
        for(int i = 0; i < len; i++){
            book.addOrder(buy1[i]);
            book.addOrder(sell1[i]); // adding low sells and high buys first so the first half sell and the next cannot find fills
        }
        for(int i = 0; i <  len; i++){
            if(i < 5) {
                assert sell1[i].getFillPercentage().compareTo(ONE_HUNDRED) ==  0;
                assert buy1[i].getFillPercentage().compareTo(ONE_HUNDRED) ==  0;
                int cost = (len - i) * price;
                assert sell1[i].getCost().compareTo(new BigDecimal(cost)) ==  0;
                assert buy1[i].getCost().compareTo(new BigDecimal(cost)) ==  0;
            } else {
                assert sell1[i].getFillPercentage().compareTo(ZERO) ==  0;
                assert buy1[i].getFillPercentage().compareTo(ZERO) ==  0;
                assert sell1[i].getCost().compareTo(ZERO) ==  0;
                assert buy1[i].getCost().compareTo(ZERO) ==  0;
            }
        }
        setUpFor(len,price,size, true, false);
        for(int i = 0; i < len; i++){
            book.addOrder(buy1[i]);
            book.addOrder(sell1[i]); // adding high sells and low buys first so all the orders fill immediately
        }
        for(int i = 0; i < len; i++){
            assert sell1[i].getFillPercentage().compareTo(ONE_HUNDRED) ==  0;
            assert buy1[i].getFillPercentage().compareTo(ONE_HUNDRED) ==  0;
            int cost = (len - i) * price;
            assert sell1[i].getCost().compareTo(new BigDecimal(cost)) ==  0;
        }
    }

    @Test
    void getOrders() {
        int len = 10, price = 10,size = 10;
        setUpFor(len,price,size, true, false);
        for(int i = 0; i < 8; i++){
            book.addOrder(buy1[i]);
            book.addOrder(sell1[i]);
        }
        ArrayList<Order> orders = book.getOrders();
        for(int i=0; i<len;i++){
            if(i<3){
                assert orders.contains(buy1[i]); // Will contain unfilled orders
                assert orders.contains(sell1[i]);
            } else if(i > 2 && i < 8){
                assert orders.contains(buy1[i]); // Will contain filled orders
                assert orders.contains(sell1[i]);
            } else {
                assert !orders.contains(buy1[i]); // Will not contain not added orders
                assert !orders.contains(sell1[i]);
            }
        }
    }

    @Test
    void getOrderById() {
        int len = 10, price = 10,size = 10;
        setUpFor(len,price,size, true, false);
        for(int i = 0; i < 8; i++){
            book.addOrder(buy1[i]);
            book.addOrder(sell1[i]);
        }
        for(int i=0; i<len;i++){
            if(i<3){
                try {
                    assert book.getOrderById(buy1[i].getOrderId()) == buy1[i]; // Will get unfilled orders
                    assert book.getOrderById(sell1[i].getOrderId()) == sell1[i];
                } catch (OrderNotFoundException e){
                    fail();
                }
            } else if(i > 2 && i < 8){
                try {
                    assert book.getOrderById(buy1[i].getOrderId()) == buy1[i]; // Will get filled orders
                    assert book.getOrderById(sell1[i].getOrderId()) == sell1[i];
                } catch (OrderNotFoundException e){
                    fail();
                }
            } else {
                int finalI = i; // Will not get not added orders
                assertThrows(OrderNotFoundException.class, () -> {
                    assert book.getOrderById(buy1[finalI].getOrderId()) == buy1[finalI];
                });
                assertThrows(OrderNotFoundException.class, () -> {
                    assert book.getOrderById(sell1[finalI].getOrderId()) == sell1[finalI];
                });
            }
        }
    }

    @Test
    void getFilledOrders() {
        int len = 10, price = 10,size = 10;
        setUpFor(len,price,size, true, false);
        for(int i = 0; i < 8; i++){
            book.addOrder(buy1[i]);
            book.addOrder(sell1[i]);
        }
        ArrayList<Order> orders = book.getFilledOrders();
        for(int i=0; i<len;i++){
            if(i<2){
                assert !orders.contains(buy1[i]); // Will not contain unfilled orders
                assert !orders.contains(sell1[i]);
            } else if(i > 1 && i < 8){
                assert orders.contains(buy1[i]); // Will contain filled orders
                assert orders.contains(sell1[i]);
            } else {
                assert !orders.contains(buy1[i]); // Will not contain not added orders
                assert !orders.contains(sell1[i]);
            }
        }
    }

    @Test
    void getOpenOrders() {
        int len = 10, price = 10,size = 10;
        setUpFor(len,price,size, true, false);
        for(int i = 0; i < 8; i++){
            book.addOrder(buy1[i]);
            book.addOrder(sell1[i]);
        }
        ArrayList<Order> orders = book.getOpenOrders();
        for(int i=0; i<len;i++){
            if(i<2){
                assert orders.contains(buy1[i]); // Will contain unfilled orders
                assert orders.contains(sell1[i]);
            } else if(i > 1 && i < 8){
                assert !orders.contains(buy1[i]); // Will not contain filled orders
                assert !orders.contains(sell1[i]);
            } else {
                assert !orders.contains(buy1[i]); // Will not contain not added orders
                assert !orders.contains(sell1[i]);
            }
        }
    }

    @Test
    void getSellOrders() {
        int len = 10, price = 10,size = 10;
        setUpFor(len,price,size, true, false);
        for(int i = 0; i < 8; i++){
            book.addOrder(buy1[i]);
            book.addOrder(sell1[i]);
        }
        ArrayList<Order> orders = book.getSellOrders();
        for(int i=0; i<len;i++){
            assert !orders.contains(buy1[i]); // Will not contain any buy orders
            if(i<2){
                assert orders.contains(sell1[i]); // Will contain unfilled sells
            } else {
                assert !orders.contains(sell1[i]); // Will not contain filled buys
            }
        }
    }

    @Test
    void getBuyOrders() {
        int len = 10, price = 10,size = 10;
        setUpFor(len,price,size, true, false);
        for(int i = 0; i < 8; i++){
            book.addOrder(buy1[i]);
            book.addOrder(sell1[i]);
        }
        ArrayList<Order> orders = book.getBuyOrders();
        for(int i=0; i<len;i++){
            assert !orders.contains(sell1[i]); // Will not contain any sell orders
            if(i<2){
                assert orders.contains(buy1[i]); // Will contain unfilled buys
            } else {
                assert !orders.contains(buy1[i]); // Will not contain filled buys
            }
        }
    }

    /*TODO:
    *  Test for ZonedDateTime stuff
    *  Test reduces */
}