package com.example.shop.order.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class EmailTest {

    @Test
    @DisplayName("正しい形式なら生成できる")
    void 正しい形式なら生成できる() {
        assertThat(new Email("taguchi@example.com").value())
                .isEqualTo("taguchi@example.com");
    }

    @ParameterizedTest
    @DisplayName("不正な形式は生成できない")
    @ValueSource(strings = {"", " ", "taguchi", "taguchi@", "@example.com", "a b@example.com"})
    void 不正な形式は生成できない(String invalid) {
        assertThatThrownBy(() -> new Email(invalid))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("同じ値なら等しい")
    void 同じ値なら等しい() {
        assertThat(new Email("a@example.com")).isEqualTo(new Email("a@example.com"));
        assertThat(new Email("a@example.com")).hasSameHashCodeAs(new Email("a@example.com"));
    }
}
