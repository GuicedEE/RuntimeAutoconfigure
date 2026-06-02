package com.guicedee.runtime.autoconfigure.providers;

import com.guicedee.runtime.autoconfigure.RuntimeEnvironment;
import com.guicedee.runtime.autoconfigure.RuntimeEnvironmentProvider;
import lombok.extern.log4j.Log4j2;

import java.util.HashMap;
import java.util.Map;


/**
 * Detects Azure Container Apps runtime environment.
 * <p>
 * Detection rule: both {@code CONTAINER_APP_NAME} and {@code CONTAINER_APP_ENV_DNS_SUFFIX}
 * must be present. These are automatically injected by the Azure Container Apps platform.
 * <p>
 * Mapping:
 * <ul>
 *   <li>serviceName → CONTAINER_APP_NAME</li>
 *   <li>serviceId → CONTAINER_APP_REPLICA_NAME</li>
 *   <li>hostname → CONTAINER_APP_HOSTNAME</li>
 *   <li>port → CONTAINER_APP_PORT (default 8080)</li>
 *   <li>revision → CONTAINER_APP_REVISION</li>
 *   <li>replicaName → CONTAINER_APP_REPLICA_NAME</li>
 *   <li>extras.fqdn → CONTAINER_APP_NAME + "." + CONTAINER_APP_ENV_DNS_SUFFIX</li>
 *   <li>extras.dnsSuffix → CONTAINER_APP_ENV_DNS_SUFFIX</li>
 * </ul>
 */
@Log4j2
public class AzureContainerAppsEnvironmentProvider extends AbstractEnvironmentProvider implements RuntimeEnvironmentProvider {

    @Override
    public boolean detected() {
        return has("CONTAINER_APP_NAME") && has("CONTAINER_APP_ENV_DNS_SUFFIX");
    }

    @Override
    public RuntimeEnvironment runtimeEnvironment() {
        var appName = env("CONTAINER_APP_NAME", "");
        var dnsSuffix = env("CONTAINER_APP_ENV_DNS_SUFFIX", "");
        var replicaName = env("CONTAINER_APP_REPLICA_NAME", appName);
        var hostname = env("CONTAINER_APP_HOSTNAME", appName + "." + dnsSuffix);
        var revision = env("CONTAINER_APP_REVISION", "");
        var port = envInt("CONTAINER_APP_PORT", 8080);

        Map<String, String> extras = new HashMap<>();
        extras.put("fqdn", appName + "." + dnsSuffix);
        extras.put("dnsSuffix", dnsSuffix);
        extras.put("revision", revision);
        ifPresent("CONTAINER_APP_ENV_NAME", v -> extras.put("environmentName", v));

        return new RuntimeEnvironment(
                "azure-container-apps",
                appName,
                replicaName,
                hostname,
                port,
                revision,
                replicaName,
                env("AZURE_REGION", ""),
                "",
                Map.copyOf(extras)
        );
    }

    @Override
    public String providerId() {
        return "azure-container-apps";
    }

    @Override
    public Integer sortOrder() {
        return 100;
    }
}

