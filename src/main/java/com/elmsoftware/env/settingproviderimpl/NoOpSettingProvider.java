package com.elmsoftware.env.settingproviderimpl;

import com.elmsoftware.env.SettingProvider;

public class NoOpSettingProvider implements SettingProvider {

	@Override
	public String getProperty(final String environment, final String key) {
		return null;
	}

}
