package com.example.shop.inventory.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class StockTest {

    @Test
    @DisplayName("手元にある数より多くは引き当てられない")
    void rejectsReservationBeyondQuantity() {
        Stock stock = new Stock("P-1", 3);

        assertThatThrownBy(() -> stock.reserve(4))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("在庫が足りません");

        assertThat(stock.quantity()).isEqualTo(3);
    }

    @Test
    @DisplayName("手元にある数までは引き当てられる")
    void reservesUpToQuantity() {
        Stock stock = new Stock("P-1", 3);

        stock.reserve(3);

        assertThat(stock.quantity()).isZero();
    }
}
