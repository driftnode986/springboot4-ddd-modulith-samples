package com.example.shop.order.application;

import java.util.List;

/**
 * 注文を受け付けるユースケースへの入力。
 * ここに現れるのは外から渡される値であって、ドメインモデルではない。
 */
public record PlaceOrderCommand(String customerId, String address, List<Line> lines) {

    public PlaceOrderCommand {
        lines = lines == null ? List.of() : List.copyOf(lines);
    }

    /** 明細1行ぶんの入力。単価は円の整数で受け取る。 */
    public record Line(String productId, int quantity, long unitPriceYen) {}
}
