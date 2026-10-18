package com.example.shop.before;

import static org.assertj.core.api.Assertions.assertThatCode;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PlaceOrderWithStringsTest {

    @Test
    @DisplayName("顧客と商品の ID を取り違えても、例外は出ない")
    void 取り違えても例外は出ない() {
        var orders = new PlaceOrderWithStrings();
        String customerId = "C-1";
        String productId = "P-1";

        assertThatCode(() -> orders.placeOrder(
                        productId, customerId, 1))
                .doesNotThrowAnyException();
    }
}
