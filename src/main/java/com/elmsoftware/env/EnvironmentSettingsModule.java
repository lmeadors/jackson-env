package com.elmsoftware.env;

import com.elmsoftware.env.settingproviderimpl.NoOpSettingProvider;
import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.name.Names;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;
import java.util.Map;

public class EnvironmentSettingsModule implements Module {

	private static final Logger log = LoggerFactory.getLogger(EnvironmentSettingsModule.class);

	private final Util util;
	private final SettingProvider settingProvider;

	public EnvironmentSettingsModule() {
		this(new NoOpSettingProvider(), new Util());
	}

	public EnvironmentSettingsModule(
			final SettingProvider settingProvider
	) {
		this(settingProvider, new Util());
	}

	public EnvironmentSettingsModule(
			final SettingProvider settingProvider,
			final Util util
	) {
		this.settingProvider = settingProvider;
		this.util = util;
	}

	@Override
	public void configure(final Binder binder) {

		// Load up all named properties
		final String environment = util.determineEnvironment(EnvironmentSettings.ENV_VAR);

		final String resourceName = System.getProperty("environment.json", "environment.json");

		log.debug("Loading environment {} using resource {}", environment, resourceName);
		final EnvironmentSettings settings = EnvironmentSettings.load(resourceName);
		overrideEnvironmentSettings(settings, environment);
		final Map<String, String> properties = settings.merge(environment, settingProvider);

		Names.bindProperties(binder, properties);
		configure(binder, properties);

	}

	protected void configure(final Binder binder, final Map<String, String> properties) {

	}

	protected void overrideEnvironmentSettings(final EnvironmentSettings environmentSettings, final String environment) {
		final Map<String, String> settings = environmentSettings.getEnvironmentSettings().get(environment);

		settings.keySet()
			.forEach(key -> {
				final String envKey = key.replaceAll("[^A-Za-z0-9]", "_").toUpperCase(Locale.US);
				final String envValue = System.getenv(envKey);
				if (envValue != null) {
					log.debug("Overriding " + key + " with environment variable");
					environmentSettings.withEnvironmentSetting(environment, key, envValue);
				}
			});
	}

}
