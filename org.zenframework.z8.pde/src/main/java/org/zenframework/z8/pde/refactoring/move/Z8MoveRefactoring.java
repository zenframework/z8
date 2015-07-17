package org.zenframework.z8.pde.refactoring.move;

import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.MoveProcessor;
import org.eclipse.ltk.core.refactoring.participants.MoveRefactoring;
import org.eclipse.ltk.core.refactoring.participants.RefactoringArguments;
import org.eclipse.ltk.core.refactoring.participants.RefactoringProcessor;

import org.zenframework.z8.pde.refactoring.IScriptableRefactoring;
import org.zenframework.z8.pde.refactoring.messages.Messages;
import org.zenframework.z8.pde.refactoring.messages.RefactoringMessages;

public class Z8MoveRefactoring extends MoveRefactoring implements IScriptableRefactoring {
    public Z8MoveRefactoring(MoveProcessor processor) {
        super(processor);
    }

    @Override
    public RefactoringStatus initialize(RefactoringArguments arguments) {
        RefactoringProcessor processor = getProcessor();

        if(processor instanceof IScriptableRefactoring) {
            return ((IScriptableRefactoring)processor).initialize(arguments);
        }

        return RefactoringStatus.createFatalErrorStatus(Messages.format(
                RefactoringMessages.ProcessorBasedRefactoring_error_unsupported_initialization, getProcessor()
                        .getIdentifier()));
    }
}
