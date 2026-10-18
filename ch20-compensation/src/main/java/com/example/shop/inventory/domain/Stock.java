package com.example.shop.inventory.domain;

public class Stock {

    private final String sku;
    private int quantity;

    public Stock(String sku, int quantity) {
        this.sku = sku;
        this.quantity = quantity;
    }

    public String sku() {
        return sku;
    }

    public int quantity() {
        return quantity;
    }

    public void reserve(int amount) {
        if (amount > quantity) {
            throw new IllegalArgumentException("在庫が足りません: " + sku);
        }
        this.quantity -= amount;
    }

    /** 引き当てを取り消して手元に戻す。 */
    public void release(int amount) {
        this.quantity += amount;
    }
}
