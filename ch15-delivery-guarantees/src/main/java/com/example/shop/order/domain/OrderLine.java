package com.example.shop.order.domain;

// #region order-line
import java.math.BigDecimal;

public record OrderLine(ProductId productId, Quantity quantity,
        Money unitPrice) {

    public OrderLine {
        if (productId == null || quantity == null
                || unitPrice == null) {
            throw new IllegalArgumentException(
                    "商品・数量・単価は必須です");
        }
    }

    /** この明細の金額。単価に数量を掛けたもの。 */
    public Money subtotal() {
        return new Money(
                unitPrice.amount().multiply(
                        BigDecimal.valueOf(quantity.value())),
                unitPrice.currency());
    }
}
// #endregion
