package org.zenframework.z8.pde.preferences;

import java.io.File;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.zenframework.z8.pde.Plugin;

public class PreferencePage extends org.eclipse.jface.preference.PreferencePage implements IWorkbenchPreferencePage {
	private Label lbl_tomcatHome;
	private Label lbl_tomcatMemoryBound;
	private Label lbl_webinfPath;

	private Text txt_tomcatHome;
	private Text txt_tomcatMemoryBound;
	private Text txt_webinfPath;
	private Button btn_tomcatButton;
	private Button btn_webinfButton;

	@Override
	protected Control createContents(Composite parent) {
		GridData gd = null;
		Object grd = null;

		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout(3, false);

		// ATTR_TOMCAT_HOME
		composite.setLayout(layout);
		lbl_tomcatHome = new Label(composite, SWT.NONE);
		lbl_tomcatHome.setText("&Tomcat home directory:");
		gd = new GridData(GridData.BEGINNING);
		lbl_tomcatHome.setLayoutData(gd);
		txt_tomcatHome = new Text(composite, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		txt_tomcatHome.setLayoutData(gd);
		txt_tomcatHome.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				validatePage();
			}
		});
		btn_tomcatButton = new Button(composite, SWT.BUTTON1);
		btn_tomcatButton.setText("&Browse...");
		// SWTUtil.createPushButton(composite, "&Browse...", null);
		// //$NON-NLS-1$
		grd = btn_tomcatButton.getLayoutData();
		if(grd instanceof GridData) {
			((GridData)grd).widthHint = btn_tomcatButton.getSize().x;
			// SWTUtil.getButtonWidthHint(btn_tomcatButton);
			((GridData)grd).horizontalAlignment = GridData.END;
		}
		btn_tomcatButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String browsePath = browseDirectory(txt_tomcatHome.getText());
				if(browsePath != null)
					txt_tomcatHome.setText(browsePath);
			}
		});

		// ATTR_TOMCAT_MEMORY_BOUND
		composite.setLayout(layout);
		gd = new GridData(GridData.BEGINNING);
		lbl_tomcatMemoryBound = new Label(composite, SWT.NONE);
		lbl_tomcatMemoryBound.setText("Tomcat &memory bound, MB:");
		lbl_tomcatMemoryBound.setLayoutData(gd);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		txt_tomcatMemoryBound = new Text(composite, SWT.SINGLE | SWT.BORDER);
		txt_tomcatMemoryBound.setLayoutData(gd);
		txt_tomcatMemoryBound.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				validatePage();
			}
		});
		txt_tomcatMemoryBound.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent e) {
				if(Integer.valueOf(txt_tomcatMemoryBound.getText()) <= 0)
					txt_tomcatMemoryBound.setText("");
			}

			@Override
			public void focusLost(FocusEvent e) {
				if(txt_tomcatMemoryBound.getText() == "" || Integer.valueOf(txt_tomcatMemoryBound.getText()) <= 0)
					txt_tomcatMemoryBound.setText("0");
			}
		});
		grd = txt_tomcatMemoryBound.getLayoutData();
		if(grd instanceof GridData) {
			((GridData)grd).horizontalSpan = 2;
		}

		// ATTR_WEB_INF_PATH
		composite.setLayout(layout);
		lbl_webinfPath = new Label(composite, SWT.NONE);
		lbl_webinfPath.setText("&WEB-INF path:");
		gd = new GridData(GridData.BEGINNING);
		lbl_webinfPath.setLayoutData(gd);
		txt_webinfPath = new Text(composite, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		txt_webinfPath.setLayoutData(gd);
		btn_webinfButton = new Button(composite, SWT.BUTTON1);
		btn_webinfButton.setText("&Browse...");
		// SWTUtil.createPushButton(composite, "&Browse...", null);
		// //$NON-NLS-1$
		grd = btn_webinfButton.getLayoutData();
		txt_webinfPath.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				validatePage();
			}
		});

		if(grd instanceof GridData) {
			((GridData)grd).widthHint = btn_webinfButton.getSize().x;
			// SWTUtil.getButtonWidthHint(btn_tomcatButton);
			((GridData)grd).horizontalAlignment = GridData.END;
		}
		btn_webinfButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String browsePath = browseDirectory(txt_webinfPath.getText());
				if(browsePath != null)
					txt_webinfPath.setText(browsePath);
			}
		});

		initializeValues();
		return composite;
	}

	protected String browseFile(String path) {
		FileDialog dialog = new FileDialog(getShell());
		// dialog.setText("Choose a file");
		dialog.setFilterExtensions(new String[] { "*.exe" });
		dialog.setFilterPath(path);
		return dialog.open();
	}

	protected String browseDirectory(String path) {
		DirectoryDialog dialog = new DirectoryDialog(getShell());
		dialog.setFilterPath(path);
		// dialog.setText("Choose a directory");
		return dialog.open();
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
		txt_tomcatHome.setText("");
		txt_tomcatMemoryBound.setText("256");
		txt_webinfPath.setText("../../../WebApp/WEB-INF");
	}

	private void initializeValues() {
		txt_tomcatHome.setText(doGetPreferenceStore().getString(PreferencePageConsts.ATTR_TOMCAT_HOME));
		txt_tomcatMemoryBound.setText(new Integer(doGetPreferenceStore().getInt(PreferencePageConsts.ATTR_TOMCAT_MEMORY_BOUND)).toString());
		txt_webinfPath.setText(doGetPreferenceStore().getString(PreferencePageConsts.ATTR_WEB_INF_PATH));
		validatePage();
	}

	private void validatePage() {
		String tomcatMemoryBound = txt_tomcatMemoryBound.getText();
		if(!tomcatMemoryBound.matches("[0-9]*")) {
			setErrorMessage("������� ����� � ���� Tomcat Memory Bound");
			setValid(false);
			return;
		}

		String tomcatHome = txt_tomcatHome.getText();
		if(tomcatHome.length() > 0) {
			if(!new File(tomcatHome).exists()) {
				setErrorMessage("������ �� ������������ ���� � Tomcat Home");
				setValid(false);
				return;
			}
		}
		String webInfPath = txt_webinfPath.getText();
		if(webInfPath.length() > 0) {
			if(!new File(webInfPath).exists()) {
				setErrorMessage("������ �� ������������ ���� � WebInf");
				setValid(false);
				return;
			}
		}
		setErrorMessage(null);
		setValid(true);
	}

	private void storeValues() {
		doGetPreferenceStore().setValue(PreferencePageConsts.ATTR_TOMCAT_HOME, txt_tomcatHome.getText());
		doGetPreferenceStore().setValue(PreferencePageConsts.ATTR_TOMCAT_MEMORY_BOUND, new Integer(txt_tomcatMemoryBound.getText()).intValue());

		String webInfPath = txt_webinfPath.getText();

		doGetPreferenceStore().setValue(PreferencePageConsts.ATTR_WEB_INF_PATH, webInfPath);
	}

	@Override
	protected IPreferenceStore doGetPreferenceStore() {
		return Plugin.getDefault().getPreferenceStore();
	}

	public Integer getAttribute(String attr, Integer defaultValue) {
		return new Integer(doGetPreferenceStore().getInt(attr));
	}

	public String getAttribute(String attr, String defaultValue) {
		return doGetPreferenceStore().getString(attr);
	}

	public boolean getAttribute(String attr, boolean defaultValue) {
		return doGetPreferenceStore().getBoolean(attr);
	}

}
