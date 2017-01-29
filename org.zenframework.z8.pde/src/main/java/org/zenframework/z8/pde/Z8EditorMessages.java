package org.zenframework.z8.pde;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class Z8EditorMessages {
	private static final ResourceBundle bundle = new Z8ResourceBundle();

	private Z8EditorMessages() {
	}

	public static String getString(String key) {
		try {
			return getResourceBundle().getString(key);
		} catch(MissingResourceException e) {
			return "!" + key + "!";
		}
	}

	public static ResourceBundle getResourceBundle() {
		return bundle;
	}
}
