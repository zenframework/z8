package org.zenframework.z8.oda.designer.ui.wizards;

import java.util.Properties;

import org.eclipse.jface.dialogs.DialogPage;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.PropertyPage;
import org.zenframework.z8.oda.designer.plugin.Plugin;

public class DataSourcePageHelper {
	private DialogPage page;

	private Text url;

	private String DEFAULT_MESSAGE;

	final private static String EMPTY_URL = Plugin.getResourceString("error.emptyDatabaseUrl");

	DataSourcePageHelper(WizardPage page) {
		DEFAULT_MESSAGE = Plugin.getResourceString("wizard.message.createDataSource");
		this.page = page;
	}

	DataSourcePageHelper(PreferencePage page) {
		DEFAULT_MESSAGE = Plugin.getResourceString("wizard.message.editDataSource");
		this.page = page;
	}

	void createCustomControl(Composite parent) {
		Composite content = new Composite(parent, SWT.NONE);

		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.verticalSpacing = 10;
		layout.marginBottom = 10;
		content.setLayout(layout);

		GridData gridData;

		new Label(content, SWT.RIGHT).setText(Plugin.getResourceString("wizard.label.url"));

		gridData = new GridData();
		gridData.horizontalSpan = 1;
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		url = new Text(content, SWT.BORDER | SWT.READ_ONLY);
		url.setLayoutData(gridData);

		addControlListeners();
		verifyProperties();
	}

	void initCustomControl(Properties profileProps) {
		url.setText(Plugin.getWebInfPath().toString());

		verifyProperties();
	}

	Properties collectCustomProperties(Properties props) {
		if(props == null)
			props = new Properties();

		return props;
	}

	private void addControlListeners() {
		url.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				if(!url.isFocusControl() && url.getText().trim().length() == 0) {
					return;
				}
				verifyProperties();
			}
		});
	}

	private boolean isURLBlank() {
		return Plugin.getWebInfPath().toString().isEmpty();
	}

	private void verifyProperties() {
		boolean urlBlank = isURLBlank();
		setPageComplete(!urlBlank);
		setMessage(urlBlank ? EMPTY_URL : DEFAULT_MESSAGE, urlBlank ? IMessageProvider.ERROR : IMessageProvider.INFORMATION);
	}

	private void setPageComplete(boolean complete) {
		if(page instanceof WizardPage)
			((WizardPage)page).setPageComplete(complete);
		else if(page instanceof PropertyPage)
			((PropertyPage)page).setValid(complete);
	}

	private void setMessage(String message, int type) {
		page.setMessage(message, type);
	}
}
