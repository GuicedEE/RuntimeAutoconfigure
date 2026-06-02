package com.guicedee.runtime.autoconfigure.providers;

import com.guicedee.runtime.autoconfigure.RuntimeEnvironment;
import com.guicedee.runtime.autoconfigure.RuntimeEnvironmentProvider;
import lombok.extern.log4j.Log4j2;

import java.util.HashMap;
import java.util.Map;


/**
 * Detects AWS Elastic Beanstalk runtime environment.
 * <p>
 * Detection rule: {@code AWS_ELASTIC_BEANSTALK_ENVIRONMENT_NAME} or
 * {@code ELASTIC_BEANSTALK_ENVIRONMENT_NAME} must be present.
 * <p>
 * Mapping:
 * <ul>
 *   <li>serviceName → AWS_ELASTIC_BEANSTALK_ENVIRONMENT_NAME</li>
 *   <li>hostname → HOSTNAME</li>
 *   <li>port → PORT or 5000</li>
 *   <li>region → AWS_REGION</li>
 * </ul>
 */
@Log4j2
public class AwsElasticBeanstalkEnvironmentProvider extends AbstractEnvironmentProvider implements RuntimeEnvironmentProvider {

    @Override
    public boolean detected() {
        return has("AWS_ELASTIC_BEANSTALK_ENVIRONMENT_NAME") || has("ELASTIC_BEANSTALK_ENVIRONMENT_NAME");
    }

    @Override
    public RuntimeEnvironment runtimeEnvironment() {
        var envName = env("AWS_ELASTIC_BEANSTALK_ENVIRONMENT_NAME",
                env("ELASTIC_BEANSTALK_ENVIRONMENT_NAME", ""));
        var region = env("AWS_REGION", env("AWS_DEFAULT_REGION", ""));
        var port = envInt("PORT", 5000);

        Map<String, String> extras = new HashMap<>();
        ifPresent("AWS_ELASTIC_BEANSTALK_ENVIRONMENT_ID", v -> extras.put("environmentId", v));
        ifPresent("AWS_ELASTIC_BEANSTALK_PLATFORM_ARN", v -> extras.put("platformArn", v));

        return new RuntimeEnvironment(
                "aws-elastic-beanstalk",
                envName,
                env("HOSTNAME", ""),
                env("HOSTNAME", envName),
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
        return "aws-elastic-beanstalk";
    }

    @Override
    public Integer sortOrder() {
        return 110;
    }
}

