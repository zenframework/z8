package org.zenframework.z8.oda.designer.ui.wizards;

import java.util.Properties;

import org.eclipse.swt.widgets.Composite;

public class DataSourceWizardPage extends org.eclipse.datatools.connectivity.oda.design.ui.wizards.DataSourceWizardPage {
	private DataSourcePageHelper pageHelper;
	private Properties folderProperties;

	public DataSourceWizardPage(String pageName) {
		super(pageName);
	}

	@Override
	public void createPageCustomControl(Composite parent) {
		if(pageHelper == null)
			pageHelper = new DataSourcePageHelper(this);
		pageHelper.createCustomControl(parent);
		pageHelper.initCustomControl(folderProperties);
		setPingButtonVisible(false);
	}

	@Override
	public void setInitialProperties(Properties dataSourceProps) {
		folderProperties = dataSourceProps;
		if(pageHelper == null)
			return;
		pageHelper.initCustomControl(folderProperties);
	}

	@Override
	public Properties collectCustomProperties() {
		if(pageHelper != null)
			return pageHelper.collectCustomProperties(folderProperties);

		return (folderProperties != null) ? folderProperties : new Properties();
	}

	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		getControl().setFocus();
	}

	@Override
	public void refresh() {
		enableAllControls(getControl(), isSessionEditable());
	}

}
