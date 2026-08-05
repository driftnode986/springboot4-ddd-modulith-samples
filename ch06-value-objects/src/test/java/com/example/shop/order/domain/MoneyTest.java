package com.example.shop.order.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.Currency;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MoneyTest {

    @Test
    @DisplayName("同じ通貨どうしは足せる")
    void 同じ通貨どうしは足せる() {
        assertThat(Money.yen(1200).plus(Money.yen(800)))
                .isEqualTo(Money.yen(2000));
    }

    @Test
    @DisplayName("通貨が違う金額は足せない")
    void 通貨が違う金額は足せない() {
        Money usd = new Money(BigDecimal.valueOf(10), Currency.getInstance("USD"));
        assertThatThrownBy(() -> Money.yen(1000).plus(usd))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("通貨が違う");
    }

    @Test
    @DisplayName("加算しても元の値は変わらない")
    void 加算しても元の値は変わらない() {
        Money base = Money.yen(1000);
        base.plus(Money.yen(500));
        assertThat(base).isEqualTo(Money.yen(1000));
    }
}
