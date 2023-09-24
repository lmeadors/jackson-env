package com.elmsoftware.env.settingproviderimpl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetParametersRequest;
import software.amazon.awssdk.services.ssm.model.GetParametersResponse;
import software.amazon.awssdk.services.ssm.model.Parameter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AwsSsmV2SettingProviderTest {

	@Mock
	private SsmClient ssmClient;

	private AwsSsmV2SettingProvider provider;

	@Captor
	private ArgumentCaptor<GetParametersRequest> requestArgumentCaptor;

	@Before
	public void beforeAwsSsmV2SettingProviderTest() {
		provider = new AwsSsmV2SettingProvider(ssmClient, "prefix");
	}

	@Test
	public void should_get_value_with_full_name() {

		// setup
		final String expected = "expected-value-here";
		final GetParametersResponse response = GetParametersResponse.builder().parameters(
			Parameter.builder()
				.name("/test/prefix/some-key")
				.value(expected)
				.build()
		).build();

		when(ssmClient.getParameters(any(GetParametersRequest.class))).thenReturn(response);

		// run test
		final String actual = provider.getProperty("test", "some-key");

		// verify mocks / capture values
		verify(ssmClient).getParameters(requestArgumentCaptor.capture());
		verifyNoMoreInteractions(ssmClient);

		// assert results
		assertEquals(expected, actual);
		assertTrue(requestArgumentCaptor.getValue().names().contains("/test/prefix/some-key"));

	}

	@Test
	public void should_get_value_for_environment() {

		// setup
		final String expected = "expected-value-here";
		final GetParametersResponse response = GetParametersResponse.builder().parameters(
			Parameter.builder()
				.name("/test/some-key")
				.value(expected)
				.build()
		).build();

		when(ssmClient.getParameters(any(GetParametersRequest.class))).thenReturn(response);

		// run test
		final String actual = provider.getProperty("test", "some-key");

		// verify mocks / capture values
		verify(ssmClient).getParameters(requestArgumentCaptor.capture());
		verifyNoMoreInteractions(ssmClient);

		// assert results
		assertEquals(expected, actual);
		assertTrue(requestArgumentCaptor.getValue().names().contains("/test/some-key"));
	}

	@Test
	public void should_explode_if_no_matching_parameter_found() {

		// setup
		when(ssmClient.getParameters(any(GetParametersRequest.class))).thenReturn(null);

		// run test
		try {
			provider.getProperty("test", "some-key");
		} catch (final Exception e) {
			assertTrue(e.getMessage().startsWith("unable to find parameter"));
		}

		// verify mocks / capture values
		verify(ssmClient).getParameters(requestArgumentCaptor.capture());
		verifyNoMoreInteractions(ssmClient);

		// assert results - nothing here
	}
}
