package com.example.shop.order.spi;

import java.util.UUID;

public record OrderPlaced(UUID orderId, String customerId) {
}
