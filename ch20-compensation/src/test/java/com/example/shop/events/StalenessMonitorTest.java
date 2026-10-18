package com.example.shop.events;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.shop.ShopApplication;
import com.example.shop.order.spi.OrderPlaced;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * 詰まったまま動かないイベントを、監視が失敗として拾い上げるかどうかを確かめる。
 *
 * <p>監視は 3 つの時間のどれかを 0 でない値にしたときだけ動き出す。設定しなければ
 * 定期実行そのものが登録されない。
 */
@Testcontainers(disabledWithoutDocker = true)
class StalenessMonitorTest {

    @Container
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18");

    private static final String LISTENER_ID =
            "com.example.shop.inventory.application.ReserveStockOnOrderPlaced"
                    + ".on(com.example.shop.order.spi.OrderPlaced)";

    private ConfigurableApplicationContext start(String... extraProperties) {
        String[] base = {
            "spring.datasource.url=" + postgres.getJdbcUrl(),
            "spring.datasource.username=" + postgres.getUsername(),
            "spring.datasource.password=" + postgres.getPassword(),
        };
        String[] all = new String[base.length + extraProperties.length];
        System.arraycopy(base, 0, all, 0, base.length);
        System.arraycopy(extraProperties, 0, all, base.length, extraProperties.length);

        return new SpringApplicationBuilder(ShopApplication.class)
                .web(WebApplicationType.NONE)
                .properties(all)
                // 設定ファイルより優先させたいので、コマンドライン引数として渡す。
                .run("--spring.modulith.events.republish-outstanding-events-on-restart=false");
    }

    /** 十分に古い「発表済みのまま」のイベントを 1 件置く。 */
    private void leaveStalePublication(JdbcTemplate jdbc) {
        jdbc.update("DELETE FROM event_publication");

        String serialized =
                """
                {"eventId":"%s","orderId":"%s","customerId":"CUS-STALE",\
                "items":[{"productId":"P-STALE","quantity":1}]}"""
                        .formatted(UUID.randomUUID(), UUID.randomUUID());

        jdbc.update(
                """
                INSERT INTO event_publication
                  (id, listener_id, event_type, serialized_event,
                   publication_date, completion_date, completion_attempts, status)
                VALUES (?, ?, ?, ?, ?, NULL, 0, 'PUBLISHED')
                """,
                UUID.randomUUID(),
                LISTENER_ID,
                OrderPlaced.class.getName(),
                serialized,
                java.sql.Timestamp.from(Instant.now().minus(Duration.ofHours(1))));
    }

    private String statusOf(JdbcTemplate jdbc) {
        return jdbc.queryForObject("SELECT status FROM event_publication", String.class);
    }

    @Test
    @DisplayName("時間を設定しなければ、詰まったイベントは発表済みのまま放置される")
    void stalePublicationIsLeftAloneWithoutConfiguration() {
        try (ConfigurableApplicationContext context = start()) {
            JdbcTemplate jdbc = context.getBean(JdbcTemplate.class);
            leaveStalePublication(jdbc);

            Awaitility.await()
                    .during(Duration.ofSeconds(4))
                    .atMost(Duration.ofSeconds(8))
                    .untilAsserted(() -> assertThat(statusOf(jdbc)).isEqualTo("PUBLISHED"));

            System.out.println("NO_STALENESS_STATUS=" + statusOf(jdbc));
        }
    }

    @Test
    @DisplayName("時間を設定すると、詰まったイベントが失敗として拾い上げられる")
    void stalePublicationIsMarkedFailedWhenConfigured() {
        try (ConfigurableApplicationContext context =
                start(
                        "spring.modulith.events.staleness.published=1m",
                        "spring.modulith.events.staleness.check-intervall=1s")) {
            JdbcTemplate jdbc = context.getBean(JdbcTemplate.class);
            leaveStalePublication(jdbc);

            Awaitility.await()
                    .atMost(Duration.ofSeconds(15))
                    .untilAsserted(() -> assertThat(statusOf(jdbc)).isEqualTo("FAILED"));

            System.out.println("STALENESS_STATUS=" + statusOf(jdbc));
        }
    }
}
