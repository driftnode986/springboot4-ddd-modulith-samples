package com.example.shop.order;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.shop.inventory.internal.StockRepository;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

// probes の OrderService（内部の StockRepository を直接使う版）に合わせたテスト。
// src/ に写すと、このテストは通り、ModularityTests の verify() が落ちる。
class OrderServiceTest {

    @Test
    @DisplayName("注文を受けると在庫の数量を減らす")
    void 注文を受けると在庫の数量を減らす() {
        List<String> calls = new ArrayList<>();
        StockRepository repository = new StockRepository() {
            @Override
            public void decrement(String sku, int quantity) {
                calls.add(sku + " x " + quantity);
            }
        };

        new OrderService(repository).place(new Order("O-1", "SKU-1", 2));

        assertThat(calls).containsExactly("SKU-1 x 2");
    }
}
