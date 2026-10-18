package com.example.shop.payment.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ChargeResultTest {

    @Test
    @DisplayName("請求の結果は、成立・与信不可・不明の3つだけ")
    void hasThreeOutcomes() {
        assertThat(ChargeResult.values()).containsExactly(
                ChargeResult.SETTLED,
                ChargeResult.DECLINED,
                ChargeResult.UNKNOWN);
    }
}
