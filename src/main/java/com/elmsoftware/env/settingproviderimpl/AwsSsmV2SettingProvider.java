package com.elmsoftware.env.settingproviderimpl;

import com.elmsoftware.env.SettingProvider;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetParametersRequest;
import software.amazon.awssdk.services.ssm.model.GetParametersResponse;
import software.amazon.awssdk.services.ssm.model.Parameter;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Consumer;

@Slf4j
public class AwsSsmV2SettingProvider implements SettingProvider {

	private final SsmClient ssmClient;
	private final String prefix;
	private final Consumer<ProviderExceptionHandler.ExceptionInfo> exceptionHandler;

	public AwsSsmV2SettingProvider(
			final SsmClient ssmClient,
			final String prefix
	) {
		this.ssmClient = ssmClient;
		this.prefix = prefix;
		this.exceptionHandler = new ProviderExceptionHandler();
	}

	public AwsSsmV2SettingProvider(
			final SsmClient ssmClient,
			final String prefix,
			final ProviderExceptionHandler exceptionHandler
	) {
		this.ssmClient = ssmClient;
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

		return parameter.value();

	}

	private Parameter getParameter(final String... names) {
		try {
			final GetParametersResponse response = ssmClient.getParameters(buildGetParametersRequest(names));
			for (final String name : names) {
				log.debug("looking for parameter {}", name);
				final Optional<Parameter> parameter = response.parameters().stream()
					.filter(it -> name.equals(it.name()))
					.findFirst();
				if (parameter.isPresent()) {
					log.info("found parameter as {}", name);
					return parameter.get();
				}
			}
		} catch (final Exception e) {
			exceptionHandler.accept(new ProviderExceptionHandler.ExceptionInfo(Arrays.toString(names), e));
		}

		throw new RuntimeException(String.format("unable to find parameter for property group %s", Arrays.toString(names)));
	}
	private GetParametersRequest buildGetParametersRequest(final String... names) {
		return GetParametersRequest.builder()
			.names(names)
			.withDecryption(true)
			.build();
	}

	private String buildName(final String... part) {
		return "/" + String.join("/", part).toLowerCase();
	}

}
