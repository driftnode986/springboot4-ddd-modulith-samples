package com.example.shop.order.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PlaceOrderCommandTest {

    @Test
    @DisplayName("渡した一覧をあとで変えても、受け取った入力は変わらない")
    void copiesLines() {
        List<PlaceOrderCommand.Line> lines = new ArrayList<>();
        lines.add(new PlaceOrderCommand.Line("P-001", 1, 100));
        PlaceOrderCommand command = new PlaceOrderCommand("C-001", lines);

        lines.add(new PlaceOrderCommand.Line("P-002", 1, 200));

        assertThat(command.lines()).hasSize(1);
    }

    @Test
    @DisplayName("明細が null なら空の一覧として受け取る")
    void nullLinesBecomeEmpty() {
        assertThat(new PlaceOrderCommand("C-001", null).lines()).isEmpty();
    }
}
