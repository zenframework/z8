package org.zenframework.z8.pde.refactoring.changes;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import org.zenframework.z8.compiler.workspace.CompilationUnit;
import org.zenframework.z8.compiler.workspace.Folder;
import org.zenframework.z8.pde.refactoring.messages.Messages;
import org.zenframework.z8.pde.refactoring.messages.RefactoringMessages;

public class MoveCompilationUnitChange extends CompilationUnitReorgChange {
    private boolean fUndoable;
    private long fStampToRestore;

    public MoveCompilationUnitChange(CompilationUnit cu, Folder newFolder) {
        super(cu, newFolder);
        fStampToRestore = IResource.NULL_STAMP;
    }

    private MoveCompilationUnitChange(CompilationUnit cu, Folder newFolder, long stampToRestore) {
        this(cu, newFolder);
        fStampToRestore = stampToRestore;
    }

    @Override
    public String getName() {
        return Messages.format(RefactoringMessages.MoveCompilationUnitChange_name, new String[] { getCu().getName(),
                getDestinationFolder().getName() });
    }

    @Override
    public RefactoringStatus isValid(IProgressMonitor pm) throws CoreException {
        return super.isValid(pm, READ_ONLY | SAVE_IF_DIRTY);
    }

    @Override
    Change doPerformReorg(IProgressMonitor pm) throws CoreException, OperationCanceledException {
        String name;
        String newName = getNewName();

        if(newName == null)
            name = getCu().getName();
        else
            name = newName;

        long currentStamp = IResource.NULL_STAMP;

        IResource resource = getCu().getResource();

        if(resource != null) {
            currentStamp = resource.getModificationStamp();
        }

        CompilationUnit cu = getDestinationFolder().getCompilationUnit(name);

        fUndoable = cu == null || !cu.getResource().isAccessible();

        //		getCu().move(getDestinationFolder(), null, newName, true, pm);

        if(fStampToRestore != IResource.NULL_STAMP) {
            CompilationUnit moved = getDestinationFolder().getCompilationUnit(name);

            IResource movedResource = moved.getResource();

            if(movedResource != null) {
                movedResource.revertModificationStamp(fStampToRestore);
            }
        }

        if(fUndoable) {
            return new MoveCompilationUnitChange(getCu(), getOldFolder(), currentStamp);
        }
        else {
            return null;
        }
    }
}
