package com.guicedee.runtime.autoconfigure.test;

import com.guicedee.client.IGuiceContext;
import com.guicedee.runtime.autoconfigure.RuntimeEnvironment;
import com.guicedee.runtime.autoconfigure.implementations.RuntimeAutoConfigurePreStartup;
import com.guicedee.runtime.autoconfigure.providers.AzureContainerAppsEnvironmentProvider;
import org.junit.jupiter.api.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Live Azure Container Apps integration test.
 * <p>
 * Uses the {@code az} CLI to fetch real container app metadata from your subscription,
 * sets the env vars as system properties to simulate running inside Azure,
 * then verifies the {@link AzureContainerAppsEnvironmentProvider} detects correctly.
 * <p>
 * Also makes a live HTTP call to the container app FQDN to verify it's reachable.
 * <p>
 * Prerequisites:
 * <ul>
 *   <li>{@code az} CLI installed and logged in</li>
 *   <li>Container apps deployed in resource group "DevSites"</li>
 * </ul>
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Tag("azure")
class AzureContainerAppsLiveTest
{
    private static final String RESOURCE_GROUP = "DevSites";
    private static final String APP_NAME = "guicedee-website";

    private String appFqdn;
    private String dnsSuffix;
    private String revision;

    @BeforeAll
    void setUp() throws Exception
    {
        // Fetch real values from Azure via CLI
        appFqdn = azQuery(APP_NAME, RESOURCE_GROUP, "properties.configuration.ingress.fqdn");
        revision = azQuery(APP_NAME, RESOURCE_GROUP, "properties.latestRevisionName");
        dnsSuffix = azEnvQuery(RESOURCE_GROUP);

        assertNotNull(appFqdn, "Could not fetch FQDN from Azure - ensure `az` is logged in");
        assertNotNull(dnsSuffix, "Could not fetch DNS suffix from Azure");
        assertFalse(appFqdn.isBlank(), "FQDN must not be blank");

        System.out.println("📋 Azure Container App: " + APP_NAME);
        System.out.println("   FQDN: " + appFqdn);
        System.out.println("   DNS Suffix: " + dnsSuffix);
        System.out.println("   Revision: " + revision);

        // Simulate the environment variables Azure injects inside the container
        System.setProperty("CONTAINER_APP_NAME", APP_NAME);
        System.setProperty("CONTAINER_APP_ENV_DNS_SUFFIX", dnsSuffix);
        System.setProperty("CONTAINER_APP_PORT", "8080");
        System.setProperty("CONTAINER_APP_REVISION", revision != null ? revision : "");
        System.setProperty("CONTAINER_APP_REPLICA_NAME", APP_NAME + "--replica-1");
        System.setProperty("CONTAINER_APP_HOSTNAME", appFqdn);
        System.setProperty("AZURE_REGION", "ukwest");

        IGuiceContext.registerModule("com.guicedee.runtime.autoconfigure.test");
        IGuiceContext.instance().inject();
    }

    @AfterAll
    void tearDown()
    {
        IGuiceContext.instance().destroy();
        System.clearProperty("CONTAINER_APP_NAME");
        System.clearProperty("CONTAINER_APP_ENV_DNS_SUFFIX");
        System.clearProperty("CONTAINER_APP_PORT");
        System.clearProperty("CONTAINER_APP_REVISION");
        System.clearProperty("CONTAINER_APP_REPLICA_NAME");
        System.clearProperty("CONTAINER_APP_HOSTNAME");
        System.clearProperty("AZURE_REGION");
    }

    @Test
    @Order(1)
    void testAzureContainerAppsDetected()
    {
        var provider = new AzureContainerAppsEnvironmentProvider();
        assertTrue(provider.detected(), "Azure Container Apps should be detected when env vars are present");
        System.out.println("✅ Azure Container Apps environment detected");
    }

    @Test
    @Order(2)
    void testRuntimeEnvironmentPopulated()
    {
        RuntimeEnvironment env = RuntimeAutoConfigurePreStartup.current().orElseThrow(
                () -> new AssertionError("RuntimeEnvironment should be detected"));

        assertEquals("azure-container-apps", env.provider());
        assertEquals(APP_NAME, env.serviceName());
        assertEquals(appFqdn, env.hostname());
        assertEquals(8080, env.port());
        assertEquals(revision, env.revision());
        assertEquals(dnsSuffix, env.extra("dnsSuffix").orElse(""));
        assertEquals("ukwest", env.region());

        System.out.println("✅ RuntimeEnvironment populated correctly:");
        System.out.println("   provider: " + env.provider());
        System.out.println("   serviceName: " + env.serviceName());
        System.out.println("   hostname: " + env.hostname());
        System.out.println("   fqdn: " + env.fqdn());
        System.out.println("   port: " + env.port());
        System.out.println("   region: " + env.region());
    }

    @Test
    @Order(3)
    void testFqdnMatchesAzure()
    {
        RuntimeEnvironment env = RuntimeAutoConfigurePreStartup.current().orElseThrow();
        String expectedFqdn = APP_NAME + "." + dnsSuffix;
        assertEquals(expectedFqdn, env.fqdn(), "FQDN should be appName.dnsSuffix");
        System.out.println("✅ FQDN derived correctly: " + env.fqdn());
    }

    @Test
    @Order(4)
    void testLiveHttpCallToContainerApp() throws Exception
    {
        // Actually call the live Azure Container App to verify it's running
        HttpClient client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://" + appFqdn + "/"))
                .GET()
                .timeout(java.time.Duration.ofSeconds(30))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertTrue(response.statusCode() >= 200 && response.statusCode() < 400,
                "Container app should respond with 2xx/3xx, got: " + response.statusCode());
        assertFalse(response.body().isEmpty(), "Response body should not be empty");

        System.out.println("✅ Live HTTP call to Azure Container App succeeded!");
        System.out.println("   URL: https://" + appFqdn + "/");
        System.out.println("   Status: " + response.statusCode());
        System.out.println("   Body length: " + response.body().length() + " bytes");
    }

    @Test
    @Order(5)
    void testSecondContainerAppAlsoReachable() throws Exception
    {
        // Verify the second container app (jwebmp-website) is also reachable
        String secondAppFqdn = azQuery("jwebmp-website", RESOURCE_GROUP, "properties.configuration.ingress.fqdn");
        assertNotNull(secondAppFqdn, "Second container app FQDN should be fetchable");

        HttpClient client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://" + secondAppFqdn + "/"))
                .GET()
                .timeout(java.time.Duration.ofSeconds(30))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertTrue(response.statusCode() >= 200 && response.statusCode() < 400,
                "jwebmp-website should respond with 2xx/3xx, got: " + response.statusCode());

        System.out.println("✅ Second container app (jwebmp-website) also reachable!");
        System.out.println("   URL: https://" + secondAppFqdn + "/");
        System.out.println("   Status: " + response.statusCode());
    }

    // ── Helper methods ──────────────────────────────────────────

    private String azQuery(String appName, String resourceGroup, String jsonPath) throws Exception
    {
        String azCmd = System.getProperty("os.name").toLowerCase().contains("win") ? "cmd.exe" : "az";
        String[] command = azCmd.equals("cmd.exe")
                ? new String[]{"cmd.exe", "/c", "az", "containerapp", "show", "-n", appName, "-g", resourceGroup, "--query", jsonPath, "-o", "tsv"}
                : new String[]{"az", "containerapp", "show", "-n", appName, "-g", resourceGroup, "--query", jsonPath, "-o", "tsv"};
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);
        Process process = pb.start();
        String result;
        try (var reader = new BufferedReader(new InputStreamReader(process.getInputStream())))
        {
            result = reader.lines().collect(Collectors.joining()).trim();
        }
        process.waitFor(15, TimeUnit.SECONDS);
        return result.isEmpty() ? null : result;
    }

    private String azEnvQuery(String resourceGroup) throws Exception
    {
        String azCmd = System.getProperty("os.name").toLowerCase().contains("win") ? "cmd.exe" : "az";
        String[] command = azCmd.equals("cmd.exe")
                ? new String[]{"cmd.exe", "/c", "az", "containerapp", "env", "list", "-g", resourceGroup, "--query", "[0].properties.defaultDomain", "-o", "tsv"}
                : new String[]{"az", "containerapp", "env", "list", "-g", resourceGroup, "--query", "[0].properties.defaultDomain", "-o", "tsv"};
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);
        Process process = pb.start();
        String result;
        try (var reader = new BufferedReader(new InputStreamReader(process.getInputStream())))
        {
            result = reader.lines().collect(Collectors.joining()).trim();
        }
        process.waitFor(15, TimeUnit.SECONDS);
        return result.isEmpty() ? null : result;
    }
}




