package com.example.shop.order;

import com.example.shop.inventory.InventoryService;

public class OrderService {

    private final InventoryService inventoryService;

    public OrderService(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    public void place(Order order) {
        inventoryService.decrease(order.sku(), order.quantity());
    }
}
