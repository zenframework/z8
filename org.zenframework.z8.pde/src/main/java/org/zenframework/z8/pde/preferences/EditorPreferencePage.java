package org.zenframework.z8.pde.preferences;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import org.zenframework.z8.pde.Plugin;

public class EditorPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	private Button btn_alwaysSave;

	@Override
	protected Control createContents(Composite parent) {
		// GridData gd = null;
		// Object grd = null;

		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout(1, false);
		composite.setLayout(layout);

		btn_alwaysSave = new Button(composite, SWT.CHECK);
		btn_alwaysSave.setText("&Save editor on all actions");

		initializeValues();
		return composite;
	}

	@Override
	public void init(IWorkbench workbench) {
	}

	@Override
	public boolean performOk() {
		storeValues();
		return super.performOk();
	}

	@Override
	protected void performDefaults() {
		initializeDefaults();
		super.performDefaults();
	}

	private void initializeDefaults() {
		btn_alwaysSave.setSelection(false);
	}

	private void initializeValues() {
		btn_alwaysSave.setSelection(doGetPreferenceStore().getBoolean(PreferencePageConsts.ATTR_EDITOR_ALWAYS_SAVE));
		validatePage();
	}

	private void validatePage() {
		setErrorMessage(null);
		setValid(true);
	}

	private void storeValues() {
		doGetPreferenceStore().setValue(PreferencePageConsts.ATTR_EDITOR_ALWAYS_SAVE, btn_alwaysSave.getSelection());
	}

	@Override
	protected IPreferenceStore doGetPreferenceStore() {
		return Plugin.getDefault().getPreferenceStore();
	}

}
