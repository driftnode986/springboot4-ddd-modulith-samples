package com.example.shop.order.domain;

import com.example.shop.order.domain.OrderState.Accepted;
import com.example.shop.order.domain.OrderState.Confirmed;
import java.time.Instant;

class ProbeCancelConfirmed {

    // #region cancel-order
    void cancelOrder(CancellableState state) {
        state.cancel(Instant.now(), "顧客都合");
    }
    // #endregion

    void probe() {
        // #region call
        Confirmed confirmed = new Accepted(Instant.now())
                .reserve(new ReservationId("R-1"))
                .pay(new PaymentId("PAY-1"))
                .confirm(Instant.now());
        cancelOrder(confirmed);
        // #endregion
    }
}
