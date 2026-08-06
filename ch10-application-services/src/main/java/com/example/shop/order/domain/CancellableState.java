package com.example.shop.order.domain;

import java.time.Instant;

/**
 * 取り消せる状態にだけ実装させる。
 * この型を引数に取るメソッドは、確定済みの注文を受け取れない。
 */
public sealed interface CancellableState
        permits OrderState.Accepted, OrderState.Reserved, OrderState.Paid {

    OrderState.Cancelled cancel(Instant cancelledAt, String reason);
}
