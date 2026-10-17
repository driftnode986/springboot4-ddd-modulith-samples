package com.example.shop.order.domain.steps.nolimit;

import com.example.shop.order.domain.CustomerId;
import com.example.shop.order.domain.Money;
import com.example.shop.order.domain.OrderId;
import com.example.shop.order.domain.OrderLine;
import com.example.shop.order.domain.OrderState;
import com.example.shop.order.domain.ProductId;
import com.example.shop.order.domain.Quantity;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 第8章の途中の版の注文。上限金額の規則と lines() を入れる前。
 * 完成形は com.example.shop.order.domain.Order。
 */
// #region fields-place
public class Order {

    private final OrderId id;
    private final CustomerId customerId;
    private final List<OrderLine> lines = new ArrayList<>();
    private OrderState state;

    private Order(OrderId id, CustomerId customerId,
            Instant acceptedAt) {
        if (id == null || customerId == null) {
            throw new IllegalArgumentException(
                    "注文の識別子と顧客の識別子は必須です");
        }
        this.id = id;
        this.customerId = customerId;
        this.state = new OrderState.Accepted(acceptedAt);
    }

    /** 注文を受け付ける。注文を作る経路はこれだけ。 */
    public static Order place(OrderId id, CustomerId customerId,
            Instant acceptedAt) {
        return new Order(id, customerId, acceptedAt);
    }
// #endregion

    // #region add-line-total
    /** 明細を追加する。 */
    public void addLine(ProductId productId, Quantity quantity,
            Money unitPrice) {
        lines.add(
                new OrderLine(productId, quantity, unitPrice));
    }

    /** 明細の合計金額。 */
    public Money total() {
        return lines.stream()
                .map(OrderLine::subtotal)
                .reduce(Money.yen(0), Money::plus);
    }
    // #endregion

    // #region equals
    /**
     * 同じ注文かどうかは識別子だけで決まる。
     * 明細や状態は見ない。
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        return other instanceof Order that
                && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    // #endregion

    // #region accessors
    public OrderId id() {
        return id;
    }

    public CustomerId customerId() {
        return customerId;
    }

    public OrderState state() {
        return state;
    }
}
// #endregion
