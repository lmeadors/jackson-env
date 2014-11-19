package com.elmsoftware.env;

import com.google.inject.Binder;
import com.google.inject.Key;
import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class EnvironmentSettingsModuleTest {

	@Mock
	private Binder binder;

	@Mock
	private LinkedBindingBuilder<String> builder;

	@Captor
	private ArgumentCaptor<Key<String>> argumentCaptor;

	@Before
	public void beforeEnvironmentSettingsModuleTest() {

		System.setProperty("environment.json", "guice-test.json");

		when(binder.bind(any(Key.class))).thenReturn(builder);
		when(binder.skipSources(Names.class)).thenReturn(binder);

	}

	@Test
	public void should_bind_named_values() {

		// setup test
		final EnvironmentSettingsModule module = new EnvironmentSettingsModule();

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

}
