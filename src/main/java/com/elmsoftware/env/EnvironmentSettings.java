package com.elmsoftware.env;

import com.elmsoftware.env.settingproviderimpl.JvmArgSettingProvider;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EnvironmentSettings {

	public static final String DUPLICATE_GLOBAL_VARIABLE = "The value for '{}' is '{}' in both the "
		+ "global and '{}' environments; you can remove the duplicate value in the '{}' "
		+ "environment to simplify your configuration file.";

	public static final String ENV_VAR = "com.elmsoftware.env";

	private static final Logger log = LoggerFactory.getLogger(EnvironmentSettings.class);
	private static final Util util = new Util();

	private Map<String, String> globalSettings = new HashMap<>();
	private Map<String, Map<String, String>> environmentSettings = new HashMap<>();
	private List<String> requiredSettings = new ArrayList<>();
	// these should not be logged ever
	private List<String> sensitiveSettings = new ArrayList<>();
	private Map<String, String> aliases = new HashMap<>();

	public static EnvironmentSettings load(final String resourceName) {

		final EnvironmentSettings settings;

		final ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.getFactory().enable(JsonParser.Feature.ALLOW_COMMENTS);

		final InputStream inputStream = EnvironmentSettings.class.getClassLoader().getResourceAsStream(resourceName);

		if (null != inputStream) {
			try {
				settings = objectMapper.readValue(inputStream, EnvironmentSettings.class);
			} catch (final Exception e) {
				throw new RuntimeException(e.toString(), e);
			} finally {
				util.closeQuietly(inputStream);
			}
		} else {
			settings = null;
		}

		return settings;

	}

	public EnvironmentSettings withRequiredSetting(final String key) {
		requiredSettings.add(key);
		return this;
	}

	public EnvironmentSettings withGlobalSetting(final String key, final String value) {
		globalSettings.put(key, value);
		return this;
	}

	public EnvironmentSettings withSensitiveSetting(final String key) {
		sensitiveSettings.add(key);
		return this;
	}

	public EnvironmentSettings withEnvironmentSetting(
		final String environment,
		final String key,
		final String value
	) {
		if (!environmentSettings.containsKey(environment)) {
			environmentSettings.put(environment, new HashMap<>());
		}
		environmentSettings.get(environment).put(key, value);
		return this;
	}

	/**
	 * Merges and validates the keys and values - allows overrides using -D
	 *
	 * @param environment - the environment we want values for
	 * @return A map of name / value pairs
	 */
	public Map<String, String> merge(final String environment) {
		return merge(environment, new JvmArgSettingProvider());
	}

	/**
	 * Merges and validates the keys and values.
	 *
	 * @param environment     - the environment
	 * @param settingProvider - optional setting provider
	 * @return A map of name / value pairs
	 */
	public Map<String, String> merge(
		final String environment,
		final SettingProvider settingProvider
	) {

		// our merged set of values
		log.debug("Adding global values to merged results: {}", globalSettings.keySet());
		final Map<String, String> mergedResults = new HashMap<>(globalSettings);

		final Map<String, String> environmentValues = environmentSettings.get(environment);
		if (null != environmentValues) {
			log.debug("Adding environment values to merged results: {}", environmentValues.keySet());
			log.debug("Checking for duplicates in environment values");
			for (final String key : environmentValues.keySet()) {
				final String globalValue = mergedResults.get(key);
				if (null != globalValue) {
					if (environmentValues.get(key).equals(globalValue)) {
						log.warn(
							DUPLICATE_GLOBAL_VARIABLE,
							key, protectedValue(key, globalValue), environment, environment
						);
					}
				}
			}
			mergedResults.putAll(environmentValues);
		}

		log.debug("Verifying that the required properties are present: {}", requiredSettings);
		final StringBuilder missingSettings = new StringBuilder();
		for (final String key : requiredSettings) {
			if (!mergedResults.containsKey(key)) {
				// check the setting provider for the setting
				final String providedProperty = settingProvider.getProperty(environment, key);
				if (!util.isBlank(providedProperty)) {
					log.debug(
						"Config property {} received from {} as '{}'",
						key,
						settingProvider,
						protectedValue(key, providedProperty)
					);
					mergedResults.put(key, providedProperty);
				} else {
					// ok, it's not here
					if (missingSettings.length() > 0) {
						missingSettings.append(", ");
					}
					missingSettings.append(key);
				}
			}
		}

		if (missingSettings.length() > 0) {
			throw new RuntimeException("Missing required settings: " + missingSettings.toString());
		}

		log.debug("Checking VM options for configuration value replacements");
		for (final String key : mergedResults.keySet()) {
			final String property = System.getProperties().getProperty(key);
			if (null != property) {
				final String oldValue = mergedResults.get(key);
				if (!oldValue.equals(property)) {
					log.info(
						"Replacing config property {} (old value: '{}') with VM property value '{}'",
						key,
						protectedValue(key, oldValue),
						protectedValue(key, property)
					);
					mergedResults.put(key, property);
				}
			}
		}

		// apply aliases
		log.info("applying aliases for {}", aliases.keySet());
		aliases.keySet().forEach(key -> {
			log.debug(
				"setting property '{}' to '{}' (an alias of property '{}')",
				aliases.get(key),
				protectedValue(key, mergedResults.get(key)),
				key
			);
			mergedResults.put(aliases.get(key), mergedResults.get(key));
		});

		return mergedResults;

	}

	private String protectedValue(final String key, final String value) {
		final String result;
		if (sensitiveSettings.contains(key)) {
			//noinspection ReplaceAllDot
			result = value.replaceAll(".", "*");
		} else {
			result = value;
		}
		return result;
	}

	public Map<String, String> getGlobalSettings() {
		return globalSettings;
	}

	public Map<String, Map<String, String>> getEnvironmentSettings() {
		return environmentSettings;
	}

	public List<String> getRequiredSettings() {
		return requiredSettings;
	}

	public Map<String, String> getAliases() {
		return aliases;
	}

	public void setAliases(final Map<String, String> aliases) {
		this.aliases = aliases;
	}

	public List<String> getSensitiveSettings() {
		return sensitiveSettings;
	}

	public void setSensitiveSettings(final List<String> sensitiveSettings) {
		this.sensitiveSettings = sensitiveSettings;
	}

}
