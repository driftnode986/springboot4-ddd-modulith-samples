package com.example.shop.payment.infrastructure;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import com.example.shop.payment.domain.ChargeResult;
import com.example.shop.payment.domain.PaymentGateway;
import com.github.tomakehurst.wiremock.WireMockServer;
import java.util.UUID;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * 実際に HTTP を出して、相手の応答が支払いの語彙へ翻訳されることを確かめる。
 *
 * <p>相手役は WireMock が務める。決済代行会社に本当につなぐわけではないが、 通信そのものは本物なので、遅延や失敗をこちらから作れる。
 */
@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
class HttpPaymentGatewayTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18");

    private static WireMockServer wireMock;

    @Autowired PaymentGateway gateway;

    @BeforeAll
    static void startStubServer() {
        // 0 を渡すと空いているポートが選ばれる。固定すると他のテストとぶつかる。
        wireMock = new WireMockServer(0);
        wireMock.start();
    }

    @AfterAll
    static void stopStubServer() {
        wireMock.stop();
    }

    @DynamicPropertySource
    static void paymentApiUrl(DynamicPropertyRegistry registry) {
        // 起動してからでないとポートが決まらないので、ここで設定に流し込む。
        registry.add("spring.http.serviceclient.payment.base-url", () -> wireMock.baseUrl());
        // WireMock が同梱する Jetty は、JDK 標準のクライアントと相性が悪く
        // 応答の途中で切られたように見える。ここでは素朴なクライアントに切り替える。
        registry.add("spring.http.clients.imperative.factory", () -> "simple");
        // 応答を待ち続けないよう、待ち時間の上限を決めておく。
        registry.add("spring.http.clients.connect-timeout", () -> "1s");
        registry.add("spring.http.clients.read-timeout", () -> "300ms");
    }

    @BeforeEach
    void resetStubs() {
        wireMock.resetAll();
    }

    @Test
    @DisplayName("相手が succeeded を返したら、支払いは成立として扱う")
    void translatesSucceededToSettled() {
        wireMock.stubFor(
                post(urlEqualTo("/v1/charges"))
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withHeader("Content-Type", "application/json")
                                        .withBody(
                                                """
                                                {"id":"ch_1","status":"succeeded"}
                                                """)));

        UUID orderId = UUID.randomUUID();
        ChargeResult result = gateway.charge(orderId, 12_000);

        assertThat(result).isEqualTo(ChargeResult.SETTLED);

        // 相手が決めた項目名で送れていることも確かめておく。
        wireMock.verify(
                postRequestedFor(urlEqualTo("/v1/charges"))
                        .withHeader("Content-Type", equalTo("application/json"))
                        .withRequestBody(
                                equalToJson(
                                        """
                                        {"reference":"%s","amount":12000,"currency":"JPY"}
                                        """
                                                .formatted(orderId))));
    }

    @Test
    @DisplayName("相手が declined を返したら、与信が通らなかったとして扱う")
    void translatesDeclinedToDeclined() {
        wireMock.stubFor(
                post(urlEqualTo("/v1/charges"))
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withHeader("Content-Type", "application/json")
                                        .withBody(
                                                """
                                                {"id":"ch_2","status":"declined"}
                                                """)));

        assertThat(gateway.charge(UUID.randomUUID(), 12_000))
                .isEqualTo(ChargeResult.DECLINED);
    }

    @Test
    @DisplayName("相手が 500 を返したら、結果は確定していないものとして扱う")
    void treatsServerErrorAsUnknown() {
        wireMock.stubFor(post(urlEqualTo("/v1/charges")).willReturn(aResponse().withStatus(500)));

        assertThat(gateway.charge(UUID.randomUUID(), 12_000)).isEqualTo(ChargeResult.UNKNOWN);
    }

    @Test
    @DisplayName("相手が時間内に応答しなかったら、結果は確定していないものとして扱う")
    void treatsTimeoutAsUnknown() {
        wireMock.stubFor(
                post(urlEqualTo("/v1/charges"))
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withHeader("Content-Type", "application/json")
                                        .withBody("""
                                                {"id":"ch_4","status":"succeeded"}
                                                """)
                                        // 待ち時間の上限より長く、応答を送り始めるまで待たせる。
                                        .withFixedDelay(2_000)));

        assertThat(gateway.charge(UUID.randomUUID(), 12_000)).isEqualTo(ChargeResult.UNKNOWN);
    }

    @Test
    @DisplayName("相手が知らない状態を返したら、成立とは見なさない")
    void treatsUnknownStatusAsUnknown() {
        wireMock.stubFor(
                post(urlEqualTo("/v1/charges"))
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withHeader("Content-Type", "application/json")
                                        .withBody(
                                                """
                                                {"id":"ch_3","status":"processing"}
                                                """)));

        assertThat(gateway.charge(UUID.randomUUID(), 12_000)).isEqualTo(ChargeResult.UNKNOWN);
    }
}
