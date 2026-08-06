package com.example.shop.payment.infrastructure;

import com.example.shop.payment.domain.ChargeResult;
import com.example.shop.payment.domain.PaymentGateway;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

/**
 * 決済代行会社の語彙を支払いの語彙へ翻訳する層。
 *
 * <p>相手が返す {@code "succeeded"} や HTTP の 500 は、この中で {@link ChargeResult} に置き換わる。 支払いの内側は、相手がどんな文字列を返すかを知らないままでいられる。
 */
@Component
class HttpPaymentGateway implements PaymentGateway {

    private final PaymentApiClient client;

    HttpPaymentGateway(PaymentApiClient client) {
        this.client = client;
    }

    @Override
    public ChargeResult charge(UUID orderId, long amountYen) {
        try {
            ChargeApiResponse response =
                    client.charge(new ChargeApiRequest(orderId.toString(), amountYen, "JPY"));
            return translate(response.status());
        } catch (RestClientException e) {
            // 相手が異常を返した、応答が返らなかった、途中で切れた。
            // どれも「請求が通ったかどうかはこちらからは分からない」に落ちる。
            // 待ち時間を超えたときは接続そのものの例外ではなく、
            // 読みかけの本文を解釈できないという形で表面化することがある。
            return ChargeResult.UNKNOWN;
        }
    }

    /** 相手の文字列を支払いの語彙へ置き換える。知らない値は、成立とは見なさない。 */
    private ChargeResult translate(String status) {
        return switch (status) {
            case "succeeded" -> ChargeResult.SETTLED;
            case "declined" -> ChargeResult.DECLINED;
            case null, default -> ChargeResult.UNKNOWN;
        };
    }
}
