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
 * 停止したアプリを起動し直したときに、未完了のイベントが配り直されるかどうかを確かめる。
 *
 * <p>起動と停止を実際に行うため、{@code @SpringBootTest} ではなく
 * {@link SpringApplicationBuilder} で文脈を組み立てて閉じる。データベースは
 * 2 回の起動で同じものを使うので、1 回目に書いた行が 2 回目から見える。
 */
@Testcontainers(disabledWithoutDocker = true)
class RestartRepublishTest {

    @Container
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18");

    /** 受け取る側の識別子。実際に登録される値と同じ形にする。 */
    private static final String LISTENER_ID =
            "com.example.shop.inventory.application.ReserveStockOnOrderPlaced"
                    + ".on(com.example.shop.order.spi.OrderPlaced)";

    private ConfigurableApplicationContext start(boolean republish) {
        // 設定ファイルより後に読ませたいので、コマンドライン引数として渡す。
        // SpringApplicationBuilder.properties は application.yml より優先度が低い。
        return new SpringApplicationBuilder(ShopApplication.class)
                .web(WebApplicationType.NONE)
                .properties(
                        "spring.datasource.url=" + postgres.getJdbcUrl(),
                        "spring.datasource.username=" + postgres.getUsername(),
                        "spring.datasource.password=" + postgres.getPassword())
                .run(
                        "--spring.modulith.events.republish-outstanding-events-on-restart="
                                + republish);
    }

    /**
     * 「発表されたが処理されないまま停止した」状態を作る。未完了のイベントを 1 件だけ表に置く。
     */
    private void leaveOutstandingPublication(JdbcTemplate jdbc, String sku) {
        jdbc.update("DELETE FROM event_publication");
        jdbc.update("DELETE FROM inventory.handled_events");
        jdbc.update("DELETE FROM inventory.stocks WHERE sku = ?", sku);
        jdbc.update("INSERT INTO inventory.stocks (sku, quantity, version) VALUES (?, ?, 0)", sku, 10);

        String serialized =
                """
                {"eventId":"%s","orderId":"%s","customerId":"CUS-RESTART",\
                "items":[{"productId":"%s","quantity":1}]}"""
                        .formatted(UUID.randomUUID(), UUID.randomUUID(), sku);

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
                java.sql.Timestamp.from(Instant.now()));
    }

    private Integer quantityOf(JdbcTemplate jdbc, String sku) {
        return jdbc.queryForObject("SELECT quantity FROM inventory.stocks WHERE sku = ?", Integer.class, sku);
    }

    @Test
    @DisplayName("既定のままでは、起動し直しても配り直されない")
    void outstandingPublicationIsNotRepublishedByDefault() {
        String sku = "P-NOREPUB";

        try (ConfigurableApplicationContext first = start(false)) {
            leaveOutstandingPublication(first.getBean(JdbcTemplate.class), sku);
        }

        try (ConfigurableApplicationContext second = start(false)) {
            JdbcTemplate jdbc = second.getBean(JdbcTemplate.class);

            // 何も起きないことを示すため、一定の時間ずっと変化しないことを確かめる。
            Awaitility.await()
                    .during(Duration.ofSeconds(3))
                    .atMost(Duration.ofSeconds(6))
                    .untilAsserted(() -> assertThat(quantityOf(jdbc, sku)).isEqualTo(10));

            System.out.println("NOREPUBLISH_QUANTITY=" + quantityOf(jdbc, sku));
        }
    }

    @Test
    @DisplayName("明示的に有効にすると、起動し直したときに配り直される")
    void outstandingPublicationIsRepublishedWhenEnabled() {
        String sku = "P-REPUB";

        try (ConfigurableApplicationContext first = start(true)) {
            leaveOutstandingPublication(first.getBean(JdbcTemplate.class), sku);
        }

        try (ConfigurableApplicationContext second = start(true)) {
            JdbcTemplate jdbc = second.getBean(JdbcTemplate.class);

            Awaitility.await()
                    .atMost(Duration.ofSeconds(10))
                    .untilAsserted(() -> assertThat(quantityOf(jdbc, sku)).isEqualTo(9));

            System.out.println("REPUBLISH_QUANTITY=" + quantityOf(jdbc, sku));
        }
    }
}
