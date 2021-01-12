package org.zenframework.z8.web.utils;

import org.zenframework.z8.server.config.ServerConfig;
import org.zenframework.z8.server.exceptions.AccessDeniedException;

import javax.servlet.http.HttpServletRequest;

public class ServletUtil {

	public static String getSchema(HttpServletRequest request) {
		if(!ServerConfig.isMultitenant())
			return null;

		String serverName = request.getServerName();
		int index = serverName.indexOf('.');
		if(index == -1 || index == serverName.lastIndexOf('.') && !serverName.endsWith("localhost"))
			throw new AccessDeniedException();

		return serverName.substring(0, index);
	}
}
