package com.example.shop.before;

public final class PlaceOrderWithStrings {

    public void placeOrder(String customerId, String productId, int quantity) {
    }

    void 取り違えても気づけない() {
        String customerId = "C-1";
        String productId = "P-1";
        placeOrder(productId, customerId, 1);
    }
}
