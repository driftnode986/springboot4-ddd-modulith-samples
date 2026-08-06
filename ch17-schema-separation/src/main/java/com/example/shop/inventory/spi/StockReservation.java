package com.example.shop.inventory.spi;

public interface StockReservation {

    void reserve(String sku, int quantity);
}
