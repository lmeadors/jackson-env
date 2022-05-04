package com.elmsoftware.env.settingproviderimpl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetParameterRequest;
import software.amazon.awssdk.services.ssm.model.GetParameterResponse;
import software.amazon.awssdk.services.ssm.model.Parameter;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AwsSsmV2SettingProviderTest {

	@Mock
	private SsmClient ssmClient;

	private AwsSsmV2SettingProvider provider;

	@Captor
	private ArgumentCaptor<GetParameterRequest> requestArgumentCaptor;

	@Before
	public void beforeAwsSsmV2SettingProviderTest() {
		provider = new AwsSsmV2SettingProvider(ssmClient, "prefix");
	}

	@Test
	public void should_get_value_with_full_name() {

		// setup
		final String expected = "expected-value-here";
		final GetParameterResponse response = GetParameterResponse.builder().parameter(
			Parameter.builder().value(expected).build()
		).build();

		when(ssmClient.getParameter(any(GetParameterRequest.class))).thenReturn(response);

		// run test
		final String actual = provider.getProperty("test", "some-key");

		// verify mocks / capture values
		verify(ssmClient).getParameter(requestArgumentCaptor.capture());
		verifyNoMoreInteractions(ssmClient);

		// assert results
		assertEquals(expected, actual);
		assertEquals("/test/prefix/some-key", requestArgumentCaptor.getValue().name());

	}

	@Test
	public void should_get_value_for_environment() {

		// setup
		final String expected = "expected-value-here";
		final GetParameterResponse response = GetParameterResponse.builder().parameter(
			Parameter.builder().value(expected).build()
		).build();

		when(ssmClient.getParameter(any(GetParameterRequest.class))).thenReturn(null, response);

		// run test
		final String actual = provider.getProperty("test", "some-key");

		// verify mocks / capture values
		verify(ssmClient, times(2)).getParameter(requestArgumentCaptor.capture());
		verifyNoMoreInteractions(ssmClient);

		// assert results
		assertEquals(expected, actual);
		assertEquals("/test/some-key", requestArgumentCaptor.getValue().name());

	}

	@Test
	public void should_explode_if_no_matching_parameter_found() {

		// setup
		final String expected = "expected-value-here";

		when(ssmClient.getParameter(any(GetParameterRequest.class))).thenReturn(null);

		// run test
		try {
			provider.getProperty("test", "some-key");
		} catch (final Exception e) {
			assertEquals("unable to find parameter using pattern /test/prefix/some-key", e.getMessage());
		}

		// verify mocks / capture values
		verify(ssmClient, times(3)).getParameter(requestArgumentCaptor.capture());
		verifyNoMoreInteractions(ssmClient);

		// assert results - nothing here

	}

}
