package com.example.shop.shipping.spi;

/** 出荷モジュールが外へ見せる面。注文からはこの形でだけ呼べる。 */
public interface ShipmentArrangement {

    ShipmentId arrange(String orderId, String address);
}
