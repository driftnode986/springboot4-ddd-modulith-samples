package com.example.shop.inventory.application;

import com.example.shop.inventory.spi.StockReservation;
import org.springframework.stereotype.Service;

@Service
class ReserveStockService implements StockReservation {

    @Override
    public void reserve(String sku, int quantity) {
        // 在庫の引き当ては第9章で実装します。
        // ここでは order から見える窓口が spi だけであることの確認に使います。
    }
}
