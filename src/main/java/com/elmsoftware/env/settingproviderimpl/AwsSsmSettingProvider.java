package com.elmsoftware.env.settingproviderimpl;

import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagement;
import com.amazonaws.services.simplesystemsmanagement.model.GetParametersRequest;
import com.amazonaws.services.simplesystemsmanagement.model.GetParametersResult;
import com.amazonaws.services.simplesystemsmanagement.model.Parameter;
import com.elmsoftware.env.SettingProvider;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.Optional;
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

		final Parameter parameter = getParameter(
				buildName(environment, prefix, key),
				buildName(environment, key),
				buildName("global", key)
		);

		return parameter.getValue();

	}

	private Parameter getParameter(final String... names) {
		try {
			final GetParametersResult result = awsSsm.getParameters(buildGetParametersRequest(names));
			for (final String name : names) {
				log.debug("looking for parameter {}", name);
				final Optional<Parameter> parameter = result.getParameters().stream()
					.filter(it -> name.equals(it.getName()))
					.findFirst();
				if (parameter.isPresent()) {
					log.info("found parameter as {}", name);
					return parameter.get();
				}
			}
		} catch (final Exception e) {
			exceptionHandler.accept(new ProviderExceptionHandler.ExceptionInfo(Arrays.toString(names), e));
		}

		throw new RuntimeException(String.format("unable to find parameter using pattern %s", names[0]));

	}

	private GetParametersRequest buildGetParametersRequest(final String... names) {
		return new GetParametersRequest()
			.withNames(names)
			.withWithDecryption(true);
	}

	private String buildName(final String... part) {
		return "/" + String.join("/", part).toLowerCase();
	}

}
