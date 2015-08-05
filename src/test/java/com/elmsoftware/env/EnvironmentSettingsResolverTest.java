package com.elmsoftware.env;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class EnvironmentSettingsResolverTest {

	@Test
	public void should_add_environment_values_to_system() {

		// setup test

		// sanity check:
		assertNull(System.getProperty("f523be70-0856-4794-b85c-000de747b109"));

		// run test
		new EnvironmentSettingsResolver("spring-test.json");

		// verify outcome
		assertEquals("3ceee183-3bcf-41af-af5b-2e35d65a4c2a", System.getProperty("f523be70-0856-4794-b85c-000de747b109"));
		assertNull(System.getProperty("spring.local.key"));

		System.clearProperty("local.only.value");
		System.clearProperty("f523be70-0856-4794-b85c-000de747b109");

	}

	@Test
	public void should_add_environment_values_to_system_with_local_over_ride() {

		// setup test

		// sanity check:
		assertNull(System.getProperty("f523be70-0856-4794-b85c-000de747b109"));

		// run test
		new EnvironmentSettingsResolver("spring-test.json", "local-spring-test.json");

		// verify outcome
		assertEquals("3ceee183-3bcf-41af-af5b-2e35d65a4c2a", System.getProperty("f523be70-0856-4794-b85c-000de747b109"));
		assertEquals("local.only.value", System.getProperty("spring.local.key"));

		System.clearProperty("local.only.value");
		System.clearProperty("f523be70-0856-4794-b85c-000de747b109");

	}

}
