package com.example.shop.order;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.shop.inventory.InventoryService;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class OrderServiceTest {

    @Test
    @DisplayName("注文を受けると在庫の入り口に商品と数量を渡す")
    void 注文を受けると在庫の入り口に商品と数量を渡す() {
        List<String> calls = new ArrayList<>();
        InventoryService inventory = new InventoryService(null) {
            @Override
            public void decrease(String sku, int quantity) {
                calls.add(sku + " x " + quantity);
            }
        };

        new OrderService(inventory).place(new Order("O-1", "SKU-1", 2));

        assertThat(calls).containsExactly("SKU-1 x 2");
    }
}
