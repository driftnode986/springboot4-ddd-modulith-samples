package com.example.shop.payment.infrastructure;

import com.example.shop.payment.domain.ChargeResult;
import com.example.shop.payment.domain.PaymentGateway;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

/**
 * 決済代行会社の語彙を支払いの語彙へ翻訳する層。
 *
 * <p>相手が返す {@code "succeeded"} や HTTP の 500 は、
 * この中で {@link ChargeResult} に置き換わる。
 * 支払いの内側は、相手がどんな文字列を返すかを
 * 知らないままでいられる。
 */
@Component
class HttpPaymentGateway implements PaymentGateway {

    // #region sender
    private final ChargeSender sender;

    HttpPaymentGateway(ChargeSender sender) {
        this.sender = sender;
    }

    @Override
    public ChargeResult charge(UUID orderId, long amountYen) {
        try {
            return translate(sender.send(
                    idempotencyKeyFor(orderId),
                    orderId,
                    amountYen)
                    .status());
        } catch (RestClientException e) {
            // 送り直しても結果が確定しなかった。
            // 請求が通ったかどうかは、こちらからは分からない。
            return ChargeResult.UNKNOWN;
        }
    }
    // #endregion

    // #region key
    /**
     * 請求に添える冪等キーを、注文の識別子から決める。
     *
     * 毎回作り直すのではなく、同じ注文からは必ず同じキーが
     * 出るようにする。
     * ここで UUID.randomUUID() を使うと、送り直すたびに
     * 別の請求として扱われ、二重に課金される。
     */
    private String idempotencyKeyFor(UUID orderId) {
        return orderId.toString();
    }
    // #endregion

    /** 相手の文字列を支払いの語彙へ置き換える。知らない値は、成立とは見なさない。 */
    private ChargeResult translate(String status) {
        return switch (status) {
            case "succeeded" -> ChargeResult.SETTLED;
            case "declined" -> ChargeResult.DECLINED;
            case null, default -> ChargeResult.UNKNOWN;
        };
    }
}
