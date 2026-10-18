package com.example.shop.order.domain;

import java.math.BigDecimal;

/**
 * 注文明細。注文の内側にだけ存在し、単独では意味を持たない。
 * 商品そのものではなく商品の識別子を持つ。
 */
public record OrderLine(ProductId productId, Quantity quantity, Money unitPrice) {

    public OrderLine {
        if (productId == null || quantity == null || unitPrice == null) {
            throw new IllegalArgumentException("商品・数量・単価は必須です");
        }
    }

    /** この明細の金額。単価に数量を掛けたもの。 */
    public Money subtotal() {
        return new Money(
                unitPrice.amount().multiply(BigDecimal.valueOf(quantity.value())),
                unitPrice.currency());
    }
}
