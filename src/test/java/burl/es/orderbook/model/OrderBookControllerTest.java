package burl.es.orderbook.model;

import burl.es.orderbook.model.order.Order;
import burl.es.orderbook.model.order.Side;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;

class OrderBookControllerTest {

    OrderBookController controller;
    Order sell10;
    Order buy10;
    Order[] sell1;
    Order[] buy1;
    Order[] altSell1;
    Order[] altBuy1;

    static ZonedDateTime NOW = ZonedDateTime.now();
    static BigDecimal ZERO = BigDecimal.ZERO;
    static BigDecimal ONE = BigDecimal.ONE;
    static BigDecimal TEN = BigDecimal.TEN;
    static BigDecimal ONE_HUNDRED = new BigDecimal("100.00");

    @BeforeEach
    void setUp() {
        controller = new OrderBookController();
        sell10 = new Order(NOW, "a", Side.SELL, ONE, TEN);
        buy10 = new Order(NOW, "b", Side.BUY, ONE, TEN);
        sell1 = new Order[10];
        buy1 = new Order[10];
        for(int i = 0; i < 10; i++) {
            BigDecimal bigI = new BigDecimal(i);
            altSell1[i] = new Order(ZonedDateTime.now(), "a" + i, Side.SELL, bigI, ONE);
            altBuy1[i] = new Order(ZonedDateTime.now(), "b" + i, Side.BUY, TEN.subtract(bigI), ONE);
        }

    }

    @Test
    void testWhatIWant(){
//        controller.addOrder(sell10);
        for(int i = 0; i < altBuy1.length; i++){
//            controller.addOrder(altBuy1[i]);
//            controller.addOrder(altSell1[i]);
        }
    }

    @Test
    void getOpenOrders() {
    }

    @Test
    void getAllOrders() {
    }

    @Test
    void getFilledOrders() {
    }

    @Test
    void getAllSells() {
    }

    @Test
    void getAllBuys() {
    }

    @Test
    void addOrder() {
    }

    @Test
    void reduceOrder() {
    }

    @Test
    void findById() {
    }
}