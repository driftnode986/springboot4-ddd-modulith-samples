package com.example.shop.order.domain;

// #region quantity
public record Quantity(int value) {

    public Quantity {
        if (value < 1) {
            throw new IllegalArgumentException(
                    "数量は 1 以上です: " + value);
        }
    }
}
// #endregion
