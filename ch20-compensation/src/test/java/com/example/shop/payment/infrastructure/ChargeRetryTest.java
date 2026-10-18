package com.example.shop.payment.infrastructure;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
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
 * 相手が一時的に落ちているときは送り直し、断られたときは送り直さないことを確かめる。
 *
 * <p>送り直してよいのは、結果が確定していない場合に限る。 与信が通らなかったという返事は確定した結果なので、何度送っても変わらない。
 */
@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
class ChargeRetryTest {

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
    @DisplayName("相手が一度落ちても、送り直して成立する")
    void retriesAfterServerError() {
        UUID orderId = UUID.randomUUID();

        wireMock.stubFor(
                post(urlEqualTo("/v1/charges"))
                        .inScenario("recovering")
                        .whenScenarioStateIs(Scenario.STARTED)
                        .willReturn(aResponse().withStatus(500))
                        .willSetStateTo("recovered"));

        wireMock.stubFor(
                post(urlEqualTo("/v1/charges"))
                        .inScenario("recovering")
                        .whenScenarioStateIs("recovered")
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withHeader("Content-Type", "application/json")
                                        .withBody("""
                                                {"id":"ch_1","status":"succeeded"}
                                                """)));

        assertThat(gateway.charge(orderId, 12_000)).isEqualTo(ChargeResult.SETTLED);

        // 送り直しても冪等キーは変わらないので、相手は 1 回分として扱える。
        wireMock.verify(
                2,
                postRequestedFor(urlEqualTo("/v1/charges"))
                        .withHeader("Idempotency-Key", equalTo(orderId.toString())));
    }

    @Test
    @DisplayName("与信が通らなかったときは、送り直さない")
    void doesNotRetryWhenDeclined() {
        wireMock.stubFor(
                post(urlEqualTo("/v1/charges"))
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withHeader("Content-Type", "application/json")
                                        .withBody("""
                                                {"id":"ch_2","status":"declined"}
                                                """)));

        assertThat(gateway.charge(UUID.randomUUID(), 12_000)).isEqualTo(ChargeResult.DECLINED);

        wireMock.verify(1, postRequestedFor(urlEqualTo("/v1/charges")));
    }

    @Test
    @DisplayName("相手が落ち続けたら、結果は確定していないものとして諦める")
    void givesUpAfterRepeatedFailures() {
        wireMock.stubFor(post(urlEqualTo("/v1/charges")).willReturn(aResponse().withStatus(500)));

        assertThat(gateway.charge(UUID.randomUUID(), 12_000)).isEqualTo(ChargeResult.UNKNOWN);

        // 初回 + 送り直し 2 回。無限には送らない。
        wireMock.verify(3, postRequestedFor(urlEqualTo("/v1/charges")));
    }
}
