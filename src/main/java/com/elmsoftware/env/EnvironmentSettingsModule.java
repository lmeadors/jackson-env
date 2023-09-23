package com.elmsoftware.env;

import com.elmsoftware.env.settingproviderimpl.NoOpSettingProvider;
import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.name.Names;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;
import java.util.stream.Collectors;

public class EnvironmentSettingsModule implements Module {

	private static final Logger log = LoggerFactory.getLogger(EnvironmentSettingsModule.class);

	private final Util util;
	private final SettingProvider settingProvider;
	private final List<Function<Properties, Properties>> settingPostProcessors;

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
		this(settingProvider, util, Function.identity());
	}

	public EnvironmentSettingsModule(
			final SettingProvider settingProvider,
			final Util util,
			final Function<Properties, Properties> settingPostProcessor
	) {
		this(settingProvider, util, Collections.singletonList(settingPostProcessor));
	}

	public EnvironmentSettingsModule(
			final SettingProvider settingProvider,
			final Util util,
			final List<Function<Properties, Properties>> settingPostProcessors
	) {
		this.util = util;
		this.settingProvider = settingProvider;
		this.settingPostProcessors = settingPostProcessors;
	}

	@Override
	public void configure(final Binder binder) {

		// Load up all named properties
		final String environment = util.determineEnvironment(EnvironmentSettings.ENV_VAR);

		final String resourceName = System.getProperty("environment.json", "environment.json");

		log.debug("Loading environment {} using resource {}", environment, resourceName);
		final EnvironmentSettings settings = EnvironmentSettings.load(resourceName);
		final Properties properties = new Properties();
		properties.putAll(settings.merge(environment, settingProvider));

		settingPostProcessors.stream()
			.reduce(Function.identity(), Function::andThen)
			.apply(properties);

		Names.bindProperties(binder, properties);
		configure(binder, properties);
	}

	protected void configure(final Binder binder, final Properties properties) {
		// This is only the default while the deprecated configure method is still here. Once that is removed this can
		// be empty.
		final Map<String, String> propertiesMap = properties.entrySet().stream()
			.filter(it -> it.getValue() instanceof String)
			.collect(Collectors.toMap(
				it -> (String) it.getKey(),
				it -> (String) it.getValue()
			));
		configure(binder, propertiesMap);
	}

	/**
	 * @deprecated use {@link #configure(Binder, Properties)} instead.
	 */
	@Deprecated
	protected void configure(final Binder binder, final Map<String, String> properties) {

	}
}
