package com.example.shop.shipping.domain;

import com.example.shop.shipping.spi.ShipmentId;

/** 出荷。注文が確定したあとに1回だけ作られる。 */
public class Shipment {

    private final ShipmentId id;
    private final String orderId;
    private final String address;

    public Shipment(ShipmentId id, String orderId, String address) {
        if (address == null || address.isBlank()) {
            throw new IllegalArgumentException("配送先が指定されていません");
        }
        this.id = id;
        this.orderId = orderId;
        this.address = address;
    }

    public ShipmentId id() {
        return id;
    }

    public String orderId() {
        return orderId;
    }

    public String address() {
        return address;
    }
}
