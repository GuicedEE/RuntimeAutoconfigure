package com.guicedee.runtime.autoconfigure.test;

import com.guicedee.runtime.autoconfigure.CloudProvider;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link CloudProvider} enum.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CloudProviderTest {

    @Test
    @Order(1)
    void testAutoHasEmptyId() {
        assertEquals("", CloudProvider.AUTO.id());
    }

    @Test
    @Order(2)
    void testAllProvidersHaveUniqueIds() {
        var ids = java.util.Arrays.stream(CloudProvider.values())
                .filter(p -> p != CloudProvider.AUTO)
                .map(CloudProvider::id)
                .toList();
        assertEquals(ids.size(), ids.stream().distinct().count(), "All provider IDs must be unique");
    }

    @Test
    @Order(3)
    void testFromIdReturnsCorrectProvider() {
        assertEquals(CloudProvider.AZURE_CONTAINER_APPS, CloudProvider.fromId("azure-container-apps"));
        assertEquals(CloudProvider.AWS_ECS, CloudProvider.fromId("aws-ecs"));
        assertEquals(CloudProvider.AWS_LAMBDA, CloudProvider.fromId("aws-lambda"));
        assertEquals(CloudProvider.GCP_CLOUD_RUN, CloudProvider.fromId("gcp-cloud-run"));
        assertEquals(CloudProvider.DIGITALOCEAN_APP_PLATFORM, CloudProvider.fromId("digitalocean-app-platform"));
        assertEquals(CloudProvider.KUBERNETES, CloudProvider.fromId("kubernetes"));
    }

    @Test
    @Order(4)
    void testFromIdReturnsAutoForNull() {
        assertEquals(CloudProvider.AUTO, CloudProvider.fromId(null));
    }

    @Test
    @Order(5)
    void testFromIdReturnsAutoForEmpty() {
        assertEquals(CloudProvider.AUTO, CloudProvider.fromId(""));
    }

    @Test
    @Order(6)
    void testFromIdReturnsAutoForUnknown() {
        assertEquals(CloudProvider.AUTO, CloudProvider.fromId("some-unknown-platform"));
    }

    @Test
    @Order(7)
    void testProviderIds() {
        assertEquals("azure-container-apps", CloudProvider.AZURE_CONTAINER_APPS.id());
        assertEquals("aws-ecs", CloudProvider.AWS_ECS.id());
        assertEquals("aws-lambda", CloudProvider.AWS_LAMBDA.id());
        assertEquals("gcp-cloud-run", CloudProvider.GCP_CLOUD_RUN.id());
        assertEquals("digitalocean-app-platform", CloudProvider.DIGITALOCEAN_APP_PLATFORM.id());
        assertEquals("kubernetes", CloudProvider.KUBERNETES.id());
    }
}

