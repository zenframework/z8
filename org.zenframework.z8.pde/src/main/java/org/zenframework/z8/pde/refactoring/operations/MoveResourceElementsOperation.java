package org.zenframework.z8.pde.refactoring.operations;

import org.zenframework.z8.compiler.core.ILanguageElement;
import org.zenframework.z8.pde.refactoring.messages.RefactoringMessages;

public class MoveResourceElementsOperation extends CopyResourceElementsOperation {
    public MoveResourceElementsOperation(ILanguageElement[] elementsToMove, ILanguageElement[] destContainers, boolean force) {
        super(elementsToMove, destContainers, force);
    }

    @Override
    protected String getMainTaskName() {
        return RefactoringMessages.operation_moveResourceProgress;
    }

    @Override
    protected boolean isMove() {
        return true;
    }
}
