package burl.es.orderbook.model.engine;

import burl.es.orderbook.model.OrderBookController;
import burl.es.orderbook.model.order.Order;
import burl.es.orderbook.model.order.Side;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class OrderBookTest {

    OrderBook book;
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
        book = new OrderBook();
        sell10 = new Order(NOW, "s", Side.SELL, ONE, TEN);
        buy10 = new Order(NOW, "b", Side.BUY, ONE, TEN);
        sell1 = new Order[10];
        buy1 = new Order[10];
        altSell1 = new Order[10];
        altBuy1 = new Order[10];
        for(int i = 0; i < 10; i++) {
            int r = 9 - i;
            altSell1[i] = new Order(ZonedDateTime.now(), "s" + i, Side.SELL, new BigDecimal(i), TEN);
            altBuy1[i] = new Order(ZonedDateTime.now(), "b" + i, Side.BUY, new BigDecimal(r), ONE);
        }
    }

    @Test
    void testWhatIWant(){
//        controller.addOrder(sell10);
        for(int i = 0; i < altBuy1.length; i++){
            book.addOrder(altBuy1[i]);
            book.addOrder(altSell1[i]);
        }
        for(int i = 0; i < altBuy1.length; i++){
        }
    }

    @Test
    void buy() {
    }

    @Test
    void sell() {
    }

    @Test
    void reduce() {
    }

    @Test
    void checkForFills() {
    }

    @Test
    void getOrderById() {
    }
}