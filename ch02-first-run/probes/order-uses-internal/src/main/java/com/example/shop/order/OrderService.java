package com.example.shop.order;

import com.example.shop.inventory.internal.StockRepository;

public class OrderService {

    private final StockRepository stockRepository;

    public OrderService(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

    public void place(Order order) {
        stockRepository.decrement(
                order.sku(), order.quantity());
    }
}
