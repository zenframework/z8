package org.zenframework.z8.pde.navigator;

import org.eclipse.osgi.util.NLS;

public final class NavigatorMessages extends NLS {
	private static final String BUNDLE_NAME = "org.zenframework.z8.pde.navigator.navigator";

	private NavigatorMessages() {
	}

	public static String ShowBaseTypeMembers;
	public static String ShowBaseTypeMembers_tooltip;

	static {
		NLS.initializeMessages(BUNDLE_NAME, NavigatorMessages.class);
	}
}