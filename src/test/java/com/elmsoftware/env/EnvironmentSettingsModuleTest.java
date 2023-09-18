package com.elmsoftware.env;

import com.elmsoftware.env.settingpostprocessorimpl.EnvironmentOverridingPostProcessor;
import com.elmsoftware.env.settingproviderimpl.NoOpSettingProvider;
import com.google.inject.Binder;
import com.google.inject.Key;
import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import static com.github.stefanbirkner.systemlambda.SystemLambda.withEnvironmentVariable;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EnvironmentSettingsModuleTest {

	@Mock
	private Binder binder;

	@Mock
	private LinkedBindingBuilder<String> builder;

	@Mock
	private SettingProvider settingProvider;

	private Util util = new Util();

	@Captor
	private ArgumentCaptor<Key<String>> argumentCaptor;

	@Test
	public void should_bind_named_values_without_optional_overrides() {

		// setup test
		System.setProperty("environment.json", "guice-test.json");
		System.setProperty("local.environment.json", "snapped-the-frame.json");

		when(binder.bind(any(Key.class))).thenReturn(builder);
		when(binder.skipSources(Names.class)).thenReturn(binder);

		final EnvironmentSettingsModule module = new EnvironmentSettingsModule(new NoOpSettingProvider(), util);

		// run test
		module.configure(binder);

		// verify outcome
		verify(binder).skipSources(Names.class);
		verify(binder, times(2)).bind(argumentCaptor.capture());
		verifyNoMoreInteractions(binder);

		final List<String> propertyNames = argumentCaptor.getAllValues().stream()
			.map(it -> {
				assertTrue(it.hasAttributes());
				return ((Named) it.getAnnotation()).value();
			})
			.collect(Collectors.toList());

		assertTrue(propertyNames.contains("some.key"));
		assertTrue(propertyNames.contains("other.key"));
	}

	@Test
	public void should_create_util_instance_if_not_provided() throws Exception {
		// setup test
		// run test
		final EnvironmentSettingsModule module = new EnvironmentSettingsModule();
		// verify outcome
		final Field utilField = EnvironmentSettingsModule.class.getDeclaredField("util");
		assertNotNull(utilField);
		utilField.setAccessible(true);
		assertNotNull(utilField.get(module));
	}

	@Test
	public void should_use_settings_provider_if_provided() throws Exception {
		// setup test
		// run test
		final EnvironmentSettingsModule module = new EnvironmentSettingsModule(settingProvider);

		// verify outcome
		final Field utilField = EnvironmentSettingsModule.class.getDeclaredField("util");
		assertNotNull(utilField);
		utilField.setAccessible(true);
		assertNotNull(utilField.get(module));

		final Field providerField = EnvironmentSettingsModule.class.getDeclaredField("settingProvider");
		assertNotNull(providerField);
		providerField.setAccessible(true);
		assertNotNull(providerField.get(module));
	}

	@Test
	public void should_use_environment_settings_if_set() throws Exception {
		// setup test
		final String dockerEnvironmentVariable = "I'm configurable for docker!";
		final EnvironmentOverridingPostProcessor postProcessor = new EnvironmentOverridingPostProcessor();
		final EnvironmentSettingsModule module = new EnvironmentSettingsModule(new NoOpSettingProvider(), util, postProcessor);

		System.setProperty("environment.json", "guice-test.json");
		when(binder.bind(any(Key.class))).thenReturn(builder);
		when(binder.skipSources(Names.class)).thenReturn(binder);

		// run test
		withEnvironmentVariable("SOME_KEY", dockerEnvironmentVariable)
			.execute(() -> module.configure(binder));

		// verify outcome
		verify(builder).toInstance(eq(dockerEnvironmentVariable));
	}

	@Test
	public void should_use_legacy_configure_on_only_string_properties() {
		// setup test
		final String keyToObjectProperty = "other.key";
		final SettingPostProcessor objectifyingPostProcessor = new SettingPostProcessor() {
			@Override
			public Properties process(Properties properties) {
				assertTrue(properties.containsKey(keyToObjectProperty));
				properties.put(keyToObjectProperty, Collections.emptyList());

				return properties;
			}
		};
		final EnvironmentSettingsModule module = new EnvironmentSettingsModule(new NoOpSettingProvider(), util, objectifyingPostProcessor) {
			@Override
			protected void configure(Binder binder, Map<String, String> properties) {
				assertEquals(1, properties.size());
				assertTrue(properties.containsKey("some.key"));
			}
		};

		System.setProperty("environment.json", "guice-test.json");
		when(binder.bind(any(Key.class))).thenReturn(builder);
		when(binder.skipSources(Names.class)).thenReturn(binder);

		// run test
		module.configure(binder);

		// verify outcome
	}
}
