package com.example.shop.payment.infrastructure;

import java.util.UUID;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

/**
 * 請求を送り、結果が確定しなかったときは間隔を空けて送り直す。
 *
 * <p>送り直しを {@link HttpPaymentGateway} の中のメソッドに付けても働かない。 同じオブジェクトの中から呼ぶと、送り直しを差し込んだ包みを通らずに
 * 本体へ直接届いてしまうためになる。別の部品に分けているのはそのためになる。
 *
 * <p>送り直してよいのは、冪等キーによって課金が 1 回に留まると分かっているからになる。 キーを添えずにこれを行うと、二重課金の仕掛けにしかならない。
 */
@Component
class ChargeSender {

    private final PaymentApiClient client;

    ChargeSender(PaymentApiClient client) {
        this.client = client;
    }

    /**
     * 請求を送る。
     *
     * <p>与信が通らなかったという返事は例外にならないため、ここでは送り直されない。 何度送っても結果は変わらないので、送り直す意味がない。
     */
    @Retryable(
            includes = RestClientException.class,
            maxRetries = 2,
            delay = 200,
            multiplier = 2,
            maxDelay = 2_000)
    ChargeApiResponse send(String idempotencyKey, UUID orderId, long amountYen) {
        return client.charge(
                idempotencyKey, new ChargeApiRequest(orderId.toString(), amountYen, "JPY"));
    }
}
