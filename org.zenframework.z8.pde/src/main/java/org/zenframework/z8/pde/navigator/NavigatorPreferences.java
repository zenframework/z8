package org.zenframework.z8.pde.navigator;

import org.zenframework.z8.pde.Plugin;

public class NavigatorPreferences {
	public static final String FILTER_BASE_TYPE_MEMBERS = "Navigator.FilterBaseTypeMembers";

	public static boolean getFilterBaseTypeMembers() {
		return Plugin.getPreferenceBoolean(FILTER_BASE_TYPE_MEMBERS);
	}

	public static void setFilterBaseTypeMembers(boolean value) {
		Plugin.setPreference(FILTER_BASE_TYPE_MEMBERS, value);
	}

	private NavigatorPreferences() {
	}
}
