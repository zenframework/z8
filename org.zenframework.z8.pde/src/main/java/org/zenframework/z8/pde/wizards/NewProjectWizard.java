package org.zenframework.z8.pde.wizards;

import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.ui.wizards.newresource.BasicNewProjectResourceWizard;

import org.zenframework.z8.pde.Z8ProjectNature;
import org.zenframework.z8.pde.Plugin;

public class NewProjectWizard extends BasicNewProjectResourceWizard {

	@Override
	public boolean performFinish() {
		if(!super.performFinish())
			return false;
		updateNatures();
		return true;
	}

	protected void updateNatures() {
		try {
			IProjectDescription d = getNewProject().getDescription();
			d.setNatureIds(new String[] { Z8ProjectNature.Id });
			getNewProject().setDescription(d, 0, null);
		} catch(Exception e) {
			Plugin.log(e);
		}
	}

}
