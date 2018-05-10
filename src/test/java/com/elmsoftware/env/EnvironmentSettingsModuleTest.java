package com.elmsoftware.env;

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
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

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
	public void should_bind_named_values_with_optional_overrides() {

		// setup test
		System.setProperty("environment.json", "guice-test.json");
		System.setProperty("local.environment.json", "local-guice-test.json");

		when(binder.bind(any(Key.class))).thenReturn(builder);
		when(binder.skipSources(Names.class)).thenReturn(binder);

		final EnvironmentSettingsModule module = new EnvironmentSettingsModule(new NoOpSettingProvider(), util);

		// run test
		module.configure(binder);

		// verify outcome
		verify(binder).skipSources(Names.class);
		verify(binder, times(2)).bind(argumentCaptor.capture());
		verifyNoMoreInteractions(binder);

		final List<Key<String>> allValues = argumentCaptor.getAllValues();

		assertNotNull(findKey(allValues, "some.key"));
		assertNotNull(findKey(allValues, "some.local.key"));

	}

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
		verify(binder).bind(argumentCaptor.capture());
		verifyNoMoreInteractions(binder);

		final Key<String> value = argumentCaptor.getValue();
		assertTrue(value.hasAttributes());
		final Named annotation = (Named) value.getAnnotation();
		assertEquals("some.key", annotation.value());
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

	private Key<String> findKey(final List<Key<String>> allValues, final String name) {
		for (final Key<String> key : allValues) {
			final Named annotation = (Named) key.getAnnotation();
			if (annotation.value().equals(name)) {
				return key;
			}
		}
		return null;
	}

}
