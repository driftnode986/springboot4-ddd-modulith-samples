package com.example.shop.order.domain;

import com.example.shop.order.domain.OrderState.Accepted;
import java.time.Instant;

class ProbeIllegalTransition {
    void probe() {
        // #region call
        Accepted accepted = new Accepted(Instant.now());
        accepted.pay(new PaymentId("PAY-1"));
        // #endregion
    }
}
