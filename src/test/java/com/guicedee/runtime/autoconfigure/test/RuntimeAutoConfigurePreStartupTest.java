package com.guicedee.runtime.autoconfigure.test;

import com.guicedee.client.IGuiceContext;
import com.guicedee.runtime.autoconfigure.implementations.RuntimeAutoConfigurePreStartup;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for {@link RuntimeAutoConfigurePreStartup}.
 * <p>
 * Since we're running locally (not in any cloud), no provider should detect.
 * Verifies that the detection runs cleanly and reports no cloud environment.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RuntimeAutoConfigurePreStartupTest {

    @BeforeAll
    void setUp() {
        IGuiceContext.registerModule("com.guicedee.runtime.autoconfigure.test");
        IGuiceContext.instance().inject();
    }

    @AfterAll
    void tearDown() {
        IGuiceContext.instance().destroy();
    }

    @Test
    @Order(1)
    void testNoCloudDetectedLocally() {
        // Running locally — no cloud env vars should be set
        assertFalse(RuntimeAutoConfigurePreStartup.isCloudDetected(),
                "No cloud runtime should be detected in local test environment");
        assertTrue(RuntimeAutoConfigurePreStartup.current().isEmpty(),
                "current() should return empty when no cloud detected");
        assertNull(RuntimeAutoConfigurePreStartup.getDetectedProviderId(),
                "detectedProviderId should be null when no cloud detected");
    }

    @Test
    @Order(2)
    void testCurrentReturnsOptional() {
        var result = RuntimeAutoConfigurePreStartup.current();
        assertNotNull(result, "current() should never return null itself");
    }
}

