package com.example.shop.payment.application;

import java.util.UUID;

/**
 * 請求が通ったかどうかが確定していない。
 *
 * <p>成立とも不成立とも扱えない。不成立として在庫を戻すと、代金を受け取ったまま在庫も戻る
 * 場合がある。人が確認できるよう、確定していない事実として残す。
 */
public class PaymentUndeterminedException extends RuntimeException {

    public PaymentUndeterminedException(UUID orderId) {
        super("支払いの結果が確定していません: " + orderId);
    }
}
