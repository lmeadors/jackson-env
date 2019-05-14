package com.elmsoftware.env.settingproviderimpl;

import com.elmsoftware.env.SettingProvider;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class MapSettingProvider implements SettingProvider {

	private final Map<String, String> properties;

	public MapSettingProvider(final Properties properties) {
		this.properties = new HashMap<>();
		for (final Object key : properties.keySet()) {
			this.properties.put(key.toString(), properties.getProperty(key.toString()));
		}
	}

	public MapSettingProvider(final Map<String, String> properties) {
		this.properties = properties;
	}

	@Override
	public String getProperty(final String environment, final String key) {
		return properties.get(key);
	}

}
