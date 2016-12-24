package org.zenframework.z8.oda.designer.ui.wizards;

import java.util.Properties;

import org.eclipse.datatools.connectivity.oda.design.ui.wizards.DataSourceEditorPage;
import org.eclipse.swt.widgets.Composite;

public class DataSourcePropertyPage extends DataSourceEditorPage {
	private DataSourcePageHelper pageHelper;

	public DataSourcePropertyPage() {
		super();
	}

	@Override
	public Properties collectCustomProperties(Properties profileProps) {
		if(pageHelper == null)
			return profileProps;

		return pageHelper.collectCustomProperties(profileProps);
	}

	@Override
	protected void createAndInitCustomControl(Composite parent, Properties profileProps) {
		if(pageHelper == null)
			pageHelper = new DataSourcePageHelper(this);

		pageHelper.createCustomControl(parent);
		this.setPingButtonVisible(false);
		pageHelper.initCustomControl(profileProps);
	}

	@Override
	public void refresh(Properties customConnectionProps) {
		pageHelper.initCustomControl(customConnectionProps);

		enableAllControls(getControl(), isSessionEditable());
	}

}
