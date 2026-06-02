package com.guicedee.runtime.autoconfigure.test;

import com.guicedee.client.Environment;
import com.guicedee.client.IGuiceContext;
import com.guicedee.runtime.autoconfigure.implementations.RuntimeAutoConfigurePreStartup;
import com.guicedee.runtime.autoconfigure.RuntimeEnvironment;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Docker integration test — simulates Azure Container Apps by setting system properties
 * that match the env vars Azure injects.
 * <p>
 * Run with: {@code docker run -e CONTAINER_APP_NAME=wallet-api -e CONTAINER_APP_ENV_DNS_SUFFIX=myenv.eastus.azurecontainerapps.io ...}
 * <p>
 * Or locally by pre-setting system properties (which EnvironmentUtils reads as fallback).
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SimulatedCloudDetectionTest {

    @BeforeAll
    void setUp() {
        // Simulate Azure Container Apps environment
        System.setProperty("CONTAINER_APP_NAME", "wallet-api");
        System.setProperty("CONTAINER_APP_ENV_DNS_SUFFIX", "happydesert-abc123.eastus.azurecontainerapps.io");
        System.setProperty("CONTAINER_APP_PORT", "3000");
        System.setProperty("CONTAINER_APP_REVISION", "wallet-api--rev5");
        System.setProperty("CONTAINER_APP_REPLICA_NAME", "wallet-api--rev5-7f8d9c4b2-xk9m2");
        System.setProperty("CONTAINER_APP_HOSTNAME", "wallet-api.happydesert-abc123.eastus.azurecontainerapps.io");

        // Boot GuicedEE — scans the .azure sub-package which has @AzureContainerApps annotation
        IGuiceContext.registerModule("com.guicedee.runtime.autoconfigure.test.azure");
        IGuiceContext.instance().inject();
    }

    @AfterAll
    void tearDown() {
        IGuiceContext.instance().destroy();
        System.clearProperty("CONTAINER_APP_NAME");
        System.clearProperty("CONTAINER_APP_ENV_DNS_SUFFIX");
        System.clearProperty("CONTAINER_APP_PORT");
        System.clearProperty("CONTAINER_APP_REVISION");
        System.clearProperty("CONTAINER_APP_REPLICA_NAME");
        System.clearProperty("CONTAINER_APP_HOSTNAME");
    }

    @Test
    @Order(1)
    void testCloudDetected() {
        assertTrue(Boolean.parseBoolean(Environment.getSystemPropertyOrEnvironment("CLOUD","false")),
                "Azure Container Apps should be detected");
    }

    @Test
    @Order(2)
    void testProviderIdIsAzure() {
        assertEquals("azure-container-apps", Boolean.parseBoolean(Environment.getSystemPropertyOrEnvironment("CLOUD", "false")));
    }

    @Test
    @Order(3)
    void testEnvironmentValues() {
        if(Boolean.parseBoolean(Environment.getSystemPropertyOrEnvironment("CLOUD","false"))) {
            System.out.println("☁️  Cloud environment detected, running Azure Container Apps environment variable tests");
        } else {
            System.out.println("⚠️  CLOUD env var not set to true, but system properties should still be read for testing");
        }
        RuntimeEnvironment env = RuntimeAutoConfigurePreStartup.current().orElseThrow();

        assertEquals("azure-container-apps", env.provider());
        assertEquals("wallet-api", env.serviceName());
        assertEquals("wallet-api--rev5-7f8d9c4b2-xk9m2", env.serviceId());
        assertEquals("wallet-api.happydesert-abc123.eastus.azurecontainerapps.io", env.hostname());
        assertEquals(3000, env.port());
        assertEquals("wallet-api--rev5", env.revision());
        assertEquals("wallet-api--rev5-7f8d9c4b2-xk9m2", env.replicaName());
    }

    @Test
    @Order(4)
    void testFqdnDerived() {
        RuntimeEnvironment env = RuntimeAutoConfigurePreStartup.current().orElseThrow();
        assertEquals("wallet-api.happydesert-abc123.eastus.azurecontainerapps.io", env.fqdn());
    }

    @Test
    @Order(5)
    void testDnsSuffixExtra() {
        RuntimeEnvironment env = RuntimeAutoConfigurePreStartup.current().orElseThrow();
        assertEquals("happydesert-abc123.eastus.azurecontainerapps.io",
                env.extra("dnsSuffix").orElse(""));
    }
}

