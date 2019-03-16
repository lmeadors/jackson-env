package com.elmsoftware.env.settingproviderimpl;

import com.elmsoftware.env.SettingProvider;

public class JvmArgSettingProvider extends MapSettingProvider implements SettingProvider {

	public JvmArgSettingProvider() {
		super(System.getProperties());
	}

}
