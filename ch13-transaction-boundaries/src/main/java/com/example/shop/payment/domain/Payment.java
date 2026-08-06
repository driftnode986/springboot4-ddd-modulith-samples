package com.example.shop.payment.domain;

import java.util.UUID;

public class Payment {

    private final UUID orderId;

    public Payment(UUID orderId) {
        this.orderId = orderId;
    }

    public UUID orderId() {
        return orderId;
    }
}
