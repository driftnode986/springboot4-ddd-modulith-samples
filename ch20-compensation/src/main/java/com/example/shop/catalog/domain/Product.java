package com.example.shop.catalog.domain;

public class Product {

    private final String sku;
    private final String name;

    public Product(String sku, String name) {
        this.sku = sku;
        this.name = name;
    }

    public String sku() {
        return sku;
    }

    public String name() {
        return name;
    }
}
