package org.zenframework.z8.pde;

import java.util.ArrayList;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.IStartup;

public class Startup implements IStartup {
	@Override
	public void earlyStartup() {
	}
}

class BuildProjectsJob extends Job {
	public BuildProjectsJob(String name) {
		super(name);
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {

		final int ticks = 100;

		ArrayList<IProject> projects = new ArrayList<IProject>(2);
		for(IProject proj : ResourcesPlugin.getWorkspace().getRoot().getProjects())
			try {
				if(proj.isOpen() && proj.hasNature(Z8ProjectNature.Id))
					projects.add(proj);
			} catch(Exception e) {
				Plugin.log(e);
			}

		monitor.beginTask(getName(), ticks);
		try {
			if(monitor.isCanceled())
				return Status.CANCEL_STATUS;
			for(IProject proj : projects) {
				monitor.subTask("Building project " + proj.getName());
				proj.build(IncrementalProjectBuilder.FULL_BUILD, null);
				monitor.worked(ticks / projects.size());
			}
		} catch(Exception e) {
			Plugin.log(e);
		} finally {
			monitor.done();
		}

		return Status.OK_STATUS;
	}

}
