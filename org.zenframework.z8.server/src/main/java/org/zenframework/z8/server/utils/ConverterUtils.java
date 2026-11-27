package org.zenframework.z8.server.utils;

import org.zenframework.z8.server.config.ServerConfig;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public final class ConverterUtils {
	private ConverterUtils() {}

	public static void check() {
		try {
			HttpURLConnection c = (HttpURLConnection) new URL(ServerConfig.converterUrl() + "/api/v1/status").openConnection();
			if (c.getResponseCode() != HttpURLConnection.HTTP_OK)
				throw new RuntimeException("z8-x2t: missing");
			c.disconnect();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static InputStream convert(InputStream input, String from, String to) {
		try {
			String url = String.format("%s/api/v1/convert?from=%s&to=%s", ServerConfig.converterUrl(), from, to);
			HttpURLConnection c = (HttpURLConnection) new URL(url).openConnection();
			c.setRequestMethod("POST");
			c.setDoInput(true);
			c.setDoOutput(true);

			OutputStream o = null;
			try {
				o = c.getOutputStream();
				IOUtils.copy(input, o);
			} finally {
				IOUtils.closeQuietly(o);
			}

			if (c.getResponseCode() != HttpURLConnection.HTTP_OK)
				throw new RuntimeException("Unexpected response code: " + c.getResponseCode());

			return c.getInputStream();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
