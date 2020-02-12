package com.elmsoftware.env.settingproviderimpl;

import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagement;
import com.amazonaws.services.simplesystemsmanagement.model.GetParameterRequest;
import com.amazonaws.services.simplesystemsmanagement.model.GetParameterResult;
import com.amazonaws.services.simplesystemsmanagement.model.Parameter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AwsSsmSettingProviderTest {

	@Mock
	private AWSSimpleSystemsManagement ssm;

	private AwsSsmSettingProvider provider;

	@Captor
	private ArgumentCaptor<GetParameterRequest> getParameterRequestArgumentCaptor;

	@Before
	public void beforeAwsSsmSettingProviderTest() {
		provider = new AwsSsmSettingProvider(ssm, "prefix");
	}

	@After
	public void after() {
		verifyNoMoreInteractions(ssm);
	}

	@Test
	public void should_get_value_from_ssm_with_default_prefix() {

		// setup
		provider = new AwsSsmSettingProvider(ssm, "prefix");

		final String expected = "snapped-the-frame";
		final GetParameterResult parameterResult = new GetParameterResult()
			.withParameter(new Parameter().withValue(expected));

		when(ssm.getParameter(any())).thenReturn(parameterResult);

		// run test
		final String actual = provider.getProperty("foo", "my-key");

		// verify mocks / capture values
		verify(ssm).getParameter(getParameterRequestArgumentCaptor.capture());

		// assert results

		// did we get the expected value?
		assertEquals(expected, actual);

		// was the request what we expected?
		final List<String> nameList = getParameterRequestArgumentCaptor.getAllValues()
			.stream()
			.map(GetParameterRequest::getName)
			.collect(Collectors.toList());
		assertTrue(nameList.contains("/foo/prefix/my-key"));

	}

	@Test
	public void should_blow_if_setting_can_not_be_found() {

		// setup
		final String[] expectedValues = {
			"/fml/prefix/snapped-the-frame",
			"/fml/snapped-the-frame",
			"/global/snapped-the-frame"
		};

		when(ssm.getParameter(any())).thenThrow(new RuntimeException("no parameter defined"));

		// run test
		try {
			provider.getProperty("fml", "snapped-the-frame");
			fail("no parameter was found - this should have failed.");
		} catch (final Exception e) {
			// this is expected
			assertEquals(
				"unable to find parameter using pattern /fml/prefix/snapped-the-frame",
				e.getLocalizedMessage()
			);
		}

		// verify mocks / capture values
		verify(ssm, times(3)).getParameter(getParameterRequestArgumentCaptor.capture());

		// assert results
		final List<String> nameList = getParameterRequestArgumentCaptor.getAllValues()
			.stream()
			.map(GetParameterRequest::getName)
			.collect(Collectors.toList());

		for (final String value : expectedValues) {
			assertTrue("name list should include '" + value + "'", nameList.contains(value));
		}

	}

}
