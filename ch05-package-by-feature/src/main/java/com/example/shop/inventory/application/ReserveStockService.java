package com.example.shop.inventory.application;

import com.example.shop.order.spi.OrderPlaced;
import org.springframework.stereotype.Service;

@Service
public class ReserveStockService {

    public void on(OrderPlaced event) {
        // 第14章でドメインイベントとして受け取る形に置き換えます。
        // ここでは order の公開面だけを参照できることの確認に使います。
    }
}
