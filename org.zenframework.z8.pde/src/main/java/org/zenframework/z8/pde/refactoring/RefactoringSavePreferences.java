package org.zenframework.z8.pde.refactoring;

import org.eclipse.jface.preference.IPreferenceStore;

import org.zenframework.z8.pde.PreferenceConstants;
import org.zenframework.z8.pde.Plugin;

public class RefactoringSavePreferences {
    public static final String PREF_SAVE_ALL_EDITORS = PreferenceConstants.REFACTOR_SAVE_ALL_EDITORS;

    public static boolean getSaveAllEditors() {
        IPreferenceStore store = Plugin.getDefault().getPreferenceStore();
        return store.getBoolean(PREF_SAVE_ALL_EDITORS);
    }

    public static void setSaveAllEditors(boolean save) {
        IPreferenceStore store = Plugin.getDefault().getPreferenceStore();
        store.setValue(RefactoringSavePreferences.PREF_SAVE_ALL_EDITORS, save);
    }
}
