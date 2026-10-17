package com.example.shop.order.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.shop.order.domain.Order;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class InMemoryOrderRepositoryTest {

    @Test
    @DisplayName("保存した注文を ID で取り出せる")
    void 保存した注文をIDで取り出せる() {
        var repository = new InMemoryOrderRepository();
        var order = new Order(UUID.randomUUID(), "C-1");

        repository.save(order);

        assertThat(repository.findById(order.id())).containsSame(order);
        assertThat(repository.findById(UUID.randomUUID())).isEmpty();
    }
}
