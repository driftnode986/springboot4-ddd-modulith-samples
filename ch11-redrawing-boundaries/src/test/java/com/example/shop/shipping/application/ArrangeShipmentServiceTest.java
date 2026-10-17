package com.example.shop.shipping.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.shop.shipping.spi.ShipmentArrangement;
import com.example.shop.shipping.spi.ShipmentId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ArrangeShipmentServiceTest {

    private final ShipmentArrangement shipments = new ArrangeShipmentService();

    @Test
    @DisplayName("出荷を手配すると、出荷の識別子が返る")
    void arrangesShipment() {
        ShipmentId id = shipments.arrange("O-001", "東京都千代田区1-1");

        assertThat(id.value()).isNotBlank();
    }

    @Test
    @DisplayName("配送先が空なら出荷を手配しない")
    void rejectsBlankAddress() {
        assertThatThrownBy(() -> shipments.arrange("O-001", " "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("配送先が指定されていません");
    }
}
