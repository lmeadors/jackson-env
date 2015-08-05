package com.elmsoftware.env;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.elmsoftware.env.Util.closeQuietly;
import static com.elmsoftware.env.Util.isBlank;

public class EnvironmentSettings {

	private static final Logger log = LoggerFactory.getLogger(EnvironmentSettings.class);
	public static final String DUPLICATE_GLOBAL_VARIABLE = "The value for '{}' is '{}' in both the " +
			"global and '{}' environments; you can remove the duplicate value in the '{}' " +
			"environment to simplify your configuration file.";

	private Map<String, String> globalSettings = new HashMap<String, String>();
	private Map<String, Map<String, String>> environmentSettings = new HashMap<String, Map<String, String>>();
	private List<String> requiredSettings = new ArrayList<String>();

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
				closeQuietly(inputStream);
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

	public EnvironmentSettings withEnvironmentSetting(
			final String environment,
			final String key,
			final String value
	) {
		if (!environmentSettings.containsKey(environment)) {
			environmentSettings.put(environment, new HashMap<String, String>());
		}
		environmentSettings.get(environment).put(key, value);
		return this;
	}

	/**
	 * Merges and validates the keys and values.
	 *
	 * @return A map of name / value pairs
	 */
	public Map<String, String> merge(final String environment) {

		// our merged set of values
		final Map<String, String> objectMap = new HashMap<String, String>();

		log.debug("Adding global values to merged results: {}", globalSettings);
		objectMap.putAll(globalSettings);

		final Map<String, String> environmentValues = environmentSettings.get(environment);
		log.debug("Adding environment values to merged results: {}", environmentValues);
		if (null != environmentValues) {
			log.debug("Checking for duplicates in environment values");
			for (final String key : environmentValues.keySet()) {
				final String globalValue = objectMap.get(key);
				if (null != globalValue) {
					if (environmentValues.get(key).equals(globalValue)) {
						log.warn(
								DUPLICATE_GLOBAL_VARIABLE,
								key, globalValue, environment, environment);
					}
				}
			}
			objectMap.putAll(environmentValues);
		}

		log.debug("Verifying that the required properties are present: {}", requiredSettings);
		final StringBuilder missingSettings = new StringBuilder("");
		for (final String key : requiredSettings) {
			final String value = objectMap.get(key);
			if (null == value) {
				// check the VM args for the setting
				final String property = System.getProperty(key);
				if (!isBlank(property)) {
					log.info("Found missing config property {} in VM properties as '{}'", key, property);
					objectMap.put(key, property);
				} else {
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
		for (final String key : objectMap.keySet()) {
			final String property = System.getProperties().getProperty(key);
			if (null != property) {
				final String oldValue = objectMap.get(key);
				if (!oldValue.equals(property)) {
					log.info("Replacing config property {} (old value: '{}') with VM property value '{}'", key, oldValue, property);
					objectMap.put(key, property);
				}
			}
		}

		return objectMap;

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

}
