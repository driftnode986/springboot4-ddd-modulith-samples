package com.example.shop.order.web;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.shop.order.application.PlaceOrderCommand;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PlaceOrderRequestTest {

    @Test
    @DisplayName("受け取った形を、項目を変えずにユースケースへの入力に移し替える")
    void convertsToCommand() {
        PlaceOrderRequest request = new PlaceOrderRequest(
                "C-001",
                List.of(new PlaceOrderRequest.Line(
                        "P-001", 2, 1500)));

        assertThat(request.toCommand())
                .isEqualTo(new PlaceOrderCommand(
                        "C-001",
                        List.of(new PlaceOrderCommand.Line(
                                "P-001", 2, 1500))));
    }

    @Test
    @DisplayName("明細が無い要求は、明細0件の入力になる")
    void nullLinesBecomeEmpty() {
        assertThat(new PlaceOrderRequest("C-001", null)
                        .toCommand().lines())
                .isEmpty();
    }
}
