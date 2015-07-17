package org.zenframework.z8.pde.refactoring.changes;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import org.zenframework.z8.compiler.workspace.Folder;
import org.zenframework.z8.pde.refactoring.reorg.INewNameQuery;

abstract class FolderReorgChange extends Z8Change {
    private Folder m_folder;
    private Folder m_destination;
    private INewNameQuery m_nameQuery;

    FolderReorgChange(Folder source, Folder destination, INewNameQuery nameQuery) {
        m_folder = source;
        m_destination = destination;
        m_nameQuery = nameQuery;
    }

    abstract Change doPerformReorg(IProgressMonitor pm) throws CoreException, OperationCanceledException;

    @Override
    public RefactoringStatus isValid(IProgressMonitor pm) throws CoreException {
        return isValid(pm, NONE);
    }

    @Override
    public final Change perform(IProgressMonitor pm) throws CoreException, OperationCanceledException {
        pm.beginTask(getName(), 1);
        try {
            final Change result = doPerformReorg(pm);
            return result;
        }
        finally {
            pm.done();
        }
    }

    @Override
    public Object getModifiedElement() {
        return getFolder();
    }

    Folder getDestination() {
        return m_destination;
    }

    Folder getFolder() {
        return m_folder;
    }

    String getNewName() throws OperationCanceledException {
        if(m_nameQuery == null)
            return null;
        return m_nameQuery.getNewName();
    }
}
