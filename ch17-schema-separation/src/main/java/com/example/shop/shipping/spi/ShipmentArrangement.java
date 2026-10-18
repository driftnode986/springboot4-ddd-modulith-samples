package com.example.shop.shipping.spi;

public interface ShipmentArrangement {

    ShipmentId arrange(String orderId, String address);
}
