package com.example.shop.order.domain;

public final class PlaceOrder {

    public void placeOrder(CustomerId customerId,
            ProductId productId, Quantity quantity) {
    }

    void 取り違えても気づけない() {
        // #region mix-up
        CustomerId customer = new CustomerId("C-1");
        ProductId product = new ProductId("P-1");

        placeOrder(product, customer, new Quantity(1));
        // #endregion
    }
}
