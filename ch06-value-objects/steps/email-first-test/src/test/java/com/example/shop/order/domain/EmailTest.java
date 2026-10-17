package com.example.shop.order.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class EmailTest {

    @Test
    @DisplayName("正しい形式なら生成できる")
    void 正しい形式なら生成できる() {
        assertThat(new Email("taguchi@example.com").value())
                .isEqualTo("taguchi@example.com");
    }
}
