package com.example.shop.order.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 注文。まとめて整合を保つ単位であり、外から触れてよい唯一の入口。
 * 明細はこの中にしか存在せず、追加も合計の計算もここを通る。
 */
public class Order {

    /** 1 注文で受け付けられる金額の上限。 */
    private static final Money LIMIT = Money.yen(500_000);

    private final OrderId id;
    private final CustomerId customerId;
    private final List<OrderLine> lines = new ArrayList<>();
    private OrderState state;

    private Order(OrderId id, CustomerId customerId, Instant acceptedAt) {
        this(id, customerId, new OrderState.Accepted(acceptedAt));
    }

    private Order(OrderId id, CustomerId customerId, OrderState state) {
        if (id == null || customerId == null) {
            throw new IllegalArgumentException("注文の識別子と顧客の識別子は必須です");
        }
        this.id = id;
        this.customerId = customerId;
        this.state = state;
    }

    /** 注文を受け付ける。業務として新しい注文が生まれる経路はこれだけ。 */
    public static Order place(OrderId id, CustomerId customerId, Instant acceptedAt) {
        return new Order(id, customerId, acceptedAt);
    }

    /**
     * 保存済みの注文を組み立て直す。業務上の出来事ではなく、
     * 保存したものを読み戻すためだけに使う。
     * 受付や明細の追加を業務の経路でやり直すと、規則を変えたあとに
     * 既存のデータが読めなくなるため、検査を通さずに組み立てる。
     */
    public static Order restore(
            OrderId id, CustomerId customerId, OrderState state, List<OrderLine> lines) {
        if (id == null || customerId == null || state == null || lines == null) {
            throw new IllegalArgumentException("読み戻しには識別子・顧客・状態・明細が必要です");
        }
        Order order = new Order(id, customerId, state);
        order.lines.addAll(lines);
        return order;
    }

    /** 明細を追加する。上限を超える追加は受け付けない。 */
    public void addLine(ProductId productId, Quantity quantity, Money unitPrice) {
        OrderLine line = new OrderLine(productId, quantity, unitPrice);
        Money next = total().plus(line.subtotal());
        if (next.amount().compareTo(LIMIT.amount()) > 0) {
            throw new IllegalStateException(
                    "1 注文の上限を超えます: " + next.amount() + " > " + LIMIT.amount());
        }
        lines.add(line);
    }

    /** 明細の合計金額。 */
    public Money total() {
        return lines.stream()
                .map(OrderLine::subtotal)
                .reduce(Money.yen(0), Money::plus);
    }

    /** 明細の一覧。呼び出し側で書き換えても注文の中身は変わらない。 */
    public List<OrderLine> lines() {
        return List.copyOf(lines);
    }

    public OrderId id() {
        return id;
    }

    public CustomerId customerId() {
        return customerId;
    }

    public OrderState state() {
        return state;
    }

    /** 同じ注文かどうかは識別子だけで決まる。明細や状態は見ない。 */
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        return other instanceof Order that && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
