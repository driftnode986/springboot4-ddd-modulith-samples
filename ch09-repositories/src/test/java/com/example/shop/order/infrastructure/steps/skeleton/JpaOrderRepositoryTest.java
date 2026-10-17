package com.example.shop.order.infrastructure.steps.skeleton;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

/*
 * 第9章で最初に書く形（テストの本体を書く前）。
 * 完成形は com.example.shop.order.infrastructure.JpaOrderRepositoryTest。
 */
// #region skeleton
@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
class JpaOrderRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:18");

    // テスト本体はこのあとで書きます
}
// #endregion
