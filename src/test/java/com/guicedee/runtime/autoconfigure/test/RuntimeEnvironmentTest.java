package com.guicedee.runtime.autoconfigure.test;

import com.guicedee.runtime.autoconfigure.RuntimeEnvironment;
import org.junit.jupiter.api.*;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link RuntimeEnvironment} record.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RuntimeEnvironmentTest {

    @Test
    @Order(1)
    void testFqdnReturnsFqdnFromExtras() {
        var env = new RuntimeEnvironment(
                "test-provider", "my-app", "replica-1",
                "my-app.internal", 8080, "rev-1", "replica-1",
                "us-east-1", "us-east-1a",
                Map.of("fqdn", "my-app.custom.domain.com")
        );
        assertEquals("my-app.custom.domain.com", env.fqdn());
    }

    @Test
    @Order(2)
    void testFqdnFallsBackToHostname() {
        var env = new RuntimeEnvironment(
                "test-provider", "my-app", "replica-1",
                "my-app.internal", 8080, "rev-1", "replica-1",
                "us-east-1", "us-east-1a",
                Map.of()
        );
        assertEquals("my-app.internal", env.fqdn());
    }

    @Test
    @Order(3)
    void testFqdnFallsBackToHostnameWhenFqdnEmpty() {
        var env = new RuntimeEnvironment(
                "test-provider", "my-app", "replica-1",
                "my-app.internal", 8080, "rev-1", "replica-1",
                "", "",
                Map.of("fqdn", "")
        );
        assertEquals("my-app.internal", env.fqdn());
    }

    @Test
    @Order(4)
    void testExtraReturnsValueWhenPresent() {
        var env = new RuntimeEnvironment(
                "azure-container-apps", "wallet-api", "replica-abc",
                "wallet-api.env.azurecontainerapps.io", 8080, "rev-2", "replica-abc",
                "", "",
                Map.of("dnsSuffix", "env.azurecontainerapps.io", "revision", "rev-2")
        );
        Optional<String> suffix = env.extra("dnsSuffix");
        assertTrue(suffix.isPresent());
        assertEquals("env.azurecontainerapps.io", suffix.get());
    }

    @Test
    @Order(5)
    void testExtraReturnsEmptyWhenMissing() {
        var env = new RuntimeEnvironment(
                "test", "app", "id", "host", 80, "", "", "", "",
                Map.of()
        );
        assertTrue(env.extra("nonexistent").isEmpty());
    }

    @Test
    @Order(6)
    void testRecordAccessors() {
        var env = new RuntimeEnvironment(
                "gcp-cloud-run", "payments", "payments-rev3-abc",
                "payments", 8080, "payments-rev3", "payments-rev3-abc",
                "us-central1", "",
                Map.of("projectId", "my-project")
        );
        assertEquals("gcp-cloud-run", env.provider());
        assertEquals("payments", env.serviceName());
        assertEquals("payments-rev3-abc", env.serviceId());
        assertEquals("payments", env.hostname());
        assertEquals(8080, env.port());
        assertEquals("payments-rev3", env.revision());
        assertEquals("payments-rev3-abc", env.replicaName());
        assertEquals("us-central1", env.region());
        assertEquals("", env.zone());
    }
}

