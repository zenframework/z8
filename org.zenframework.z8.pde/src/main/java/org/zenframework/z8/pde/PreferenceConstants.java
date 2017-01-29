package org.zenframework.z8.pde;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPreferenceStore;

public class PreferenceConstants {
	public static final String REFACTOR_SAVE_ALL_EDITORS = "Refactoring.savealleditors"; //$NON-NLS-1$

	private PreferenceConstants() {
	}

	public static void initializeDefaultValues(IPreferenceStore store) {
		store.setDefault(PreferenceConstants.REFACTOR_SAVE_ALL_EDITORS, false);
	}

	public static IPreferenceStore getPreferenceStore() {
		return Plugin.getDefault().getPreferenceStore();
	}

	@SuppressWarnings("deprecation")
	public static String getPreference(String key, IProject project) {
		String val;

		if(project != null) {
			val = new ProjectScope(project.getProject()).getNode(Plugin.PLUGIN_ID).get(key, null);

			if(val != null) {
				return val;
			}
		}

		val = new InstanceScope().getNode(Plugin.PLUGIN_ID).get(key, null);

		if(val != null) {
			return val;
		}

		return new DefaultScope().getNode(Plugin.PLUGIN_ID).get(key, null);
	}
}
