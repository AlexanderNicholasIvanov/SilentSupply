package com.silentsupply.config;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.time.Duration;

/**
 * Base class for integration tests that require a PostgreSQL database.
 * Uses host networking to avoid Docker port-mapping issues on certain systems.
 *
 * <p>The container is started once per JVM using the singleton pattern,
 * ensuring all test classes share the same database instance.</p>
 */
public abstract class IntegrationTestBase {

    private static final int PG_PORT = 15432;
    private static final String PG_USER = "silentsupply";
    private static final String PG_PASSWORD = "silentsupply";
    private static final String PG_DB = "silentsupply_test";

    /** PostgreSQL container using host networking (singleton per JVM). */
    static final GenericContainer<?> POSTGRES;

    static {
        POSTGRES = new GenericContainer<>("postgres:16-alpine")
                .withNetworkMode("host")
                .withEnv("POSTGRES_USER", PG_USER)
                .withEnv("POSTGRES_PASSWORD", PG_PASSWORD)
                .withEnv("POSTGRES_DB", PG_DB)
                .withEnv("PGPORT", String.valueOf(PG_PORT))
                .waitingFor(Wait.forLogMessage(".*database system is ready to accept connections.*\\s", 2))
                .withStartupTimeout(Duration.ofMinutes(5));
        POSTGRES.start();
    }

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
