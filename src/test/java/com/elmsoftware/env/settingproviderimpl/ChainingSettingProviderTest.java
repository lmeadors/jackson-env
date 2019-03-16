package com.elmsoftware.env.settingproviderimpl;

import com.elmsoftware.env.SettingProvider;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class ChainingSettingProviderTest {

	@Mock
	private SettingProvider provider1;

	@Mock
	private SettingProvider provider2;

	private ChainingSettingProvider provider;

	@Before
	public void beforeChainingSettingProviderTest() {
		initMocks(this);
		provider = new ChainingSettingProvider(provider1, provider2);
	}

	@Test
	public void should_stop_if_first_provider_has_value() {
		// setup
		when(provider1.getProperty("test", "key")).thenReturn("value");

		// run test and assert results
		assertEquals("value", provider.getProperty("test", "key"));

		// verify mocks / capture values
		verify(provider1).getProperty("test", "key");
		verifyNoMoreInteractions(provider1, provider2);

	}

	@Test
	public void should_stop_if_second_provider_has_value() {

		// setup
		when(provider2.getProperty("test", "key")).thenReturn("value2");

		// run test and assert results
		assertEquals("value2", provider.getProperty("test", "key"));

		// verify mocks / capture values
		verify(provider1).getProperty("test", "key");
		verify(provider2).getProperty("test", "key");
		verifyNoMoreInteractions(provider1, provider2);

	}

	@Test
	public void should_return_null_if_no_provider_has_value() {

		// setup

		// run test and assert results
		assertNull(provider.getProperty("test", "key"));

		// verify mocks / capture values
		verify(provider1).getProperty("test", "key");
		verify(provider2).getProperty("test", "key");
		verifyNoMoreInteractions(provider1, provider2);

	}

}
