package com.elmsoftware.env;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class EnvironmentSettingsResolverTest {

	private Util util = new Util();

	@Before
	public void beforeEnvironmentSettingsResolverTest() {
		System.clearProperty("local.only.value");
		System.clearProperty("f523be70-0856-4794-b85c-000de747b109");
	}

	@Test
	public void should_add_environment_values_to_system() {

		// setup test

		// sanity check:
		assertNull(System.getProperty("f523be70-0856-4794-b85c-000de747b109"));

		// run test
		new EnvironmentSettingsResolver("spring-test.json", util);

		// verify outcome
		assertEquals("3ceee183-3bcf-41af-af5b-2e35d65a4c2a", System.getProperty("f523be70-0856-4794-b85c-000de747b109"));
		assertNull(System.getProperty("spring.local.key"));

	}

	@Test
	public void should_add_environment_values_to_system_with_local_over_ride() {

		// setup test

		// sanity check:
		assertNull(System.getProperty("f523be70-0856-4794-b85c-000de747b109"));

		// run test
		new EnvironmentSettingsResolver("spring-test.json", "local-spring-test.json", util, true, new NoOpSettingProvider());

		// verify outcome
		assertEquals("3ceee183-3bcf-41af-af5b-2e35d65a4c2a", System.getProperty("f523be70-0856-4794-b85c-000de747b109"));
		assertEquals("local.only.value", System.getProperty("spring.local.key"));

	}

	@Test
	public void should_add_environment_to_system_properties_without_allowing_existing_values_to_remain() {

		// setup test

		// sanity check:
		assertNull(System.getProperty("f523be70-0856-4794-b85c-000de747b109"));

		// this should be replaced
		System.setProperty("f523be70-0856-4794-b85c-000de747b109", "snapped-the-frame");

		// run test
		new EnvironmentSettingsResolver("spring-test.json", util, false);

		// verify outcome - the value set above should be replaced
		assertEquals("3ceee183-3bcf-41af-af5b-2e35d65a4c2a", System.getProperty("f523be70-0856-4794-b85c-000de747b109"));

	}

}
