package com.elmsoftware.env;

import org.junit.Test;

import java.io.Closeable;
import java.io.IOException;

import static com.elmsoftware.env.Util.closeQuietly;
import static com.elmsoftware.env.Util.isBlank;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class UtilTest {

	@Test
	public void coverage_only() {
		// setup test
		// run test
		new Util();
		// verify outcome
	}

	@Test
	public void should_detect_blank_strings() {
		// setup test
		// run test
		assertTrue(isBlank(null));
		assertTrue(isBlank(""));
		assertTrue(isBlank("  "));
		assertFalse(isBlank("  not blank"));
		// verify outcome
	}

	@Test
	public void should_not_explode_closing_null() {
		// setup test
		// run test
		closeQuietly(null);
		// verify outcome
	}

	@Test
	public void should_not_explode_when_close_explodes() throws IOException {

		// setup test
		final Closeable closeable = mock(Closeable.class);
		doThrow(new IOException("test")).when(closeable).close();

		// run test
		closeQuietly(closeable);

		// verify outcome
		verify(closeable).close();
		verifyNoMoreInteractions(closeable);

	}

	@Test
	public void should_nicely_close_closeable_object() throws IOException {

		// setup test
		final Closeable closeable = mock(Closeable.class);

		// run test
		closeQuietly(closeable);

		// verify outcome
		verify(closeable).close();
		verifyNoMoreInteractions(closeable);

	}

}
