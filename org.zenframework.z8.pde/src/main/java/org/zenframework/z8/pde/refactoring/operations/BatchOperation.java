package org.zenframework.z8.pde.refactoring.operations;

import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;

import org.zenframework.z8.pde.refactoring.Z8Status;

public class BatchOperation extends Z8Operation {
    protected IWorkspaceRunnable m_runnable;

    public BatchOperation(IWorkspaceRunnable runnable) {
        m_runnable = runnable;
    }

    @Override
    protected boolean canModifyRoots() {
        return true;
    }

    @Override
    protected void executeOperation() throws CoreException {
        m_runnable.run(m_progressMonitor);
    }

    @Override
    protected IStatus verify() {
        return Z8Status.VERIFIED_OK;
    }
}
