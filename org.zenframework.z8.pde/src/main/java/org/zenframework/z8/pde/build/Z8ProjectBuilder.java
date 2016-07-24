package org.zenframework.z8.pde.build;

import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.PlatformUI;

import org.zenframework.z8.compiler.workspace.Project;
import org.zenframework.z8.compiler.workspace.Workspace;
import org.zenframework.z8.pde.Plugin;
import org.zenframework.z8.pde.jobs.Z8Job;

public class Z8ProjectBuilder extends IncrementalProjectBuilder {
	final static public String Id = "org.zenframework.z8.pde.ProjectBuilder";

	final static public QualifiedName PrelaunchBuild = new QualifiedName("org.zenframework.z8.pde", "PrelaunchBuild");

	public Z8ProjectBuilder() {
	}

	protected void abort(String message, Throwable exception, int code) throws CoreException {
		throw new CoreException(new Status(IStatus.ERROR, Plugin.getUniqueIdentifier(), code, message, exception));
	}

	@Override
	@SuppressWarnings("rawtypes")
	protected IProject[] build(int kind, Map args, IProgressMonitor monitor) throws CoreException {
		IProject iProject = getProject();
		Project project = Workspace.getInstance().getProject(iProject);

		final boolean[] saved = { true };

		if(getProject().getSessionProperty(PrelaunchBuild) != null) {
			Display.getDefault().syncExec(new Runnable() {
				@Override
				public void run() {
					saved[0] = PlatformUI.getWorkbench().saveAllEditors(true);
				}
			});
		}

		if(saved[0]) {
			BuildMessageConsumer consumer = new BuildMessageConsumer();

			try {
				project.build(consumer, monitor);
				iProject.refreshLocal(IResource.DEPTH_INFINITE, monitor);
			} catch(Throwable e) {
				abort("Internal compiler error", e, IResourceStatus.BUILD_FAILED);
			} finally {
				forgetLastBuiltState();
			}
		}

		return null;
	}

	@Override
	protected void clean(IProgressMonitor monitor) throws CoreException {
		IProject project = getProject();

		try {
			project.deleteMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE);
			forgetLastBuiltState();
		} catch(Throwable e) {
			Plugin.log(e);
		}
	}

	public static void focusOnProblemsView() {
		Z8Job job = new Z8Job(Z8Job.JobType.jtShowView, IPageLayout.ID_PROBLEM_VIEW);
		job.startJob();
	}
}
