package org.zenframework.z8.pde.build;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;

import org.zenframework.z8.compiler.workspace.CompilationUnit;
import org.zenframework.z8.compiler.workspace.Resource;
import org.zenframework.z8.compiler.workspace.ResourceListener;

public class OneReconciler implements ResourceListener {

	private CompilationUnit m_unit;

	private Runnable m_runnable;

	public OneReconciler(CompilationUnit unit, Runnable run) {
		m_unit = unit;
		m_runnable = run;
		if(unit.getType() != null)
			m_runnable.run();
		else {
			unit.installResourceListener(this);
			Job job = new Job("Build") {
				@Override
				public IStatus run(IProgressMonitor monitor) {
					ReconcileMessageConsumer consumer = new ReconcileMessageConsumer();
					m_unit.getProject().reconcile(m_unit.getResource(), null, consumer);
					return Status.OK_STATUS;
				}
			};
			job.schedule();
		}

	}

	@Override
	public void event(int type, Resource resource, Object object) {
		if(resource == m_unit && type == RESOURCE_CHANGED) {
			resource.uninstallResourceListener(this);
			Display.getDefault().asyncExec(m_runnable);
		}
	}

}
