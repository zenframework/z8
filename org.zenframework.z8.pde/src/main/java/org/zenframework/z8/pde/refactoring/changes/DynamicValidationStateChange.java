package org.zenframework.z8.pde.refactoring.changes;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import org.zenframework.z8.pde.Plugin;
import org.zenframework.z8.pde.refactoring.action.WorkspaceTracker;
import org.zenframework.z8.pde.refactoring.messages.RefactoringMessages;
import org.zenframework.z8.pde.refactoring.operations.BatchOperation;

public class DynamicValidationStateChange extends CompositeChange implements WorkspaceTracker.Listener {
    private boolean m_listenerRegistered = false;
    private RefactoringStatus m_validationState = null;
    private long m_timeStamp;

    private static final long LIFE_TIME = 30 * 60 * 1000;

    public DynamicValidationStateChange(Change change) {
        super(change.getName());
        add(change);
        markAsSynthetic();
    }

    public DynamicValidationStateChange(String name) {
        super(name);
        markAsSynthetic();
    }

    public DynamicValidationStateChange(String name, Change[] changes) {
        super(name, changes);
        markAsSynthetic();
    }

    @Override
    public void initializeValidationData(IProgressMonitor pm) {
        super.initializeValidationData(pm);
        WorkspaceTracker.INSTANCE.addListener(this);
        m_listenerRegistered = true;
        m_timeStamp = System.currentTimeMillis();
    }

    @Override
    public void dispose() {
        if(m_listenerRegistered) {
            WorkspaceTracker.INSTANCE.removeListener(this);
            m_listenerRegistered = false;
        }
        super.dispose();
    }

    @Override
    public RefactoringStatus isValid(IProgressMonitor pm) throws CoreException {
        if(m_validationState == null) {
            return super.isValid(pm);
        }
        return m_validationState;
    }

    @Override
    public Change perform(IProgressMonitor pm) throws CoreException {
        final Change[] result = new Change[1];

        IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
            @Override
            public void run(IProgressMonitor monitor) throws CoreException {
                result[0] = DynamicValidationStateChange.super.perform(monitor);
            }
        };

        IWorkspace workspace = ResourcesPlugin.getWorkspace();

        if(workspace.isTreeLocked()) {
            new BatchOperation(runnable).run(pm);
        }
        else {
            workspace.run(new BatchOperation(runnable), ResourcesPlugin.getWorkspace().getRoot(), IWorkspace.AVOID_UPDATE,
                    pm);
        }

        return result[0];
    }

    @Override
    protected Change createUndoChange(Change[] childUndos) {
        DynamicValidationStateChange result = new DynamicValidationStateChange(getName());
        for(int i = 0; i < childUndos.length; i++) {
            result.add(childUndos[i]);
        }
        return result;
    }

    @Override
    public void workspaceChanged() {
        long currentTime = System.currentTimeMillis();
        if(currentTime - m_timeStamp < LIFE_TIME)
            return;

        m_validationState = RefactoringStatus
                .createFatalErrorStatus(RefactoringMessages.DynamicValidationStateChange_workspace_changed);

        WorkspaceTracker.INSTANCE.removeListener(this);

        m_listenerRegistered = false;

        Change[] children = clear();

        for(int i = 0; i < children.length; i++) {
            final Change change = children[i];

            SafeRunner.run(new ISafeRunnable() {
                @Override
                public void run() throws Exception {
                    change.dispose();
                }

                @Override
                public void handleException(Throwable exception) {
                    Plugin.log(exception);
                }
            });
        }
    }
}
