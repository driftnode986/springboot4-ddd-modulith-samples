package com.example.shop.payment.infrastructure;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

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
 * キーを送るところまでの途中の版で、前の 2 つのテストが通ることを
 * 確かめる（第19章の前半）。
 *
 * <p>3 つ目（応答が届かなくても課金が 1 回で済む）は、送り直しを
 * 組み込んだ完成形（src/test）でだけ通る。
 */
@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
class IdempotentChargeTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:18");

    private static WireMockServer wireMock;

    @Autowired PaymentGateway gateway;

    @BeforeAll
    static void startStubServer() {
        wireMock = new WireMockServer(0);
        wireMock.start();
    }

    @AfterAll
    static void stopStubServer() {
        wireMock.stop();
    }

    @DynamicPropertySource
    static void paymentApiUrl(
            DynamicPropertyRegistry registry) {
        registry.add(
                "spring.http.serviceclient.payment.base-url",
                () -> wireMock.baseUrl());
        registry.add(
                "spring.http.clients.imperative.factory",
                () -> "simple");
        registry.add(
                "spring.http.clients.connect-timeout",
                () -> "1s");
        registry.add(
                "spring.http.clients.read-timeout",
                () -> "300ms");
    }

    @BeforeEach
    void resetStubs() {
        wireMock.resetAll();
    }

    @Test
    @DisplayName("同じ注文を二度請求しても、"
            + "相手には同じ冪等キーが届く")
    void sendsSameKeyForSameOrder() {
        wireMock.stubFor(post(urlEqualTo("/v1/charges"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type",
                                "application/json")
                        .withBody("""
                        {"id":"ch_1","status":"succeeded"}
                        """)));

        UUID orderId = UUID.randomUUID();
        gateway.charge(orderId, 12_000);
        gateway.charge(orderId, 12_000);

        // 2 回とも同じキーで届いていること。
        // 相手はこれを見て 1 回分と判断できる。
        wireMock.verify(
                2,
                postRequestedFor(urlEqualTo("/v1/charges"))
                        .withHeader("Idempotency-Key",
                                equalTo(orderId.toString())));
    }

    @Test
    @DisplayName("注文が違えば、冪等キーも違う")
    void sendsDifferentKeysForDifferentOrders() {
        wireMock.stubFor(post(urlEqualTo("/v1/charges"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type",
                                "application/json")
                        .withBody("""
                        {"id":"ch_1","status":"succeeded"}
                        """)));

        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();
        gateway.charge(first, 12_000);
        gateway.charge(second, 12_000);

        wireMock.verify(
                1,
                postRequestedFor(urlEqualTo("/v1/charges"))
                        .withHeader("Idempotency-Key",
                                equalTo(first.toString())));
        wireMock.verify(
                1,
                postRequestedFor(urlEqualTo("/v1/charges"))
                        .withHeader("Idempotency-Key",
                                equalTo(second.toString())));
    }
}
