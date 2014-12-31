package com.elmsoftware.env;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.name.Names;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class EnvironmentSettingsModule implements Module {

	private static final Logger log = LoggerFactory.getLogger(EnvironmentSettingsModule.class);

	@Override
	public void configure(Binder binder) {

		// Load up all named properties
		final String environment = System.getProperty("environment", "LOCAL");
		final String resourceName = System.getProperty("environment.json", "environment.json");
		log.debug("Loading environment {} using resource {}", environment, resourceName);

		final EnvironmentSettings settings = EnvironmentSettings.load(resourceName);
		final Map<String, String> properties = settings.merge(environment);
		Names.bindProperties(binder, properties);
		configure(binder, properties);

	}

	protected void configure(final Binder binder, final Map<String, String> properties){

	}

}
