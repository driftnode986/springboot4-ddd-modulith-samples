package com.example.shop.inventory;

import com.example.shop.inventory.internal.StockRepository;

public class InventoryService {

    private final StockRepository stockRepository;

    public InventoryService(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

    public void decrease(String sku, int quantity) {
        stockRepository.decrement(sku, quantity);
    }
}
