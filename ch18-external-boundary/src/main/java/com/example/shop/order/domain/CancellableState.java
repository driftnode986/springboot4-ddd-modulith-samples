package com.example.shop.order.domain;

import java.time.Instant;

// #region cancellable
public sealed interface CancellableState
        permits OrderState.Accepted, OrderState.Reserved,
                OrderState.Paid {

    OrderState.Cancelled cancel(Instant cancelledAt,
            String reason);
}
// #endregion
