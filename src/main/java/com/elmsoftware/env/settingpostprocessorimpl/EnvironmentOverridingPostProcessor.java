package com.elmsoftware.env.settingpostprocessorimpl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;
import java.util.Properties;
import java.util.function.Function;

/**
 * Replaces loaded properties with values from the environment if present. Property names are roughly converted to match
 * the environment variable naming convention by capitalizing and replacing all non-alphanumeric characters with
 * underscores "_". Examples:
 * <ul>
 *     <li>{@code my.property} -> {@code MY_PROPERTY}</li>
 *     <li>{@code some.ssm/parameter} -> {@code SOME_SSM_PARAMETER}</li>
 *     <li>{@code monitoring.env} -> {@code MONITORING_ENV}</li>
 * </ul>
 */
public class EnvironmentOverridingPostProcessor implements Function<Properties, Properties> {

	private static final Logger log = LoggerFactory.getLogger(EnvironmentOverridingPostProcessor.class);

	@Override
	public Properties apply(final Properties properties) {
		properties.stringPropertyNames().forEach(property -> {
			final String environmentProperty = property.replaceAll("[^A-Za-z0-9]", "_").toUpperCase(Locale.US);
			final String environmentValue = System.getenv(environmentProperty);
			if (environmentValue != null) {
				log.debug("Overriding {} with environment variable.", property);
				properties.setProperty(property, environmentValue);
			}
		});

		return properties;
	}
}
