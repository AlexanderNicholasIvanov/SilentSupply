package com.silentsupply;

import com.silentsupply.config.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Smoke test to verify the Spring application context loads successfully.
 */
@SpringBootTest
@ActiveProfiles("test")
class SilentSupplyApplicationTest extends IntegrationTestBase {

    @Test
    void contextLoads() {
        // Verifies the application context starts without errors
    }
}
