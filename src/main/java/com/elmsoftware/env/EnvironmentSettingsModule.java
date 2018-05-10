package com.elmsoftware.env;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.name.Names;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	public void configure(Binder binder) {

		// Load up all named properties
		final String environment = util.determineEnvironment(EnvironmentSettings.ENV_VAR);

		final String resourceName = System.getProperty("environment.json", "environment.json");

		log.debug("Loading environment {} using resource {}", environment, resourceName);
		final EnvironmentSettings settings = EnvironmentSettings.load(resourceName);
		final Map<String, String> properties = settings.merge(environment);

		final String localResourceName = System.getProperty("local.environment.json", "local.environment.json");
		log.debug("trying to load local environment using {}", localResourceName);
		final EnvironmentSettings localSettings = EnvironmentSettings.load(localResourceName);
		util.mergeProperties(environment, properties, localResourceName, localSettings, settingProvider);

		Names.bindProperties(binder, properties);
		configure(binder, properties);

	}

	protected void configure(final Binder binder, final Map<String, String> properties) {

	}

}
