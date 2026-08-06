package com.example.shop.payment.spi;

import java.util.UUID;

public interface PaymentRequest {

    void request(UUID orderId);
}
