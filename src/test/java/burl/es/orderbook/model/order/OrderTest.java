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
        sell10 = new Order(NOW, "s", Side.SELL, ONE, TEN);
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
        assertThrows(IllegalArgumentException.class, () -> { // Can't have negative price
            new Order(NOW,"a",Side.SELL, new BigDecimal("-1"),ONE);
        });
        assertThrows(IllegalArgumentException.class, () -> { //Can't have neg size
            new Order(NOW,"a",Side.SELL, ZERO, new BigDecimal("-1"));
        });
        assertThrows(IllegalArgumentException.class, () -> { //Can't have zero size
            new Order(NOW,"a",Side.SELL, ZERO, ZERO);
        });
        assertThrows(IllegalOrderException.class, () -> { //Sell can't fill itself
            sell10.fill(sell10);
        });
        assertThrows(IllegalOrderException.class, () -> { // Buy can't fill itself
            buy10.fill(buy10);
        });
        assertThrows(IllegalOrderException.class, () -> { //Buy can't fill sell
            buy10.fill(sell10);
        });
        assertThrows(IllegalOrderException.class, () -> { //Buy can't fill sell
            sell10.fill(sell1[0]);
        });
        assertThrows(IllegalOrderException.class, () -> { //Can't fill same buy twice
            sell10.fill(buy1[0]);
            sell10.fill(buy1[0]);
        });
        assertThrows(IllegalOrderException.class, () -> { //Can't fill filled sell
            sell1[0].fill(buy10);
            sell1[0].fill(buy10);
        });
        assertThrows(IllegalOrderException.class, () -> { //Can't fill lower priced buy  - 1 < 0 == false
            sell1[1].fill(new Order(NOW,"bx",Side.BUY,BigDecimal.ZERO,BigDecimal.ONE));
        });
    }

    @Test
    void reduceOrder() {
        Reduce rs1 = new Reduce(NOW,"rs1","s3", new BigDecimal("0.5"));
        Reduce rb1 = new Reduce(NOW,"rb1","b3", new BigDecimal("0.5"));
        Reduce rs2 = new Reduce(NOW,"rs2","s4", new BigDecimal("1"));
        Reduce rb2 = new Reduce(NOW,"rb2","b4", new BigDecimal("1"));
        for(int i=0; i < buy1.length; i++){
            assert (!sell10.isFilled());
            assert (!buy10.isFilled());
            assert (!sell1[i].isFilled());
            assert (!buy1[i].isFilled());
            if(i==3){
                rs1.reduce(sell1[i]);
                rb1.reduce(buy1[i]);
                assert (!sell1[i].isFilled());
                assert (!buy1[i].isFilled());
                assert (sell1[i].getSize().compareTo(new BigDecimal("0.5")) == 0);
                assert (buy1[i].getSize().compareTo(new BigDecimal("0.5")) == 0);
                assert (sell1[i].getFill().compareTo(new BigDecimal("0")) == 0);
                assert (buy1[i].getFill().compareTo(new BigDecimal("0")) == 0);
            } else if(i==4){
                rs2.reduce(sell1[i]);
                rb2.reduce(buy1[i]);
                assert (sell1[i].isFilled());
                assert (buy1[i].isFilled());
                assert (sell1[i].getSize().compareTo(new BigDecimal("0")) == 0);
                assert (buy1[i].getSize().compareTo(new BigDecimal("0")) == 0);
                assert (sell1[i].getFill().compareTo(new BigDecimal("0")) == 0);
                assert (buy1[i].getFill().compareTo(new BigDecimal("0")) == 0);
            } else if(i > 4){
                int finalI = i;
                assertThrows(IllegalOrderException.class, () -> { //Can't fill same reduce twice
                    rs2.reduce(sell1[finalI]);
                });
                assertThrows(IllegalOrderException.class, () -> { //Can't fill same reduce twice
                    rb2.reduce(sell1[finalI]);
                });
            }
            if(i == 4){
                int finalI = i;
                assertThrows(IllegalOrderException.class, () -> { //Can't fill fully reduced order
                    sell10.fill(buy1[finalI]);
                });
                assertThrows(IllegalOrderException.class, () -> { //Can't fill fully reduced order
                    sell1[finalI].fill(buy10);
                });
            } else {
                sell10.fill(buy1[i]);
                sell1[i].fill(buy10);
                assert (sell1[i].isFilled());
                assert (buy1[i].isFilled());
            }
        }
        assert !sell10.isFilled();
        assert !buy10.isFilled();
        assert (sell10.getFill().compareTo(new BigDecimal("8.5")) == 0);
        assert (buy10.getFill().compareTo(new BigDecimal("8.5")) == 0);
        assert (sell10.getSize().compareTo(new BigDecimal("10")) == 0);
        assert (buy10.getSize().compareTo(new BigDecimal("10")) == 0);
        Reduce rs3 = new Reduce(NOW,"rs2","s", new BigDecimal("1"));
        Reduce rb3 = new Reduce(NOW,"rb2","b", new BigDecimal("1"));
        rs3.reduce(sell10);
        rb3.reduce(buy10);
        assert !sell10.isFilled();
        assert !buy10.isFilled();
        assert (sell10.getFill().compareTo(new BigDecimal("8.5")) == 0);
        assert (buy10.getFill().compareTo(new BigDecimal("8.5")) == 0);
        assert (sell10.getSize().compareTo(new BigDecimal("9")) == 0);
        assert (buy10.getSize().compareTo(new BigDecimal("9")) == 0);
        Reduce rs4 = new Reduce(NOW,"rs2","s", new BigDecimal("1")); //Reducing more than fill remainder
        Reduce rb4 = new Reduce(NOW,"rb2","b", new BigDecimal("1")); //will only reduce the remainder
        rs4.reduce(sell10);
        rb4.reduce(buy10);
        log.debug("{}:\n{}\n{}", 1, buy10, sell10);
        assert sell10.isFilled();
        assert buy10.isFilled();
        assert (sell10.getFill().compareTo(new BigDecimal("8.5")) == 0);
        assert (buy10.getFill().compareTo(new BigDecimal("8.5")) == 0);
        assert (sell10.getSize().compareTo(new BigDecimal("8.5")) == 0);
        assert (buy10.getSize().compareTo(new BigDecimal("8.5")) == 0);
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