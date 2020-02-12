package org.zenframework.z8.pde;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.zenframework.z8.pde.build.Z8ProjectBuilder;

public class Z8ProjectNature implements IProjectNature {
	public static final String Name = "Z8 Project Nature";
	public static final String Id = "org.zenframework.z8.pde.ProjectNature";

	protected IProject m_project;

	@Override
	public void configure() throws CoreException {
		if(m_project == null)
			return;

		IProjectDescription description = m_project.getDescription();
		ICommand[] oldBuildSpec = description.getBuildSpec();

		if (getZ8CommandIndex(oldBuildSpec) >= 0)
			return;
		
		ICommand[] newBuildSpec = new ICommand[oldBuildSpec.length + 1];
		System.arraycopy(oldBuildSpec, 0, newBuildSpec, 1, oldBuildSpec.length);
		ICommand newCommand = description.newCommand();
		Map<String, String> args = new HashMap<String, String>();
		args.put(BuildPathManager.JAVA_OUTPUT_PATH_KEY, BuildPathManager.JAVA_OUTPUT_DEFAULT_FOLDER);
		newCommand.setArguments(args);
		newCommand.setBuilderName(Z8ProjectBuilder.Id);
		newBuildSpec[0] = newCommand;
		description.setBuildSpec(newBuildSpec);
		m_project.setDescription(description, IResource.FORCE, null);
	}

	@Override
	public void deconfigure() throws CoreException {
		// TODO Auto-generated method stub
	}

	@Override
	public IProject getProject() {
		return this.m_project;
	}

	@Override
	public void setProject(IProject project) {
		m_project = project;
	}

	private static int getZ8CommandIndex(ICommand[] buildSpec) {
		for (int i = 0; i < buildSpec.length; ++i) {
			if (buildSpec[i].getBuilderName().equals(Z8ProjectBuilder.Id))
				return i;
		}
		return -1;
	}
}
