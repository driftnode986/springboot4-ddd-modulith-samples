package com.example.shop.order.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.Currency;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 合計の比較が成り立つ条件を固定する確認用のテスト。
 * Money は record なので equals は BigDecimal.equals に委ねられ、
 * BigDecimal.equals は桁数まで見る。
 */
class MoneyScaleProbeTest {

    private static final Currency JPY = Currency.getInstance("JPY");

    @Test
    @DisplayName("Money.yen だけを通っていれば合計は等しく比較できる")
    void totalsCompareWhenAllValuesComeFromYen() {
        Money total = Money.yen(1000).plus(Money.yen(500));

        assertThat(total).isEqualTo(Money.yen(1500));
    }

    @Test
    @DisplayName("桁数の違う金額は、数値が同じでも等しくない")
    void scaleBreaksEquality() {
        Money plain = new Money(new BigDecimal("1500"), JPY);
        Money scaled = new Money(new BigDecimal("1500.00"), JPY);

        assertThat(plain).isNotEqualTo(scaled);
        assertThat(plain.amount().compareTo(scaled.amount())).isZero();
    }

    @Test
    @DisplayName("桁数は加算で伝わるので、混ざった時点から合計が比較できなくなる")
    void scalePropagatesThroughAddition() {
        Money mixed = new Money(new BigDecimal("1000.00"), JPY).plus(Money.yen(500));

        assertThat(mixed.amount()).isEqualByComparingTo(new BigDecimal("1500"));
        assertThat(mixed).isNotEqualTo(Money.yen(1500));
    }
}
