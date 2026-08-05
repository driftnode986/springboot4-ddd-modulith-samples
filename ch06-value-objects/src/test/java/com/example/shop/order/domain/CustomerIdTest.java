package com.example.shop.order.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class CustomerIdTest {

    @Test
    @DisplayName("値があれば生成できる")
    void 値があれば生成できる() {
        assertThat(new CustomerId("C-1").value()).isEqualTo("C-1");
    }

    @ParameterizedTest
    @DisplayName("空の ID は生成できない")
    @ValueSource(strings = {"", " ", "　"})
    void 空のIDは生成できない(String invalid) {
        assertThatThrownBy(() -> new CustomerId(invalid))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
