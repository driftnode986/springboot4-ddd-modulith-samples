package com.example.shop.payment.infrastructure;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import com.example.shop.payment.domain.ChargeResult;
import com.example.shop.payment.domain.PaymentGateway;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.stubbing.Scenario;
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
 * 同じ請求を二度送っても、課金が 1 回で済むことを確かめる。
 *
 * <p>相手役の WireMock は、冪等キーごとに一度だけ課金を記録する。 本物の決済代行会社が約束していることを、こちらで真似したものになる。
 */
@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
class IdempotentChargeTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18");

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
    static void paymentApiUrl(DynamicPropertyRegistry registry) {
        registry.add("spring.http.serviceclient.payment.base-url", () -> wireMock.baseUrl());
        registry.add("spring.http.clients.imperative.factory", () -> "simple");
        registry.add("spring.http.clients.connect-timeout", () -> "1s");
        registry.add("spring.http.clients.read-timeout", () -> "300ms");
    }

    @BeforeEach
    void resetStubs() {
        wireMock.resetAll();
    }

    @Test
    @DisplayName("同じ注文を二度請求しても、相手には同じ冪等キーが届く")
    void sendsSameKeyForSameOrder() {
        wireMock.stubFor(
                post(urlEqualTo("/v1/charges"))
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withHeader("Content-Type", "application/json")
                                        .withBody("""
                                                {"id":"ch_1","status":"succeeded"}
                                                """)));

        UUID orderId = UUID.randomUUID();
        gateway.charge(orderId, 12_000);
        gateway.charge(orderId, 12_000);

        // 2 回とも同じキーで届いていること。相手はこれを見て 1 回分と判断できる。
        wireMock.verify(
                2,
                postRequestedFor(urlEqualTo("/v1/charges"))
                        .withHeader("Idempotency-Key", equalTo(orderId.toString())));
    }

    @Test
    @DisplayName("注文が違えば、冪等キーも違う")
    void sendsDifferentKeysForDifferentOrders() {
        wireMock.stubFor(
                post(urlEqualTo("/v1/charges"))
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withHeader("Content-Type", "application/json")
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
                        .withHeader("Idempotency-Key", equalTo(first.toString())));
        wireMock.verify(
                1,
                postRequestedFor(urlEqualTo("/v1/charges"))
                        .withHeader("Idempotency-Key", equalTo(second.toString())));
    }

    @Test
    @DisplayName("応答が届かなくても、送り直しで結果を取り戻し、課金は 1 回で済む")
    void chargesOnceEvenWhenResponseIsLost() {
        UUID orderId = UUID.randomUUID();
        String key = orderId.toString();

        // 一度目：課金は済むが、応答が待ち時間に間に合わない。
        wireMock.stubFor(
                post(urlEqualTo("/v1/charges"))
                        .inScenario("lost-response")
                        .whenScenarioStateIs(Scenario.STARTED)
                        .withHeader("Idempotency-Key", equalTo(key))
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withHeader("Content-Type", "application/json")
                                        .withBody("""
                                                {"id":"ch_9","status":"succeeded"}
                                                """)
                                        .withFixedDelay(2_000))
                        .willSetStateTo("charged"));

        // 二度目：相手は同じキーを覚えていて、課金し直さずに一度目の結果を返す。
        wireMock.stubFor(
                post(urlEqualTo("/v1/charges"))
                        .inScenario("lost-response")
                        .whenScenarioStateIs("charged")
                        .withHeader("Idempotency-Key", equalTo(key))
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withHeader("Content-Type", "application/json")
                                        .withBody("""
                                                {"id":"ch_9","status":"succeeded"}
                                                """)));

        // 一度目の応答は待ち時間に間に合わないが、送り直しが自動で働く。
        // 二度目は同じキーで届くので、相手は課金し直さず一度目の結果を返す。
        assertThat(gateway.charge(orderId, 12_000)).isEqualTo(ChargeResult.SETTLED);

        // 相手が受け取った請求は 2 通。ただし課金されたのは 1 回だけで、
        // 二度目は一度目の結果を読み出しただけになる（ch_9 が同じことがその証拠）。
        wireMock.verify(
                2,
                postRequestedFor(urlEqualTo("/v1/charges"))
                        .withHeader("Idempotency-Key", equalTo(key))
                        .withRequestBody(matchingJsonPath("$.reference", equalTo(key))));
    }
}
