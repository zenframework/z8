package org.zenframework.z8.pde.refactoring.operations;

import org.eclipse.core.runtime.CoreException;

import org.zenframework.z8.compiler.core.ILanguageElement;
import org.zenframework.z8.compiler.workspace.CompilationUnit;
import org.zenframework.z8.compiler.workspace.Folder;
import org.zenframework.z8.pde.refactoring.Z8StatusConstants;
import org.zenframework.z8.pde.refactoring.messages.RefactoringMessages;

public class RenameResourceElementsOperation extends MoveResourceElementsOperation {
    public RenameResourceElementsOperation(ILanguageElement[] elements, ILanguageElement[] destinations, String[] newNames,
            boolean force) {
        super(elements, destinations, force);
        setRenamings(newNames);
    }

    @Override
    protected String getMainTaskName() {
        return RefactoringMessages.operation_renameResourceProgress;
    }

    @Override
    protected boolean isRename() {
        return true;
    }

    @Override
    protected void verify(ILanguageElement element) throws CoreException {
        super.verify(element);

        if(!(element instanceof CompilationUnit) && !(element instanceof Folder)) {
            error(Z8StatusConstants.INVALID_ELEMENT_TYPES, element);
        }

        verifyRenaming(element);
    }
}
