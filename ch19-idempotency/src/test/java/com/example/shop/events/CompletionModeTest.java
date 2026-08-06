package com.example.shop.events;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.shop.ShopApplication;
import com.example.shop.order.application.PlaceOrderCommand;
import com.example.shop.order.application.PlaceOrderService;
import java.time.Duration;
import java.util.List;
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
 * 処理し終えたイベントの記録をどう始末するかを確かめる。
 *
 * <p>既定では行が残り続ける。消す設定にすると完了と同時に行が消える。
 */
@Testcontainers(disabledWithoutDocker = true)
class CompletionModeTest {

    @Container
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18");

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

    private void placeOneOrder(ConfigurableApplicationContext context, String sku) {
        JdbcTemplate jdbc = context.getBean(JdbcTemplate.class);
        jdbc.update("DELETE FROM event_publication");
        jdbc.update("DELETE FROM inventory.handled_events");
        jdbc.update("DELETE FROM inventory.stocks WHERE sku = ?", sku);
        jdbc.update("INSERT INTO inventory.stocks (sku, quantity, version) VALUES (?, ?, 0)", sku, 10);

        context.getBean(PlaceOrderService.class)
                .place(
                        new PlaceOrderCommand(
                                "CUS-COMPLETION",
                                "東京都千代田区1-1-1",
                                List.of(new PlaceOrderCommand.Line(sku, 1, 1500))));

        // 引き当てが終わるまで待つ。
        Awaitility.await()
                .atMost(Duration.ofSeconds(10))
                .untilAsserted(
                        () ->
                                assertThat(
                                                jdbc.queryForObject(
                                                        "SELECT quantity FROM inventory.stocks WHERE sku = ?",
                                                        Integer.class,
                                                        sku))
                                        .isEqualTo(9));
    }

    private Integer rowCount(JdbcTemplate jdbc) {
        return jdbc.queryForObject("SELECT count(*) FROM event_publication", Integer.class);
    }

    @Test
    @DisplayName("既定では、処理し終えたイベントの行が残り続ける")
    void completedPublicationRemainsByDefault() {
        String sku = "P-KEEP-" + UUID.randomUUID().toString().substring(0, 4);

        try (ConfigurableApplicationContext context = start()) {
            placeOneOrder(context, sku);
            JdbcTemplate jdbc = context.getBean(JdbcTemplate.class);

            assertThat(rowCount(jdbc)).isEqualTo(1);
            System.out.println(
                    "UPDATE_MODE_ROW="
                            + jdbc.queryForList(
                                    "SELECT status, completion_date IS NOT NULL AS completed"
                                            + " FROM event_publication"));
        }
    }

    @Test
    @DisplayName("消す設定にすると、完了と同時に行が消える")
    void completedPublicationIsDeletedWhenConfigured() {
        String sku = "P-DEL-" + UUID.randomUUID().toString().substring(0, 4);

        try (ConfigurableApplicationContext context =
                start("spring.modulith.events.completion-mode=delete")) {
            placeOneOrder(context, sku);
            JdbcTemplate jdbc = context.getBean(JdbcTemplate.class);

            assertThat(rowCount(jdbc)).isZero();
            System.out.println("DELETE_MODE_ROWCOUNT=" + rowCount(jdbc));
        }
    }
}
