package com.example.shop.order.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class QuantityTest {

    @Test
    @DisplayName("1以上なら生成できる")
    void 一以上なら生成できる() {
        assertThat(new Quantity(1).value()).isEqualTo(1);
    }

    @ParameterizedTest
    @DisplayName("数量は1以上")
    @ValueSource(ints = {0, -1})
    void 数量は1以上(int invalid) {
        assertThatThrownBy(() -> new Quantity(invalid))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
