package com.example.shop.payment.application;

import com.example.shop.payment.domain.ChargeResult;
import com.example.shop.payment.domain.PaymentGateway;
import com.example.shop.payment.spi.PaymentFailed;
import com.example.shop.payment.spi.PaymentRequest;
import java.util.List;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 請求を実行し、成立しなかったことを発表する。
 *
 * <p>誰が在庫を戻すかは知らない。「支払いが成立しなかった」という事実を発表するだけで、
 * それを受けて何を戻すかは、受け取る側の判断になる。
 */
@Service
class RequestPaymentService implements PaymentRequest {

    private final PaymentGateway gateway;
    private final ApplicationEventPublisher events;

    RequestPaymentService(PaymentGateway gateway, ApplicationEventPublisher events) {
        this.gateway = gateway;
        this.events = events;
    }

    @Override
    @Transactional
    public void request(UUID orderId, long amountYen, List<Reserved> reserved) {
        ChargeResult result = gateway.charge(orderId, amountYen);

        if (result == ChargeResult.SETTLED) {
            return;
        }

        // 成立しなかった。結果が確定していない場合もここに入る。
        // 請求が通っていたかもしれないが、通っていないものとして扱うと在庫だけが戻り、
        // 代金は受け取ったままになる。第19章で述べたとおり、確定していないものは
        // 確定していないものとして残し、ここでは戻さない。
        if (result == ChargeResult.UNKNOWN) {
            throw new PaymentUndeterminedException(orderId);
        }

        events.publishEvent(
                new PaymentFailed(
                        UUID.randomUUID().toString(),
                        orderId.toString(),
                        reserved.stream()
                                .map(r -> new PaymentFailed.Item(r.productId(), r.quantity()))
                                .toList(),
                        result.name()));
    }
}
