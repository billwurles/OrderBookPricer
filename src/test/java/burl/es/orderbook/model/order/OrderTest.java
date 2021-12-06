package burl.es.orderbook.model.order;

import burl.es.orderbook.model.exceptions.IllegalOrderException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class OrderTest {

    Order sell10;
    Order buy10;
    Order[] sell1;
    Order[] buy1;

    static ZonedDateTime NOW = ZonedDateTime.now();
    static BigDecimal ZERO = BigDecimal.ZERO;
    static BigDecimal ONE = BigDecimal.ONE;
    static BigDecimal TEN = BigDecimal.TEN;
    static BigDecimal ONE_HUNDRED = new BigDecimal("100.00");

    @BeforeEach
    void setUp() {
        sell10 = new Order(NOW, "a", Side.SELL, ONE, TEN);
        buy10 = new Order(NOW, "b", Side.BUY, ONE, TEN);
        sell1 = new Order[10];
        buy1 = new Order[10];
        for(int i = 0; i < 10; i++){
            sell1[i] = new Order(ZonedDateTime.now(), "a"+i, Side.SELL, ONE, ONE);
            buy1[i] = new Order(ZonedDateTime.now(), "b"+i, Side.BUY, ONE, ONE);
        }
    }

    @Test
    void exceptionTests() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Order(NOW,"a",Side.SELL, new BigDecimal("-1"),ONE);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            new Order(NOW,"a",Side.SELL, ZERO, new BigDecimal("-1"));
        });
        assertThrows(IllegalArgumentException.class, () -> {
            new Order(NOW,"a",Side.SELL, ZERO, ZERO);
        });
        assertThrows(IllegalOrderException.class, () -> {
            sell10.fill(sell10);
        });
        assertThrows(IllegalOrderException.class, () -> {
            buy10.fill(buy10);
        });

    }

        @Test
    void fill() {
//        for(int i=0; i < buy1.length; i++){
//        assert sell10.getFillRemainder().compareTo(TEN.subtract(new BigDecimal(i))) == 0;
//        assert buy10.getFillRemainder().compareTo(TEN.subtract(new BigDecimal(i))) == 0;
//        sell10.fill(buy1[i]);
//        sell1[i].fill(buy10);
//        assert sell1[i].getFillRemainder().compareTo(ZERO) == 0;
//        assert buy1[i].getFillRemainder().compareTo(ZERO) == 0;
//        }
//        assert sell10.getFillRemainder().compareTo(ZERO) == 0;
//        assert buy10.getFillRemainder().compareTo(ZERO) == 0;
    }

    @Test
    void reduceOrder() {
    }

    @Test
    void isFilled() {
        for(int i=0; i < buy1.length; i++){
            assert (!sell10.isFilled());
            assert (!buy10.isFilled());
            assert (!sell1[i].isFilled());
            assert (!buy1[i].isFilled());
            sell10.fill(buy1[i]);
            sell1[i].fill(buy10);
            assert (sell1[i].isFilled());
            assert (buy1[i].isFilled());
        }
        assert (sell10.isFilled());
        assert (buy10.isFilled());
    }

    @Test
    void getFillPercentage() {
        for(int i=0; i < buy1.length; i++){
            assert sell10.getFillPercentage().compareTo(TEN.multiply(new BigDecimal(i))) == 0;
            sell10.fill(buy1[i]);
            assert buy1[i].getFillPercentage().compareTo(ONE_HUNDRED) == 0;
            log.debug("getFillPercentage s:{} b{}:{}",sell10.getFillPercentage(), i, buy1[i].getFillPercentage());
        }
        assert sell10.getFillPercentage().compareTo(ONE_HUNDRED) == 0;
    }

    @Test
    void getInteractedOrders() {
        assert sell10.getConsumedOrders().isEmpty();
        for(int i=0; i < buy1.length; i++) {
            sell10.fill(buy1[i]);
            for(int r=0; r <= i; r++) {
                log.debug("Checking contains {}",r);
                assert sell10.getConsumedOrders().contains(buy1[r]);
                assert buy1[r].getConsumedOrders().contains(sell10);
            }
        }
    }
}