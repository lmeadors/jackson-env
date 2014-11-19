package com.elmsoftware.env;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class EnvironmentSettingsResolver {

	private static final Logger log = LoggerFactory.getLogger(EnvironmentSettingsResolver.class);

	public EnvironmentSettingsResolver(final String resourceName) {

		final String environment = System.getProperty("environment", "LOCAL");

		final Map<String, String> properties = EnvironmentSettings.load(resourceName).merge(environment);

		log.debug("Adding properties to system configuration from environment {}: \n{}", environment, properties);

		System.getProperties().putAll(properties);

	}

}
