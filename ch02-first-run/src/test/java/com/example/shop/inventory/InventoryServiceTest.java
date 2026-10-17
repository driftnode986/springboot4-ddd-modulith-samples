package com.example.shop.inventory;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.shop.inventory.internal.StockRepository;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class InventoryServiceTest {

    @Test
    @DisplayName("在庫を減らす依頼を内部のリポジトリに渡す")
    void 在庫を減らす依頼を内部のリポジトリに渡す() {
        List<String> calls = new ArrayList<>();
        StockRepository repository = new StockRepository() {
            @Override
            public void decrement(String sku, int quantity) {
                calls.add(sku + " x " + quantity);
            }
        };

        new InventoryService(repository).decrease("SKU-1", 3);

        assertThat(calls).containsExactly("SKU-1 x 3");
    }
}
