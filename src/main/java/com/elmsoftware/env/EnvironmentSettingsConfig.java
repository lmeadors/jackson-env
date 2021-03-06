package com.elmsoftware.env;

import com.elmsoftware.env.settingproviderimpl.JvmArgSettingProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;

import java.util.Optional;
import java.util.Properties;
import java.util.function.Supplier;

@Configuration
public class EnvironmentSettingsConfig {

	private static final Logger log = LoggerFactory.getLogger(EnvironmentSettingsConfig.class);

	private final ConfigurableEnvironment configurableEnvironment;

	@Autowired
	public EnvironmentSettingsConfig(final ConfigurableEnvironment configurableEnvironment) {
		this.configurableEnvironment = configurableEnvironment;
	}

	@Autowired
	@Bean("com.elmsoftware.env.EnvironmentSettings")
	@Lazy(false)
	public EnvironmentSettings environmentSettings(
			final Optional<Util> optionalUtil,
			final Optional<SettingProvider> optionalSettingProvider,
			final Optional<SettingPostProcessor> optionalSettingPostProcessor
	) {

		final Util util = optionalUtil.orElseGet(() -> {
			log.debug("no util supplied - creating default");
			return new Util();
		});
		final SettingProvider settingProvider = optionalSettingProvider.orElseGet(() -> {
			log.info("no setting provided supplied - using default jvm arg provider");
			return new JvmArgSettingProvider();
		});
		final SettingPostProcessor postProcessor = optionalSettingPostProcessor.orElseGet(new Supplier<SettingPostProcessor>() {
			@Override
			public SettingPostProcessor get() {
				return new SettingPostProcessor() {
					// default behavior is adequate for most cases
				};
			}
		});

		// figure out the environment name
		final String environment = util.determineEnvironment(EnvironmentSettings.ENV_VAR);
		log.trace("using environment name '{}'", environment);

		// figure out the name of the json file to use - the default is "environment.json"
		final String resourceName = System.getProperty("environment.json", "environment.json");
		log.trace("using resource name '{}'", resourceName);

		log.debug("Loading environment {} from resource {}", environment, resourceName);
		final EnvironmentSettings settings = EnvironmentSettings.load(resourceName);
		log.trace("loaded settings: {}", settings);

		// merge the global and env-specific properties
		final Properties properties = new Properties();
		properties.putAll(settings.merge(environment, settingProvider));
		log.trace("merged settings: {}", properties);

		final Properties processed = postProcessor.process(properties);

		// add the property source to spring's environment
		configurableEnvironment.getPropertySources().addLast(
				new PropertiesPropertySource(
						resourceName + "/" + environment,
						processed
				)
		);

		return settings;

	}

}
