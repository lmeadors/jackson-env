package com.elmsoftware.env;

import org.junit.Test;

import java.io.Closeable;
import java.io.IOException;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class UtilTest {

	private final Util util = new Util();

	@Test
	public void should_detect_blank_strings() {
		// setup test
		// run test
		assertTrue(util.isBlank(null));
		assertTrue(util.isBlank(""));
		assertTrue(util.isBlank("  "));
		assertFalse(util.isBlank("  not blank"));
		// verify outcome
	}

	@Test
	public void should_not_explode_closing_null() {
		// setup test
		// run test
		util.closeQuietly(null);
		// verify outcome
	}

	@Test
	public void should_not_explode_when_close_explodes() throws IOException {

		// setup test
		final Closeable closeable = mock(Closeable.class);
		doThrow(new IOException("test")).when(closeable).close();

		// run test
		util.closeQuietly(closeable);

		// verify outcome
		verify(closeable).close();
		verifyNoMoreInteractions(closeable);

	}

	@Test
	public void should_nicely_close_closeable_object() throws IOException {

		// setup test
		final Closeable closeable = mock(Closeable.class);

		// run test
		util.closeQuietly(closeable);

		// verify outcome
		verify(closeable).close();
		verifyNoMoreInteractions(closeable);

	}

	@Test
	public void should_get_system_property() {

		// setup test
		final String targetName = UUID.randomUUID().toString();
		final String targetValue = UUID.randomUUID().toString();

		System.setProperty(EnvironmentSettings.ENV_VAR, targetName);
		System.setProperty(targetName, targetValue);

		// run test
		final String actual = util.determineEnvironment(EnvironmentSettings.ENV_VAR);

		// verify outcome
		assertEquals(targetValue, actual);
		System.clearProperty(EnvironmentSettings.ENV_VAR);

	}

}
