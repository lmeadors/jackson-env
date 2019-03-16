package com.elmsoftware.env.settingproviderimpl;

import com.elmsoftware.env.SettingProvider;

public class SystemEnvironmentSettingProvider extends MapSettingProvider implements SettingProvider {

	public SystemEnvironmentSettingProvider() {
		super(System.getenv());
	}

}
