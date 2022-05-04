package com.elmsoftware.env.settingproviderimpl;

import com.elmsoftware.env.SettingProvider;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetParameterRequest;
import software.amazon.awssdk.services.ssm.model.GetParameterResponse;
import software.amazon.awssdk.services.ssm.model.Parameter;

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
		for (final String name : names) {
			try {
				log.debug("looking for parameter {}", name);
				final GetParameterResponse response = ssmClient.getParameter(buildGetParameterRequest(name));
				log.info("found parameter as {}", name);
				return response.parameter();
			} catch (final Exception e) {
				exceptionHandler.accept(new ProviderExceptionHandler.ExceptionInfo(name, e));
			}
		}

		throw new RuntimeException(String.format("unable to find parameter using pattern %s", names[0]));

	}

	private GetParameterRequest buildGetParameterRequest(final String name) {
		return GetParameterRequest.builder()
			.name(name)
			.withDecryption(true).build();
	}

	private String buildName(final String... part) {
		return "/" + String.join("/", part).toLowerCase();
	}

}
