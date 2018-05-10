package com.elmsoftware.env;

public class NoOpSettingProvider implements SettingProvider {

	@Override
	public String getProperty(final String environment, final String key) {
		return null;
	}

}
