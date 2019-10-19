package com.elmsoftware.env;

import java.util.Properties;

public interface SettingPostProcessor {

	default Properties process(final Properties properties) {
		return properties;
	}

}
