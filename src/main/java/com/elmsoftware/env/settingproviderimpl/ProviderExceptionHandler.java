package com.elmsoftware.env.settingproviderimpl;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Consumer;

@Slf4j
public class ProviderExceptionHandler implements Consumer<ProviderExceptionHandler.ExceptionInfo> {

	@Override
	public void accept(final ExceptionInfo info) {
		// this can fail - we want to deal with that gracefully...
		log.info("unable to find parameter(s) {}", info.name);
		// ...but provide SOME idea what happened for debugging
		log.debug(info.toString(), info);
	}

	@Data
	public static class ExceptionInfo {
		private final String name;
		private final Exception exception;
	}

}
