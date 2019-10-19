package com.elmsoftware.env;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.*;

public class EnvironmentSettingsTest {

	private final ObjectMapper objectMapper = new ObjectMapper();

	private static final Logger log = LoggerFactory.getLogger(EnvironmentSettingsTest.class);

	@Before
	public void beforeEnvTest() {
		objectMapper.getFactory().enable(JsonParser.Feature.ALLOW_COMMENTS);
	}

	@Test
	public void show_example_json() throws JsonProcessingException {

		// setup test
		// add some settings
		final EnvironmentSettings environmentSettings = new EnvironmentSettings()
				.withRequiredSetting("some.required.key")
				.withRequiredSetting("this.is.required")
				.withGlobalSetting("an.integer", "234")
				.withEnvironmentSetting("PROD", "env.key", "env.prod.value")
				.withEnvironmentSetting("LOCAL", "an.integer", "123")
				.withGlobalSetting("some.key", "default.global.value")
				.withGlobalSetting("a.boolean", "true")
				.withGlobalSetting("some.optional.key", "default.value");

		// run test
		final String exampleJson = objectMapper.writeValueAsString(environmentSettings);
		log.info("exampleJson: \n{}", exampleJson);

	}

	@Test
	public void should_load_good_env_with_comments() throws IOException {

		// setup test
		final EnvironmentSettings settings = EnvironmentSettings.load("environment-test.json");

		// run tests and verify outcomes

		// all environments should merge OK
		final Map<String, String> mergedProd = settings.merge("PROD");
		final Map<String, String> mergedTest = settings.merge("TEST");
		final Map<String, String> mergedDev = settings.merge("DEV");
		final Map<String, String> mergedLocal = settings.merge("LOCAL");

		final List<Map<String, String>> allSettings = Arrays.asList(
				mergedProd, mergedTest, mergedDev, mergedLocal
		);

		// all environments should share these values:
		for (final Map<String, String> env : allSettings) {
			assertTrue(Boolean.valueOf(env.get("a.boolean")));
			assertTrue(Boolean.valueOf(env.get("this.is.required")));
			assertEquals("default.global.value", env.get("some.key"));
		}

		// these are the default
		assertEquals("234", mergedProd.get("an.integer"));
		assertEquals("234", mergedTest.get("an.integer"));
		assertEquals("234", mergedDev.get("an.integer"));

		// this one is non-default
		assertEquals("123", mergedLocal.get("an.integer"));

		// this is only in prod:
		assertEquals("env.prod.value", mergedProd.get("env.key"));
		assertNull(mergedTest.get("env.key"));
		assertNull(mergedDev.get("env.key"));
		assertNull(mergedLocal.get("env.key"));

	}

	@Test
	public void should_return_null_if_settings_do_not_exist() {

		// setup test - none required
		// run test
		final EnvironmentSettings settings = EnvironmentSettings.load("this_does_not_exist.json");

		// verify outcome
		assertNull(settings);

	}

	@Test(expected = RuntimeException.class)
	public void should_explode_if_unable_to_load_settings() {

		// setup test - none required
		// run test
		EnvironmentSettings.load("crap_file.json");

		// verify outcome
		fail("this should have already exploded.");

	}

	@Test
	public void should_use_settings_provider_to_load_missing_settings() {

		// setup test

		// we need a required setting that isn't in the json file
		final EnvironmentSettings settings = EnvironmentSettings
				.load("environment-test.json")
				.withRequiredSetting("settings-provider-test");

		// here's a simple provider...
		final SettingProvider provider = new SettingProvider() {
			@Override
			public String getProperty(final String environment, final String key) {
				return environment + ":" + key;
			}
		};

		// run test

		final Map<String, String> prodEnv = settings.merge("PROD", provider);

		// verify outcome
		System.out.println(prodEnv);
		assertEquals("PROD:settings-provider-test", prodEnv.get("settings-provider-test"));

	}

	@Test
	public void should_detect_and_report_single_missing_vm_arg() {

		// setup test
		final String key1 = UUID.randomUUID().toString();

		// load our test file - but add some required VM args on the fly
		final EnvironmentSettings settings = EnvironmentSettings.load("environment-test.json")
				.withRequiredSetting(key1);

		// run test and verify outcome
		try {
			settings.merge("PROD");
		} catch (final RuntimeException e) {
			assertEquals("Missing required settings: " + key1, e.getMessage());
		}

	}

	@Test
	public void should_detect_and_report_missing_vm_args() {

		// setup test
		final String key1 = UUID.randomUUID().toString();
		final String key2 = UUID.randomUUID().toString();

		// load our test file - but add some required VM args on the fly
		final EnvironmentSettings settings = EnvironmentSettings.load("environment-test.json")
				.withRequiredSetting(key1)
				.withRequiredSetting(key2);

		// run test and verify outcome
		try {
			settings.merge("PROD");
		} catch (final RuntimeException e) {
			assertEquals("Missing required settings: " + key1 + ", " + key2, e.getMessage());
		}

	}

	@Test
	public void should_include_VM_args() {

		// setup test
		final String key = UUID.randomUUID().toString();
		final String value = UUID.randomUUID().toString();
		System.setProperty(key, value);
		final EnvironmentSettings settings = EnvironmentSettings.load("environment-test.json")
				.withRequiredSetting(key);

		// run test
		final Map<String, String> objectMap = settings.merge("PROD");

		// verify outcome
		assertEquals(value, objectMap.get(key));

	}

	@Test
	public void should_lazy_init_env_specific_settings() {

		// setup test
		final String key1 = UUID.randomUUID().toString();
		final String value1 = UUID.randomUUID().toString();
		final String key2 = UUID.randomUUID().toString();
		final String value2 = UUID.randomUUID().toString();
		final EnvironmentSettings settings = EnvironmentSettings.load("environment-test.json");

		// run tests and verify outcomes

		// this isn't in the config file, so it should be null
		assertNull(settings.getEnvironmentSettings().get("DEV"));

		// setting a value should work anyway - and the map should be non-null after it is set
		settings.withEnvironmentSetting("DEV", key1, value1);
		final Map<String, String> devMap = settings.getEnvironmentSettings().get("DEV");
		assertNotNull(devMap);
		assertEquals(value1, devMap.get(key1));

		// setting another value should not re-create the map
		settings.withEnvironmentSetting("DEV", key2, value2);
		assertEquals(devMap, settings.getEnvironmentSettings().get("DEV"));
		assertEquals(value2, settings.getEnvironmentSettings().get("DEV").get(key2));

	}

	@Test
	public void should_detect_and_report_single_missing_output_key() {

		// setup test
		final String key = UUID.randomUUID().toString();
		final EnvironmentSettings settings = EnvironmentSettings.load("environment-test.json")
				.withRequiredSetting(key);

		// run test and verify outcome
		try {
			settings.merge("PROD");
		} catch (final RuntimeException e) {
			assertEquals("Missing required settings: " + key, e.getMessage());
		}

	}

	@Test
	public void should_detect_and_report_multiple_missing_output_keys() {

		// setup test
		final String key1 = UUID.randomUUID().toString();
		final String key2 = UUID.randomUUID().toString();
		final EnvironmentSettings settings = EnvironmentSettings.load("environment-test.json")
				.withRequiredSetting(key1)
				.withRequiredSetting(key2);

		// run test and verify outcome
		try {
			settings.merge("PROD");
		} catch (final RuntimeException e) {
			assertEquals("Missing required settings: " + key1 + ", " + key2, e.getMessage());
		}

	}

	@Test
	public void should_check_VM_arg_for_missing_required_properties() {

		// setup test
		final String key = UUID.randomUUID().toString();
		final String value = UUID.randomUUID().toString();
		System.setProperty(key, value);

		final EnvironmentSettings settings = EnvironmentSettings.load("environment-test.json")
				.withRequiredSetting(key);

		// run test
		final Map<String, String> objectMap = settings.merge("PROD");

		// verify outcome
		assertEquals(value, objectMap.get(key));

	}

	@Test
	public void should_replace_default_values_with_vm_args_if_defined() {

		// setup test
		final String key = UUID.randomUUID().toString();
		final String value1 = UUID.randomUUID().toString();
		final String value2 = UUID.randomUUID().toString();
		final EnvironmentSettings settings = new EnvironmentSettings().withGlobalSetting(key, value1);
		System.setProperty(key, value2);

		// run test
		final Map<String, String> objectMap = settings.merge("PROD");

		// verify outcome
		assertEquals(value2, objectMap.get(key));

	}

	@Test
	public void should_replace_environment_values_with_vm_args_if_defined() {

		// setup test
		final String key = UUID.randomUUID().toString();
		final String value1 = UUID.randomUUID().toString();
		final String value2 = UUID.randomUUID().toString();
		final String environment = "PROD";
		final EnvironmentSettings settings = new EnvironmentSettings().withEnvironmentSetting(environment, key, value1);
		System.setProperty(key, value2);

		// run test
		final Map<String, String> objectMap = settings.merge(environment);

		// verify outcome
		assertEquals(value2, objectMap.get(key));

	}

	@Test
	public void should_warn_about_duplicated_values() {
		// setup test
		final String environment = "test";
		final String value = "value";
		final String keyName = "key_name";
		final EnvironmentSettings settings = new EnvironmentSettings()
				.withGlobalSetting(keyName, value)
				.withSensitiveSetting(keyName)
				.withEnvironmentSetting(environment, keyName, value);

		// run test
		final Map<String, String> map = settings.merge(environment);

		// verify outcome - doesn't assert anything related to the logging,
		// but it's a sanity check for the key/value handling
		assertEquals(value, map.get(keyName));

	}

}
