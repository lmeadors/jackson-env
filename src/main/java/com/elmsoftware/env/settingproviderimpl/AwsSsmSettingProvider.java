package com.elmsoftware.env.settingproviderimpl;

import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagement;
import com.amazonaws.services.simplesystemsmanagement.model.GetParameterRequest;
import com.amazonaws.services.simplesystemsmanagement.model.GetParameterResult;
import com.elmsoftware.env.SettingProvider;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Consumer;

@Slf4j
public class AwsSsmSettingProvider implements SettingProvider {

	private final AWSSimpleSystemsManagement awsSsm;
	private final String prefix;
	private final Consumer<ProviderExceptionHandler.ExceptionInfo> exceptionHandler;

	public AwsSsmSettingProvider(
			final AWSSimpleSystemsManagement awsSsm,
			final String prefix
	) {
		this.awsSsm = awsSsm;
		this.prefix = prefix;
		this.exceptionHandler = new ProviderExceptionHandler();
	}

	public AwsSsmSettingProvider(
			final AWSSimpleSystemsManagement awsSsm,
			final String prefix,
			final Consumer<ProviderExceptionHandler.ExceptionInfo> exceptionHandler
	) {
		this.awsSsm = awsSsm;
		this.prefix = prefix;
		this.exceptionHandler = exceptionHandler;
	}

	@Override
	public String getProperty(final String environment, final String key) {

		log.info("looking for property {} in environment {}", key, environment);

		final GetParameterResult parameter = getParameter(
				buildName(environment, prefix, key),
				buildName(environment, key),
				buildName("global", key)
		);

		return parameter.getParameter().getValue();

	}

	private GetParameterResult getParameter(final String... names) {
		for (final String name : names) {
			try {
				log.debug("looking for parameter {}", name);
				final GetParameterResult parameter = awsSsm.getParameter(buildGetParameterRequest(name));
				log.info("found parameter as {}", name);
				return parameter;
			} catch (final Exception e) {
				exceptionHandler.accept(new ProviderExceptionHandler.ExceptionInfo(name, e));
			}
		}

		throw new RuntimeException(String.format("unable to find parameter using pattern %s", names[0]));

	}

	private GetParameterRequest buildGetParameterRequest(final String name) {
		return new GetParameterRequest()
			.withName(name)
			.withWithDecryption(true);
	}

	private String buildName(final String... part) {
		return "/" + String.join("/", part).toLowerCase();
	}

}
