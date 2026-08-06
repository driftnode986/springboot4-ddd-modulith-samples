package com.example.shop.payment.application;

import com.example.shop.payment.spi.PaymentRequest;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
class RequestPaymentService implements PaymentRequest {

    @Override
    public void request(UUID orderId) {
        // 外部の決済サービスへの接続は第18章で実装します。
    }
}
