package com.guicedee.runtime.autoconfigure;

/**
 * Enumerates the supported cloud runtime providers.
 * <p>
 * Use in {@link CloudRuntimeOptions#preferredProvider()} or programmatically
 * to identify the detected platform.
 */
public enum CloudProvider {
    /**
     * Auto-detect — first detected provider wins based on sortOrder.
     */
    AUTO(""),

    /**
     * Microsoft Azure Container Apps.
     * <p>
     * Detected via: {@code CONTAINER_APP_NAME} + {@code CONTAINER_APP_ENV_DNS_SUFFIX}
     */
    AZURE_CONTAINER_APPS("azure-container-apps"),

    /**
     * Microsoft Azure App Service / Azure Functions.
     * <p>
     * Detected via: {@code WEBSITE_SITE_NAME} (without Container Apps env vars)
     */
    AZURE_APP_SERVICE("azure-app-service"),

    /**
     * Amazon Web Services ECS / Fargate.
     * <p>
     * Detected via: {@code ECS_CONTAINER_METADATA_URI_V4} or {@code ECS_CONTAINER_METADATA_URI}
     */
    AWS_ECS("aws-ecs"),

    /**
     * Amazon Web Services Lambda.
     * <p>
     * Detected via: {@code AWS_LAMBDA_FUNCTION_NAME} (without ECS metadata present)
     */
    AWS_LAMBDA("aws-lambda"),

    /**
     * Amazon Web Services App Runner.
     * <p>
     * Detected via: {@code AWS_APP_RUNNER_SERVICE_NAME}
     */
    AWS_APP_RUNNER("aws-app-runner"),

    /**
     * Amazon Web Services Elastic Beanstalk.
     * <p>
     * Detected via: {@code AWS_ELASTIC_BEANSTALK_ENVIRONMENT_NAME}
     */
    AWS_ELASTIC_BEANSTALK("aws-elastic-beanstalk"),

    /**
     * Google Cloud Run.
     * <p>
     * Detected via: {@code K_SERVICE} + {@code K_REVISION}
     */
    GCP_CLOUD_RUN("gcp-cloud-run"),

    /**
     * Google App Engine.
     * <p>
     * Detected via: {@code GAE_APPLICATION} (without Cloud Run env vars)
     */
    GCP_APP_ENGINE("gcp-app-engine"),

    /**
     * DigitalOcean App Platform.
     * <p>
     * Detected via: {@code DIGITALOCEAN_APP_NAME}
     */
    DIGITALOCEAN_APP_PLATFORM("digitalocean-app-platform"),

    /**
     * Fly.io.
     * <p>
     * Detected via: {@code FLY_APP_NAME}
     */
    FLY_IO("fly-io"),

    /**
     * Railway.
     * <p>
     * Detected via: {@code RAILWAY_SERVICE_NAME}
     */
    RAILWAY("railway"),

    /**
     * Render.
     * <p>
     * Detected via: {@code RENDER_SERVICE_NAME} or {@code RENDER}
     */
    RENDER("render"),

    /**
     * Heroku.
     * <p>
     * Detected via: {@code DYNO}
     */
    HEROKU("heroku"),

    /**
     * Generic Kubernetes (lowest priority fallback).
     * <p>
     * Detected via: {@code KUBERNETES_SERVICE_HOST}
     */
    KUBERNETES("kubernetes");

    private final String id;

    CloudProvider(String id) {
        this.id = id;
    }

    /**
     * @return The provider ID string used in SPI matching and env var resolution.
     */
    public String id() {
        return id;
    }

    /**
     * Resolves a provider from its string ID.
     *
     * @param id The provider ID (e.g. "azure-container-apps")
     * @return The matching enum value, or {@link #AUTO} if not found.
     */
    public static CloudProvider fromId(String id) {
        if (id == null || id.isEmpty()) return AUTO;
        for (CloudProvider provider : values()) {
            if (provider.id.equals(id)) return provider;
        }
        return AUTO;
    }
}

