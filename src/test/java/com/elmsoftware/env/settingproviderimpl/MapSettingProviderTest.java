package com.elmsoftware.env.settingproviderimpl;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

public class MapSettingProviderTest {

	@Test
	public void should_get_properties_from_properties_object() {

		// setup
		final Properties properties = new Properties();
		properties.put("fml", "snapped-the-frame");

		final MapSettingProvider provider = new MapSettingProvider(properties);

		// run test and assert results
		assertEquals("snapped-the-frame", provider.getProperty(null, "fml"));

	}

	@Test
	public void should_get_properties_from_map_object() {

		// setup
		final Map<String,String> map = new HashMap<>();
		map.put("fml", "snapped-the-frame");

		final MapSettingProvider provider = new MapSettingProvider(map);

		// run test and assert results
		assertEquals("snapped-the-frame", provider.getProperty(null, "fml"));

	}

}
