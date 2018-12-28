package com.elmsoftware.env;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * @deprecated Use {@link EnvironmentSettingsConfig} instead.
 */
@Deprecated
public class EnvironmentSettingsResolver {

	private static final Logger log = LoggerFactory.getLogger(EnvironmentSettingsResolver.class);

	public EnvironmentSettingsResolver(
			final String resourceName,
			final Util util,
			final boolean allowOverride
	) {
		this(resourceName, "local." + resourceName, util, allowOverride, new NoOpSettingProvider());
	}

	public EnvironmentSettingsResolver(
			final String resourceName,
			final Util util
	) {
		this(resourceName, "local." + resourceName, util, true, new NoOpSettingProvider());
	}

	public EnvironmentSettingsResolver(
			final String resourceName,
			final String localResourceName,
			final Util util,
			final boolean allowOverride,
			final SettingProvider settingProvider
	) {

		final String environment = util.determineEnvironment(EnvironmentSettings.ENV_VAR);

		final Map<String, String> properties = EnvironmentSettings
				.load(resourceName)
				.merge(environment, allowOverride, settingProvider);

		final EnvironmentSettings localSettings = EnvironmentSettings.load(localResourceName);
		util.mergeProperties(environment, properties, localResourceName, localSettings, settingProvider);

		log.debug("Adding properties to system configuration from environment {}: \n{}", environment, properties);

		System.getProperties().putAll(properties);

	}

}
