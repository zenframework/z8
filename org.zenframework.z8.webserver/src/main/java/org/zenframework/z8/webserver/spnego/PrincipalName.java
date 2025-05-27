package org.zenframework.z8.webserver.spnego;

import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;

public class PrincipalName {

	private static final String[] JaasConfigurations = { "com.sun.security.jgss.initiate", "com.sun.security.jgss.accept" };
	private static final String Principal = "principal";

	private final String targetName;
	private final String domainName;

	public PrincipalName(String name) {
		String[] parts = name.split("@");

		if (parts.length != 2)
			throw new RuntimeException("Incorrect principal name: " + name);

		targetName = parts[0];
		domainName = parts[1];
	}

	public String getTargetName() {
		return targetName;
	}

	public String getDomainName() {
		return domainName;
	}

	@Override
	public String toString() {
		return targetName + '@' + domainName;
	}

	public static PrincipalName getJaasPrincipal() {
		Configuration config = Configuration.getConfiguration();
		for (String configName : JaasConfigurations) {
			AppConfigurationEntry[] entries = config.getAppConfigurationEntry(configName);
			if (entries != null) {
				for (AppConfigurationEntry entry : entries) {
					Object principal = entry.getOptions().get(Principal);
					if (principal != null)
						return new PrincipalName(principal.toString());
				}
			}
		}
		return null;
	}
}
