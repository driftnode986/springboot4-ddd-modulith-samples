package com.example.shop.events;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * application-schema-dump.yml で Hibernate に表の定義を書き出させ、
 * レジストリの表の serialized_event が varchar(255) で生成されることを確かめる。
 */
@SpringBootTest
@ActiveProfiles("schema-dump")
@Testcontainers(disabledWithoutDocker = true)
class SchemaGenerationProbeTest {

    private static final Path OUT = Path.of("/tmp/schema-out.sql");

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18");

    @BeforeAll
    static void removePreviousOutput() throws IOException {
        Files.deleteIfExists(OUT);
    }

    @Test
    @DisplayName("生成される event_publication の serialized_event は varchar(255) になる")
    void generatedRegistryTableUsesVarchar255() throws IOException {
        String ddl = Files.readString(OUT);

        assertThat(ddl).contains("create table event_publication");
        assertThat(ddl).containsPattern("serialized_event varchar\\(255\\) not null");
    }
}
