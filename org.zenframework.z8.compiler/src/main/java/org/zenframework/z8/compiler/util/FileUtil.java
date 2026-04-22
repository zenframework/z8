package org.zenframework.z8.compiler.util;

import java.io.File;

public class FileUtil {

	private static final String USER_HOME_PROPERTY = "user.home";
	private static final String[] USER_HOME_ENV_VARS = { "USERPROFILE", "HOMEDRIVE", "HOMEPATH" };

	public static File getUserHomeDirectory() {
		return new File(getUserHomePath()).getAbsoluteFile();
	}

	public static String getUserHomePath() {
		String home = System.getProperty(USER_HOME_PROPERTY);
		if (home != null && !home.isEmpty())
			return home;
		for (String env : USER_HOME_ENV_VARS)
			if ((home = System.getenv(env)) != null && !home.isEmpty())
					return home;
		return null;
	}

}
