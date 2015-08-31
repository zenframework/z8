package org.zenframework.z8.pde.refactoring.changes;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.Change;

import org.zenframework.z8.compiler.core.ILanguageElement;
import org.zenframework.z8.compiler.workspace.Folder;
import org.zenframework.z8.pde.refactoring.messages.Messages;
import org.zenframework.z8.pde.refactoring.messages.RefactoringMessages;
import org.zenframework.z8.pde.refactoring.operations.MoveResourceElementsOperation;

public class MoveFolderChange extends FolderReorgChange {
    public MoveFolderChange(Folder source, Folder destination) {
        super(source, destination, null);
    }

    @Override
    protected Change doPerformReorg(IProgressMonitor pm) throws CoreException, OperationCanceledException {
        ILanguageElement[] elements = new ILanguageElement[] { getFolder() };
        ILanguageElement[] containers = new ILanguageElement[] { getDestination() };

        String newName = getNewName();
        String[] renamings = null;

        if(newName != null)
            renamings = new String[] { getNewName() };

        MoveResourceElementsOperation operation = new MoveResourceElementsOperation(elements, containers, true);
        operation.setRenamings(renamings);
        operation.runOperation(pm);

        return null;
    }

    @Override
    public String getName() {
        return Messages.format(RefactoringMessages.MoveFolderChange_move, new String[] { getFolder().getName(),
                getDestination().getName() });
    }
}
