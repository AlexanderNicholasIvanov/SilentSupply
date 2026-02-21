package com.silentsupply.config;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;

/**
 * Base class for integration tests that require a PostgreSQL database.
 * Uses host networking to avoid Docker port-mapping issues on certain systems.
 */
@Testcontainers
public abstract class IntegrationTestBase {

    private static final int PG_PORT = 15432;
    private static final String PG_USER = "silentsupply";
    private static final String PG_PASSWORD = "silentsupply";
    private static final String PG_DB = "silentsupply_test";

    /** PostgreSQL container using host networking. */
    @Container
    static final GenericContainer<?> POSTGRES = new GenericContainer<>("postgres:16-alpine")
            .withNetworkMode("host")
            .withEnv("POSTGRES_USER", PG_USER)
            .withEnv("POSTGRES_PASSWORD", PG_PASSWORD)
            .withEnv("POSTGRES_DB", PG_DB)
            .withEnv("PGPORT", String.valueOf(PG_PORT))
            .waitingFor(Wait.forLogMessage(".*database system is ready to accept connections.*\\s", 2))
            .withStartupTimeout(Duration.ofMinutes(5));

    /**
     * Registers the container's JDBC properties into the Spring environment.
     *
     * @param registry the dynamic property registry
     */
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        String jdbcUrl = String.format(
                "jdbc:postgresql://127.0.0.1:%d/%s?sslmode=disable", PG_PORT, PG_DB);
        registry.add("spring.datasource.url", () -> jdbcUrl);
        registry.add("spring.datasource.username", () -> PG_USER);
        registry.add("spring.datasource.password", () -> PG_PASSWORD);
    }
}
