package com.elmsoftware.env;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.Map;

public class Util {

	private static final Logger log = LoggerFactory.getLogger(Util.class);

	/// minor hack for testing :(
	private Map<String, String> osEnv = System.getenv();

	public boolean isBlank(final String string) {
		return null == string || string.trim().isEmpty();
	}

	public void closeQuietly(final Closeable closeable) {
		if (null != closeable) {
			try {
				closeable.close();
			} catch (final IOException e) {
				log.warn("Unable to close: {}", e);
			}
		}
	}

	/**
	 * This can go a few ways:
	 * <p>
	 * The environment name can come from a JVM property named "environment" or custom JVM property set at
	 * "com.elmsoftware.env" can specify an alternate JVM property name to use.
	 * <p>
	 * The environment name can come from the OS environment variable named "environment".
	 * <p>
	 * As soon as a non-null value is found, we stop looking - the order is JVM arg, then OS environment. If no value is
	 * found in either location, the string "LOCAL" is returned.
	 *
	 * @param systemVariableName - the name of the JVM property that specifies the environment name
	 * @return - the environment name (i.e., "development", "dev", "prod", etc...)
	 */
	public String determineEnvironment(final String systemVariableName) {

		// this is the alternate name to use
		final String systemEnvironmentVariableName = System.getProperty(systemVariableName);
		log.trace("alternate variable name is {} (from {})", systemEnvironmentVariableName, systemVariableName);

		final String environmentName;
		if (isBlank(systemEnvironmentVariableName)) {
			environmentName = System.getProperty("environment");
		} else {
			environmentName = System.getProperty(systemEnvironmentVariableName);
		}

		final String finalName;
		if (isBlank(environmentName)) {
			finalName = osEnv.getOrDefault("environment", "LOCAL");
		} else {
			finalName = environmentName;
		}

		log.trace("environment name is {}", environmentName);

		return finalName;

	}

}
