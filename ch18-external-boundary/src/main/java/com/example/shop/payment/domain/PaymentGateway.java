package com.example.shop.payment.domain;

import java.util.UUID;

/**
 * 請求を実行する相手を表す。
 *
 * <p>この宣言には、決済代行会社の名前も、HTTP という語も出てこない。 支払いの都合で決まるのは「注文の識別子と金額を渡すと、
 * 支払いの語彙で結果が返る」ところまでで、 それをどう実現するかは infrastructure が引き受ける。
 */
// #region payment-gateway
public interface PaymentGateway {

    ChargeResult charge(UUID orderId, long amountYen);
}
// #endregion
