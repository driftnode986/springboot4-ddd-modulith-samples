package com.example.shop.payment.domain;

import java.util.UUID;

/** 決まった結果だけを返す差し替え用の実装。ネットワークには出ない。 */
public class StubPaymentGateway implements PaymentGateway {

    private final ChargeResult result;

    public StubPaymentGateway(ChargeResult result) {
        this.result = result;
    }

    @Override
    public ChargeResult charge(UUID orderId, long amountYen) {
        return result;
    }
}
