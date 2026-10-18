package com.example.shop;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

/** どの表がどのスキーマに置かれたかを実際に読み出して確かめる。 */
@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
class SchemaLayoutProbeTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18");

    @Autowired private JdbcTemplate jdbc;

    @Test
    @DisplayName("表の置かれたスキーマを一覧する")
    void listsTablesPerSchema() {
        List<Map<String, Object>> rows =
                jdbc.queryForList(
                        "SELECT table_schema, table_name FROM information_schema.tables"
                                + " WHERE table_schema NOT IN ('pg_catalog', 'information_schema')"
                                + " ORDER BY table_schema, table_name");

        rows.forEach(r -> System.out.println("LAYOUT " + r.get("table_schema") + "." + r.get("table_name")));
    }
}
