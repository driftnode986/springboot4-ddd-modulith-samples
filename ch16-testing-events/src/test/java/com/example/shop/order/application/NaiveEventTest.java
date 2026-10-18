package com.example.shop.order.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.shop.inventory.domain.StockRepository;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * 素直に書いたテストが何を見ているかを確かめる。
 *
 * <p>発行は見えるが、受け取る側が終わったかどうかは見えない。
 */
// #region naive-head
@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
class NaiveEventTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:18");

    @Autowired private PlaceOrderService placeOrder;
    @Autowired private StockRepository stocks;
    @Autowired private JdbcTemplate jdbc;
    // #endregion

    // #region naive-setup
    private String sku;

    @BeforeEach
    void setUp() {
        sku = "P-"
                + UUID.randomUUID().toString().substring(0, 8);
        jdbc.update(
                "INSERT INTO stocks (sku, quantity, version)"
                        + " VALUES (?, ?, 0)",
                sku, 10);
    }

    private PlaceOrderCommand command() {
        return new PlaceOrderCommand(
                "CUS-1",
                "東京都千代田区1-1",
                List.of(new PlaceOrderCommand.Line(
                        sku, 3, 1200L)));
    }
    // #endregion

    // #region cannot-see
    @Test
    @DisplayName("しかし在庫が減ったかどうかは、"
            + "この時点では分からない")
    void cannotSeeTheOutcomeYet() {
        placeOrder.place(command());

        // 受け取る側は別スレッドで動くので、
        // ここではまだ終わっていないことがある。
        int remaining =
                stocks.findBySku(sku).orElseThrow().quantity();
        System.out.println("PROBE_REMAINING_RIGHT_AFTER_PLACE="
                + remaining);
        assertThat(remaining).isBetween(7, 10);
    }
    // #endregion

    /**
     * 発行された事実はレジストリの表で見える。
     * {@code PublishedEvents} でも見えるが、引数で受け取るには
     * Spring Modulith の拡張が要る。
     * {@code @SpringBootTest} だけを付けた状態でこの引数を
     * 宣言すると、テストが動く前に {@code ParameterResolutionException} で落ちる
     * （probes/ の NaiveEventTest.java で確かめられる）。
     */
    // #region sees-publication
    @Test
    @DisplayName("発行されたことはレジストリの表で見える")
    void seesThePublication() {
        placeOrder.place(command());

        // 表にはほかのテストの記録も残るので、
        // この注文の商品を含む記録だけを数える。
        Integer publications = jdbc.queryForObject(
                "SELECT count(*) FROM event_publication"
                        + " WHERE serialized_event LIKE ?",
                Integer.class, "%" + sku + "%");
        assertThat(publications).isPositive();
    }
    // #endregion
}
