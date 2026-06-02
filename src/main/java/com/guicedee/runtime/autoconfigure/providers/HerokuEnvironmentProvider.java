package com.guicedee.runtime.autoconfigure.providers;

import com.guicedee.runtime.autoconfigure.RuntimeEnvironment;
import com.guicedee.runtime.autoconfigure.RuntimeEnvironmentProvider;
import lombok.extern.log4j.Log4j2;

import java.util.HashMap;
import java.util.Map;


/**
 * Detects Heroku runtime environment.
 * <p>
 * Detection rule: {@code DYNO} must be present (injected into every Heroku dyno).
 * <p>
 * Mapping:
 * <ul>
 *   <li>serviceName → HEROKU_APP_NAME (from Dyno Metadata lab feature) or derived from DYNO</li>
 *   <li>serviceId → DYNO (e.g. "web.1")</li>
 *   <li>hostname → HEROKU_APP_NAME.herokuapp.com</li>
 *   <li>port → PORT</li>
 *   <li>extras.dynoType → type part of DYNO (e.g. "web")</li>
 *   <li>extras.releaseVersion → HEROKU_RELEASE_VERSION</li>
 * </ul>
 */
@Log4j2
public class HerokuEnvironmentProvider extends AbstractEnvironmentProvider implements RuntimeEnvironmentProvider {

    @Override
    public boolean detected() {
        return has("DYNO");
    }

    @Override
    public RuntimeEnvironment runtimeEnvironment() {
        var dyno = env("DYNO", "");
        var appName = env("HEROKU_APP_NAME", "");
        var port = envInt("PORT", 8080);
        var hostname = appName.isEmpty() ? "" : appName + ".herokuapp.com";

        // DYNO format: "web.1", "worker.2"
        var dynoType = dyno.contains(".") ? dyno.substring(0, dyno.indexOf('.')) : dyno;

        Map<String, String> extras = new HashMap<>();
        if (!hostname.isEmpty()) extras.put("fqdn", hostname);
        extras.put("dyno", dyno);
        extras.put("dynoType", dynoType);
        ifPresent("HEROKU_RELEASE_VERSION", v -> extras.put("releaseVersion", v));
        ifPresent("HEROKU_SLUG_COMMIT", v -> extras.put("slugCommit", v));
        ifPresent("HEROKU_RELEASE_CREATED_AT", v -> extras.put("releaseCreatedAt", v));

        return new RuntimeEnvironment(
                "heroku",
                appName.isEmpty() ? dynoType : appName,
                dyno,
                hostname.isEmpty() ? dyno : hostname,
                port,
                env("HEROKU_RELEASE_VERSION", ""),
                dyno,
                "",
                "",
                Map.copyOf(extras)
        );
    }

    @Override
    public String providerId() {
        return "heroku";
    }

    @Override
    public Integer sortOrder() {
        return 100;
    }
}

