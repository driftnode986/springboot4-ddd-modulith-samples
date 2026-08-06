package com.example.shop.order.web;

import com.example.shop.order.application.PlaceOrderCommand;
import java.util.List;

/** HTTP で受け取る形。ドメインモデルではないので、外の都合で変わってよい。 */
public record PlaceOrderRequest(String customerId, List<Line> lines) {

    public record Line(String productId, int quantity, long unitPriceYen) {}

    /** 受け取った形をユースケースへの入力に移し替える。 */
    public PlaceOrderCommand toCommand() {
        List<PlaceOrderCommand.Line> commandLines =
                (lines == null ? List.<Line>of() : lines).stream()
                        .map(l -> new PlaceOrderCommand.Line(
                                l.productId(), l.quantity(), l.unitPriceYen()))
                        .toList();
        return new PlaceOrderCommand(customerId, commandLines);
    }
}
