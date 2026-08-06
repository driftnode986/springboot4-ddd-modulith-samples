package com.example.shop.payment.spi;

import java.util.List;
import java.util.UUID;

/** 支払いを頼む口。頼む側は、相手がどう請求するかを知らない。 */
public interface PaymentRequest {

    /**
     * 請求を頼む。
     *
     * <p>戻せる対象を一緒に渡す。支払いが成立しなかったとき、支払いの側は「何を戻せばよいか」を
     * 自分では知らないため、頼まれた時点で受け取っておく必要がある。ここで注文の集約を渡すと、
     * 支払いが注文の内部を知ることになるので、戻す判断に要る分だけを渡す。
     *
     * @param orderId 請求の対象となる注文
     * @param amountYen 請求する金額（円）
     * @param reserved 支払いが成立しなかったときに戻す対象
     */
    void request(UUID orderId, long amountYen, List<Reserved> reserved);

    /** 引き当て済みの品目。戻す判断に必要な分だけを持つ。 */
    record Reserved(String productId, int quantity) {
    }
}
