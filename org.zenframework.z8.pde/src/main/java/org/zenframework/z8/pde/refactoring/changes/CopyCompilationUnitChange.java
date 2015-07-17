package org.zenframework.z8.pde.refactoring.changes;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import org.zenframework.z8.compiler.workspace.CompilationUnit;
import org.zenframework.z8.compiler.workspace.Folder;
import org.zenframework.z8.pde.refactoring.messages.Messages;
import org.zenframework.z8.pde.refactoring.messages.RefactoringMessages;
import org.zenframework.z8.pde.refactoring.reorg.INewNameQuery;

public class CopyCompilationUnitChange extends CompilationUnitReorgChange {
    public CopyCompilationUnitChange(CompilationUnit cu, Folder dest, INewNameQuery newNameQuery) {
        super(cu, dest, newNameQuery);
    }

    @Override
    public RefactoringStatus isValid(IProgressMonitor pm) throws CoreException {
        return super.isValid(pm, NONE);
    }

    @Override
    Change doPerformReorg(IProgressMonitor pm) throws CoreException, OperationCanceledException {
        //		getCu().copy(getDestinationFolder(), null, getNewName(), true, pm);
        return null;
    }

    @Override
    public String getName() {
        return Messages.format(RefactoringMessages.CopyCompilationUnitChange_copy, new String[] { getCu().getName(),
                getDestinationFolder().getName() });
    }
}
