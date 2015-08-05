package com.elmsoftware.env;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class EnvironmentSettingsResolver {

	private static final Logger log = LoggerFactory.getLogger(EnvironmentSettingsResolver.class);

	public EnvironmentSettingsResolver(final String resourceName) {
		this(resourceName, "local." + resourceName);
	}

	public EnvironmentSettingsResolver(final String resourceName, final String localResourceName) {

		final String environment = System.getProperty("environment", "LOCAL");

		final Map<String, String> properties = EnvironmentSettings.load(resourceName).merge(environment);

		final EnvironmentSettings localSettings = EnvironmentSettings.load(localResourceName);
		if (null != localSettings) {
			log.info("adding local properties from {}", localResourceName);
			final Map<String, String> localProperties = localSettings.merge(environment);
			for (final String localKey : localProperties.keySet()) {
				final String value = localProperties.get(localKey);
				log.debug("replacing shared property '{}' with value of '{}' (was '{}')", localKey, value, properties.get(localKey));
				properties.put(localKey, value);
			}
		}

		log.debug("Adding properties to system configuration from environment {}: \n{}", environment, properties);

		System.getProperties().putAll(properties);

	}

}
