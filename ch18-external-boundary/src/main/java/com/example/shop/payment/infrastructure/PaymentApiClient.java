package com.example.shop.payment.infrastructure;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.PostExchange;

/**
 * 決済代行会社の HTTP API をそのまま写した宣言。
 *
 * <p>ここに出てくる語は相手側のものであり、支払いの語彙ではない。 相手の語を内側へ持ち込まないための変換は {@link HttpPaymentGateway} が行う。
 */
// #region payment-api-client
interface PaymentApiClient {

    @PostExchange("/v1/charges")
    ChargeApiResponse charge(
            @RequestBody ChargeApiRequest request);
}
// #endregion
