package com.example.shop.payment.infrastructure;

import com.example.shop.payment.domain.ChargeResult;
import com.example.shop.payment.domain.PaymentGateway;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

/**
 * 冪等キーを送るところまでの途中の版（第19章の前半）。
 *
 * <p>第18章の版から変えたのは、{@code client.charge} にキーを
 * 渡す 1 行と、キーを決める {@code idempotencyKeyFor} だけ。
 * 送り直しはまだ組み込んでいない。
 */
@Component
class HttpPaymentGateway implements PaymentGateway {

    private final PaymentApiClient client;

    HttpPaymentGateway(PaymentApiClient client) {
        this.client = client;
    }

    // #region charge-with-key
    @Override
    public ChargeResult charge(UUID orderId, long amountYen) {
        try {
            ChargeApiResponse response =
                    client.charge(
                            idempotencyKeyFor(orderId),
                            new ChargeApiRequest(
                                    orderId.toString(),
                                    amountYen,
                                    "JPY"));
            return translate(response.status());
        } catch (RestClientException e) {
            return ChargeResult.UNKNOWN;
        }
    }
    // #endregion

    private String idempotencyKeyFor(UUID orderId) {
        return orderId.toString();
    }

    private ChargeResult translate(String status) {
        return switch (status) {
            case "succeeded" -> ChargeResult.SETTLED;
            case "declined" -> ChargeResult.DECLINED;
            case null, default -> ChargeResult.UNKNOWN;
        };
    }
}
