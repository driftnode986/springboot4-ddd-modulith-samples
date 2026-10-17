package com.example.shop.payment.application;

import com.example.shop.catalog.domain.Product;

public class PaymentCatalogPeek {

    public String describe(Product product) {
        return product.name();
    }
}
