package com.example.shop.order.application;

// #region command
import java.util.List;

public record PlaceOrderCommand(
        String customerId, List<Line> lines) {

    public PlaceOrderCommand {
        lines = lines == null ? List.of() : List.copyOf(lines);
    }

    public record Line(
            String productId,
            int quantity,
            long unitPriceYen) {}
}
// #endregion
