package com.example.shop.order.web;

import com.example.shop.order.application.PlaceOrderCommand;
import java.util.List;

public record PlaceOrderRequest(
        String customerId, List<Line> lines) {

    public record Line(
            String productId,
            int quantity,
            long unitPriceYen) {}

    public PlaceOrderCommand toCommand() {
        List<PlaceOrderCommand.Line> commandLines =
                (lines == null ? List.<Line>of() : lines)
                        .stream()
                        .map(l -> new PlaceOrderCommand.Line(
                                l.productId(),
                                l.quantity(),
                                l.unitPriceYen()))
                        .toList();
        return new PlaceOrderCommand(customerId, commandLines);
    }
}
