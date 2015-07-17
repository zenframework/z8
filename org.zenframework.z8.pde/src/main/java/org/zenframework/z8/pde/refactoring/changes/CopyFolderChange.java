package org.zenframework.z8.pde.refactoring.changes;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.Change;

import org.zenframework.z8.compiler.workspace.Folder;
import org.zenframework.z8.pde.refactoring.messages.Messages;
import org.zenframework.z8.pde.refactoring.messages.RefactoringMessages;
import org.zenframework.z8.pde.refactoring.reorg.INewNameQuery;

public class CopyFolderChange extends FolderReorgChange {
    public CopyFolderChange(Folder source, Folder destination, INewNameQuery nameQuery) {
        super(source, destination, nameQuery);
    }

    @Override
    protected Change doPerformReorg(IProgressMonitor pm) throws CoreException, OperationCanceledException {
        //		getFolder().copy(getDestination(), null, getNewName(), true, pm);
        return null;
    }

    @Override
    public String getName() {
        return Messages.format(RefactoringMessages.CopyFolderChange_copy, new String[] { getFolder().getName(),
                getDestination().getName() });
    }
}
