package com.guicedee.runtime.autoconfigure.providers;

import com.guicedee.runtime.autoconfigure.RuntimeEnvironment;
import com.guicedee.runtime.autoconfigure.RuntimeEnvironmentProvider;
import lombok.extern.log4j.Log4j2;

import java.util.HashMap;
import java.util.Map;


/**
 * Detects Azure App Service / Azure Functions runtime environment.
 * <p>
 * Detection rule: {@code WEBSITE_SITE_NAME} must be present (injected by both App Service and Functions).
 * Does NOT detect if {@code CONTAINER_APP_NAME} is present (that's Azure Container Apps).
 * <p>
 * Mapping:
 * <ul>
 *   <li>serviceName → WEBSITE_SITE_NAME</li>
 *   <li>serviceId → WEBSITE_INSTANCE_ID</li>
 *   <li>hostname → WEBSITE_HOSTNAME</li>
 *   <li>port → PORT or WEBSITES_PORT or 8080</li>
 *   <li>extras.slotName → WEBSITE_SLOT_NAME</li>
 *   <li>extras.sku → WEBSITE_SKU</li>
 *   <li>extras.functionsRuntime → FUNCTIONS_WORKER_RUNTIME (if Azure Functions)</li>
 * </ul>
 */
@Log4j2
public class AzureAppServiceEnvironmentProvider extends AbstractEnvironmentProvider implements RuntimeEnvironmentProvider {

    @Override
    public boolean detected() {
        return has("WEBSITE_SITE_NAME") && !has("CONTAINER_APP_NAME");
    }

    @Override
    public RuntimeEnvironment runtimeEnvironment() {
        var siteName = env("WEBSITE_SITE_NAME", "");
        var instanceId = env("WEBSITE_INSTANCE_ID", "");
        var hostname = env("WEBSITE_HOSTNAME", siteName + ".azurewebsites.net");
        var port = envInt("PORT", envInt("WEBSITES_PORT", 8080));
        var region = env("REGION_NAME", env("WEBSITE_OWNER_NAME", ""));

        boolean isFunction = has("FUNCTIONS_WORKER_RUNTIME");
        String provider = isFunction ? "azure-functions" : "azure-app-service";

        Map<String, String> extras = new HashMap<>();
        extras.put("fqdn", hostname);
        ifPresent("WEBSITE_SLOT_NAME", v -> extras.put("slotName", v));
        ifPresent("WEBSITE_SKU", v -> extras.put("sku", v));
        ifPresent("WEBSITE_RESOURCE_GROUP", v -> extras.put("resourceGroup", v));
        ifPresent("FUNCTIONS_WORKER_RUNTIME", v -> extras.put("functionsRuntime", v));
        ifPresent("FUNCTIONS_EXTENSION_VERSION", v -> extras.put("functionsVersion", v));

        return new RuntimeEnvironment(
                provider,
                siteName,
                instanceId,
                hostname,
                port,
                env("WEBSITE_DEPLOYMENT_ID", ""),
                instanceId,
                region,
                "",
                Map.copyOf(extras)
        );
    }

    @Override
    public String providerId() {
        return "azure-app-service";
    }

    @Override
    public Integer sortOrder() {
        return 110; // slightly lower priority than Azure Container Apps
    }
}

