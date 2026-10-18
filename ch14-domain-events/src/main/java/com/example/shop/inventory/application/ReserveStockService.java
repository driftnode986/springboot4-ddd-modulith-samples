package com.example.shop.inventory.application;

import com.example.shop.inventory.spi.StockReservation;
import org.springframework.stereotype.Service;

@Service
class ReserveStockService implements StockReservation {

    @Override
    public void reserve(String sku, int quantity) {
        // この窓口は中身を持たない。在庫の引き当ては、
        // 第14章でイベントを受け取る側に実装します。
    }
}
