package com.guicedee.runtime.autoconfigure.providers;

import com.guicedee.runtime.autoconfigure.RuntimeEnvironment;
import com.guicedee.runtime.autoconfigure.RuntimeEnvironmentProvider;
import lombok.extern.log4j.Log4j2;

import java.util.HashMap;
import java.util.Map;


/**
 * Detects DigitalOcean App Platform runtime environment.
 * <p>
 * Detection rule: {@code DIGITALOCEAN_APP_NAME} must be present.
 * <p>
 * Mapping:
 * <ul>
 *   <li>serviceName → DIGITALOCEAN_APP_COMPONENT_NAME or DIGITALOCEAN_APP_NAME</li>
 *   <li>serviceId → HOSTNAME</li>
 *   <li>hostname → DIGITALOCEAN_APP_DOMAIN or component name</li>
 *   <li>port → PORT (default 8080)</li>
 *   <li>region → DIGITALOCEAN_REGION</li>
 *   <li>extras.fqdn, appUrl, appId</li>
 * </ul>
 */
@Log4j2
public class DigitalOceanAppPlatformEnvironmentProvider extends AbstractEnvironmentProvider implements RuntimeEnvironmentProvider {

    @Override
    public boolean detected() {
        return has("DIGITALOCEAN_APP_NAME");
    }

    @Override
    public RuntimeEnvironment runtimeEnvironment() {
        var appName = env("DIGITALOCEAN_APP_NAME", "");
        var componentName = env("DIGITALOCEAN_APP_COMPONENT_NAME", appName);
        var domain = env("DIGITALOCEAN_APP_DOMAIN", "");
        var port = envInt("PORT", 8080);
        var region = env("DIGITALOCEAN_REGION", env("DO_REGION", ""));

        Map<String, String> extras = new HashMap<>();
        ifPresent("DIGITALOCEAN_APP_URL", v -> extras.put("appUrl", v));
        extras.put("fqdn", domain);
        ifPresent("DIGITALOCEAN_APP_ID", v -> extras.put("appId", v));

        return new RuntimeEnvironment(
                "digitalocean-app-platform",
                componentName,
                env("HOSTNAME", componentName),
                domain.isEmpty() ? componentName : domain,
                port,
                "",
                env("HOSTNAME", ""),
                region,
                "",
                Map.copyOf(extras)
        );
    }

    @Override
    public String providerId() {
        return "digitalocean-app-platform";
    }

    @Override
    public Integer sortOrder() {
        return 100;
    }
}

