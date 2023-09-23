package com.elmsoftware.env;

import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class SettingPostProcessorTest {

	@Test
	public void legacy_settingPostProcessor_behavior_unchanged() {
		// setup test
		final SettingPostProcessor processor = new SettingPostProcessor() {
		};
		final Properties properties = new Properties();

		// run test
		@SuppressWarnings("deprecation")
		final Properties processedProperties = processor.process(properties);

		// verify outcome
		assertSame(properties, processedProperties);
	}

	@Test
	public void legacy_settingPostProcessor_method_used() {
		// setup test
		final Properties properties = new Properties();
		final SettingPostProcessor processor = new SettingPostProcessor() {
			@Override
			@SuppressWarnings("deprecation")
			public Properties process(Properties properties) {
				properties.put("test.property", "test.value");
				return properties;
			}
		};

		// run test
		final Properties processedProperties = processor.apply(properties);

		// verify outcome
		assertTrue(processedProperties.containsKey("test.property"));
	}

}
