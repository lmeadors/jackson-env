package com.elmsoftware.env.settingproviderimpl;

import com.elmsoftware.env.SettingProvider;

public class ChainingSettingProvider implements SettingProvider {

	private final SettingProvider[] settingProviderArray;

	public ChainingSettingProvider(final SettingProvider... settingProviderArray) {
		this.settingProviderArray = settingProviderArray;
	}

	@Override
	public String getProperty(final String environment, final String key) {

		for (final SettingProvider settingProvider : settingProviderArray) {

			final String value = settingProvider.getProperty(environment, key);

			if (null != value) {
				return value;
			}

		}

		return null;

	}

}
