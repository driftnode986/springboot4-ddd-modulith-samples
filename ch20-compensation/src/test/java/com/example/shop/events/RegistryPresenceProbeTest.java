package com.example.shop.events;

import static org.assertj.core.api.Assertions.assertThat;

import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.modulith.events.IncompleteEventPublications;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

/** starter-jpa を足しただけの状態で、レジストリと表がどうなっているかを見る。 */
@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
class RegistryPresenceProbeTest {

    @Container @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18");

    @Autowired IncompleteEventPublications registry;
    @Autowired DataSource dataSource;

    @Test
    void レジストリのビーンは存在する() {
        assertThat(registry).isNotNull();
        System.out.println("REGISTRY_CLASS=" + registry.getClass().getName());
    }

    @Test
    void イベントの表が存在するか調べる() {
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        var tables =
                jdbc.queryForList(
                        "select table_name from information_schema.tables where table_schema='public' order by table_name",
                        String.class);
        System.out.println("TABLES=" + tables);
    }
}
