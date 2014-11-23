package com.elmsoftware.env;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;

public class Util {

	private static final Logger log = LoggerFactory.getLogger(Util.class);

	public static boolean isBlank(final String string) {
		return null == string || string.trim().isEmpty();
	}

	public static void closeQuietly(final Closeable closeable) {
		if (null != closeable) {
			try {
				closeable.close();
			} catch (final IOException e) {
				log.warn("Unable to close: {}", e);
			}
		}
	}

}
