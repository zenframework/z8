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
import org.zenframework.z8.pde.refactoring.reorg.INewNameQuery;

public class CopyResourceChange extends ResourceReorgChange {
    public CopyResourceChange(IResource res, IContainer dest, INewNameQuery newNameQuery) {
        super(res, dest, newNameQuery);
    }

    @Override
    public RefactoringStatus isValid(IProgressMonitor pm) throws CoreException {
        return super.isValid(pm, NONE);
    }

    @Override
    protected Change doPerformReorg(IPath path, IProgressMonitor pm) throws CoreException {
        getResource().copy(path, getReorgFlags(), pm);
        return null;
    }

    @Override
    public String getName() {
        return Messages.format(RefactoringMessages.CopyResourceString_copy, new String[] {
                getResource().getFullPath().toString(), getDestination().getName() });
    }
}
