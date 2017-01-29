package org.zenframework.z8.pde.jobs;

import java.util.LinkedHashMap;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.progress.WorkbenchJob;

import org.zenframework.z8.pde.Plugin;

public class Z8Job extends WorkbenchJob {
	private JobType jobType;
	public String pageLayout;

	private static LinkedHashMap<JobType, String> mass;
	static {
		mass = new LinkedHashMap<JobType, String>();
		mass.put(JobType.jtShowView, "Open view");
	}

	public enum JobType {
		jtShowView
	}

	public Z8Job(JobType jtype) {
		super(mass.get(JobType.jtShowView));
		jobType = jtype;
	}

	public Z8Job(JobType jtype, String pgLayout) {
		super(mass.get(JobType.jtShowView));
		jobType = jtype;
		pageLayout = pgLayout;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.progress.UIJob#runInUIThread(org.eclipse.core.runtime.
	 * IProgressMonitor)
	 */
	@Override
	public IStatus runInUIThread(IProgressMonitor monitor) {

		switch(jobType) {
		case jtShowView:
			try {
				Plugin.getActiveWorkbenchWindow().getActivePage().showView(pageLayout);
			} catch(Exception e) {
				Plugin.log(e);
			}
		default:
		}
		return Status.OK_STATUS;
	}

	public void startJob() {
		setSystem(true);
		setPriority(Job.DECORATE);
		schedule();
	}

}
