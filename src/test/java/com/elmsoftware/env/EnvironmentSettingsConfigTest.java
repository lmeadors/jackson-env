package com.elmsoftware.env;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.stubbing.Answer;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;

import java.util.Optional;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class EnvironmentSettingsConfigTest {

	@Mock
	private SettingProvider provider;
	@Mock
	private Util util;
	@Mock
	private SettingPostProcessor settingPostProcessor;
	@Mock
	private ConfigurableEnvironment configurableEnvironment;

	private EnvironmentSettingsConfig config;
	private MutablePropertySources propertySources;

	@Before
	public void beforeEnvironmentSettingsConfigTest() {
		initMocks(this);
		propertySources = new MutablePropertySources();
		when(configurableEnvironment.getPropertySources()).thenReturn(propertySources);
		config = new EnvironmentSettingsConfig(configurableEnvironment);
	}

	@Test
	public void should_use_provided_util_and_provider() {

		// setup
		final Optional<Util> optionalUtil = Optional.of(util);
		final Optional<SettingProvider> optionalProvider = Optional.of(provider);
		final Optional<SettingPostProcessor> optionalSettingPostProcessor = Optional.of(settingPostProcessor);
		System.setProperty("environment.json", "environment-test.json");

		when(util.determineEnvironment(EnvironmentSettings.ENV_VAR)).thenReturn("PROD");
		when(settingPostProcessor.process(any()))
			.thenAnswer((Answer<Properties>) invocation -> invocation.getArgumentAt(0, Properties.class));

		// run test
		final EnvironmentSettings settings = config.environmentSettings(
			optionalUtil,
			optionalProvider,
			optionalSettingPostProcessor
		);

		// verify mocks / capture values
		verify(util).determineEnvironment(EnvironmentSettings.ENV_VAR);
		verify(configurableEnvironment).getPropertySources();
		verify(settingPostProcessor).process(any());
		verifyNoMoreInteractions(util, provider, configurableEnvironment);

		// assert results
		final PropertySource<Properties> propertySource = (PropertySource<Properties>) propertySources.get("environment-test.json/PROD");
		final Properties source = propertySource.getSource();
		assertEquals(5, source.size());

	}

	@Test
	public void should_create_util_and_provider_if_null() {

		// setup
		System.setProperty("environment.json", "environment-test.json");

		when(util.determineEnvironment(EnvironmentSettings.ENV_VAR)).thenReturn("PROD");

		// run test
		final EnvironmentSettings settings = config.environmentSettings(
				Optional.empty(), Optional.empty(), Optional.empty()
		);

		// verify mocks / capture values
		verify(configurableEnvironment).getPropertySources();
		verifyNoMoreInteractions(util, provider, configurableEnvironment);

		// assert results
		final PropertySource<Properties> propertySource = (PropertySource<Properties>) propertySources.get("environment-test.json/LOCAL");
		final Properties source = propertySource.getSource();
		assertEquals(4, source.size());

	}

}
