package com.guicedee.runtime.autoconfigure.providers;

import com.guicedee.runtime.autoconfigure.RuntimeEnvironment;
import com.guicedee.runtime.autoconfigure.RuntimeEnvironmentProvider;
import lombok.extern.log4j.Log4j2;

import java.util.HashMap;
import java.util.Map;


/**
 * Detects Fly.io runtime environment.
 * <p>
 * Detection rule: {@code FLY_APP_NAME} must be present.
 * <p>
 * Mapping:
 * <ul>
 *   <li>serviceName → FLY_APP_NAME</li>
 *   <li>serviceId → FLY_ALLOC_ID</li>
 *   <li>hostname → FLY_APP_NAME + ".fly.dev"</li>
 *   <li>port → PORT or 8080</li>
 *   <li>region → FLY_REGION</li>
 *   <li>extras.machineId → FLY_MACHINE_ID</li>
 *   <li>extras.publicIp → FLY_PUBLIC_IP</li>
 * </ul>
 */
@Log4j2
public class FlyIoEnvironmentProvider extends AbstractEnvironmentProvider implements RuntimeEnvironmentProvider {

    @Override
    public boolean detected() {
        return has("FLY_APP_NAME");
    }

    @Override
    public RuntimeEnvironment runtimeEnvironment() {
        var appName = env("FLY_APP_NAME", "");
        var allocId = env("FLY_ALLOC_ID", "");
        var region = env("FLY_REGION", "");
        var port = envInt("PORT", 8080);

        Map<String, String> extras = new HashMap<>();
        extras.put("fqdn", appName + ".fly.dev");
        ifPresent("FLY_MACHINE_ID", v -> extras.put("machineId", v));
        ifPresent("FLY_PUBLIC_IP", v -> extras.put("publicIp", v));
        ifPresent("FLY_IMAGE_REF", v -> extras.put("imageRef", v));
        ifPresent("PRIMARY_REGION", v -> extras.put("primaryRegion", v));

        return new RuntimeEnvironment(
                "fly-io",
                appName,
                allocId,
                appName + ".fly.dev",
                port,
                "",
                allocId,
                region,
                "",
                Map.copyOf(extras)
        );
    }

    @Override
    public String providerId() {
        return "fly-io";
    }

    @Override
    public Integer sortOrder() {
        return 100;
    }
}

