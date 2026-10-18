package com.example.shop.order.web;

/** HTTP で返す形。集約をそのまま返さないので、内部を変えても応答は変わらない。 */
public record PlaceOrderResponse(String orderId) {}
