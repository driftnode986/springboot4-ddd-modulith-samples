package com.example.shop.order.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** DB から戻る桁数の確認用。本文の主張の裏づけとして置く。 */
class MoneyScaleFromDbProbeTest {

    @Test
    @DisplayName("桁数が違う同じ金額は等しくないが比較すると同じ")
    void scaleBreaksEqualityButNotComparison() {
        BigDecimal written = BigDecimal.valueOf(1500);
        BigDecimal readBack = new BigDecimal("1500.00");

        assertThat(written.equals(readBack)).isFalse();
        assertThat(written.compareTo(readBack)).isZero();
        assertThat(written.scale()).isZero();
        assertThat(readBack.scale()).isEqualTo(2);
    }
}
