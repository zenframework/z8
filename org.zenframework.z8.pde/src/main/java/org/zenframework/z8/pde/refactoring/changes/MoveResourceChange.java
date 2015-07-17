package org.zenframework.z8.pde.refactoring.changes;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import org.zenframework.z8.pde.refactoring.messages.Messages;
import org.zenframework.z8.pde.refactoring.messages.RefactoringMessages;

public class MoveResourceChange extends ResourceReorgChange {
    public MoveResourceChange(IResource res, IContainer dest) {
        super(res, dest, null);
    }

    @Override
    public RefactoringStatus isValid(IProgressMonitor pm) throws CoreException {
        return super.isValid(pm, DIRTY);
    }

    @Override
    protected Change doPerformReorg(IPath path, IProgressMonitor pm) throws CoreException {
        getResource().move(path, getReorgFlags(), pm);
        return null;
    }

    @Override
    public String getName() {
        return Messages.format(RefactoringMessages.MoveResourceChange_move, new String[] {
                getResource().getFullPath().toString(), getDestination().getName() });
    }
}
