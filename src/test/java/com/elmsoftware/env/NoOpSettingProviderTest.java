package com.elmsoftware.env;

import org.junit.Test;

import static org.junit.Assert.*;

public class NoOpSettingProviderTest {

	private final SettingProvider settingProvider = new NoOpSettingProvider();

	@Test
	public void should_do_nothing_really() {
		// setup
		// run test
		assertNull(settingProvider.getProperty("fml", "snapped-the-frame"));
		// verify mocks / capture values
		// assert results
	}

}
