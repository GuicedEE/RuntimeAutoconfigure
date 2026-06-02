package com.guicedee.runtime.autoconfigure.test;

import com.guicedee.runtime.autoconfigure.providers.AzureContainerAppsEnvironmentProvider;

import java.util.Optional;

/**
 * Docker integration test runner.
 * Expects Azure Container Apps env vars to be set via docker run -e flags.
 * Exits with code 0 on success, 1 on failure.
 */
public class DockerIntegrationTest {
    public static void main(String[] args) {
        System.out.println("=== GuicedEE Runtime Autoconfigure - Docker Integration Test ===");
        System.out.println();

        int failures = 0;

        // Test 1: reads real env vars
        var appName = Optional.ofNullable(System.getenv("CONTAINER_APP_NAME"));
        if (appName.isPresent() && !appName.get().isEmpty()) {
            System.out.println("✅ CONTAINER_APP_NAME detected: " + appName.get());
        } else {
            System.out.println("❌ CONTAINER_APP_NAME not detected!");
            failures++;
        }

        var dnsSuffix = Optional.ofNullable(System.getenv("CONTAINER_APP_ENV_DNS_SUFFIX"));
        if (dnsSuffix.isPresent() && !dnsSuffix.get().isEmpty()) {
            System.out.println("✅ CONTAINER_APP_ENV_DNS_SUFFIX detected: " + dnsSuffix.get());
        } else {
            System.out.println("❌ CONTAINER_APP_ENV_DNS_SUFFIX not detected!");
            failures++;
        }

        // Test 2: Provider detects
        var provider = new AzureContainerAppsEnvironmentProvider();
        if (provider.detected()) {
            System.out.println("✅ AzureContainerAppsEnvironmentProvider.detected() = true");
        } else {
            System.out.println("❌ AzureContainerAppsEnvironmentProvider.detected() = false!");
            failures++;
        }

        // Test 3: RuntimeEnvironment populated correctly
        var env = provider.runtimeEnvironment();
        System.out.println();
        System.out.println("--- RuntimeEnvironment ---");
        System.out.println("  provider    : " + env.provider());
        System.out.println("  serviceName : " + env.serviceName());
        System.out.println("  serviceId   : " + env.serviceId());
        System.out.println("  hostname    : " + env.hostname());
        System.out.println("  port        : " + env.port());
        System.out.println("  revision    : " + env.revision());
        System.out.println("  replicaName : " + env.replicaName());
        System.out.println("  fqdn        : " + env.fqdn());
        System.out.println("  region      : " + env.region());
        System.out.println();

        if (!"azure-container-apps".equals(env.provider())) { failures++; System.out.println("❌ wrong provider"); }
        else System.out.println("✅ provider = azure-container-apps");

        if (!"wallet-api".equals(env.serviceName())) { failures++; System.out.println("❌ wrong serviceName"); }
        else System.out.println("✅ serviceName = wallet-api");

        if (env.port() != 3000) { failures++; System.out.println("❌ wrong port: " + env.port()); }
        else System.out.println("✅ port = 3000");

        var expectedFqdn = "wallet-api.myenv.eastus.azurecontainerapps.io";
        if (!expectedFqdn.equals(env.fqdn())) { failures++; System.out.println("❌ wrong fqdn: " + env.fqdn()); }
        else System.out.println("✅ fqdn = " + expectedFqdn);

        System.out.println();
        if (failures == 0) {
            System.out.println("🎉 ALL TESTS PASSED");
        } else {
            System.out.println("💥 " + failures + " TEST(S) FAILED");
        }
        System.exit(failures == 0 ? 0 : 1);
    }
}



