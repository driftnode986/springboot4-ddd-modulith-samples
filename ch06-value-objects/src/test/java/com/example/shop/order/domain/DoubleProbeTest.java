package com.example.shop.order.domain;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class DoubleProbeTest {

    @Test
    void 実測() {
        System.out.println("[probe] 0.1+0.2       = " + (0.1 + 0.2));
        double total = 0.0;
        for (int i = 0; i < 10; i++) {
            total += 0.1;
        }
        System.out.println("[probe] 0.1 x 10      = " + total);
        System.out.println("[probe] == 1.0        = " + (total == 1.0));
        double price = 1980.0 * 0.1;
        System.out.println("[probe] 1980*0.1      = " + price);
        System.out.println("[probe] BigDecimal    = "
                + new BigDecimal("0.1").add(new BigDecimal("0.2")));
        System.out.println("[probe] Money toString= " + Money.yen(1200));
        System.out.println("[probe] Email toString= " + new Email("a@example.com"));
    }
}
