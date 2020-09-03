package com.elmsoftware.env.settingproviderimpl;

import com.elmsoftware.env.SettingProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetParameterRequest;
import software.amazon.awssdk.services.ssm.model.GetParameterResponse;
import software.amazon.awssdk.services.ssm.model.Parameter;

public class AwsSsmV2SettingProvider implements SettingProvider {

	private static final Logger log = LoggerFactory.getLogger(AwsSsmV2SettingProvider.class);

	private final SsmClient ssmClient;
	private final String prefix;

	public AwsSsmV2SettingProvider(
			final SsmClient ssmClient,
			final String prefix
	) {
		this.ssmClient = ssmClient;
		this.prefix = prefix;
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
				// this can fail - we want to deal with that gracefully...
				log.info("unable to find parameter {}", name);
				// ...but provide SOME idea what happened for debugging
				log.debug(e.toString(), e);
			}
		}

		throw new RuntimeException(String.format("unable to find parameter using pattern %s", names[0]));

	}

	private GetParameterRequest buildGetParameterRequest(final String name) {
		return GetParameterRequest.builder().name(name).withDecryption(true).build();
	}

	private String buildName(final String... part) {
		return "/" + String.join("/", part).toLowerCase();
	}

}
