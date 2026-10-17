package com.example.shop.order.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class IdentifierTest {

    @Test
    @DisplayName("商品と注文の ID も空にはできない")
    void 商品と注文のIDも空にはできない() {
        assertThatThrownBy(() -> new ProductId(" "))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new OrderId(""))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("中身が同じでも、顧客の ID と商品の ID は別の型")
    void 中身が同じでも別の型() {
        Object customer = new CustomerId("X-1");
        Object product = new ProductId("X-1");

        assertThat(customer).isNotEqualTo(product);
    }
}
