package com.elmsoftware.env;

import java.util.Properties;
import java.util.function.Function;

/**
 * @deprecated use a {@link Function} instead;
 */
@Deprecated
public interface SettingPostProcessor extends Function<Properties, Properties> {

	/**
	 * @deprecated use {@link #apply(Properties)}} instead.
	 */
	@Deprecated
	default Properties process(final Properties properties) {
		return properties;
	}

	@Override
	default Properties apply(Properties properties) {
		return process(properties);
	}
}
