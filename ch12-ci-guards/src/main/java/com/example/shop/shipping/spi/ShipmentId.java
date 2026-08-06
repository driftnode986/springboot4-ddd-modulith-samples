package com.example.shop.shipping.spi;

/** 出荷の識別子。 */
public record ShipmentId(String value) {

    public ShipmentId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("出荷の識別子が空です");
        }
    }
}
