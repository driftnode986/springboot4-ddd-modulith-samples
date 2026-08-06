package com.example.shop.payment.infrastructure;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.service.registry.ImportHttpServices;

/**
 * 宣言だけのインターフェースから、実際に通信する実装を作らせる設定。
 *
 * <p>{@code group} に付けた名前が、そのまま設定ファイル側の
 * {@code spring.http.serviceclient.<name>} と対応する。
 */
@Configuration
@ImportHttpServices(group = "payment", types = PaymentApiClient.class)
class PaymentClientConfiguration {}
