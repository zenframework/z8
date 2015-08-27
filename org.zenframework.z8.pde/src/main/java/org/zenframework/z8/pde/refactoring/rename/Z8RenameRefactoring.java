package org.zenframework.z8.pde.refactoring.rename;

import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.RefactoringArguments;
import org.eclipse.ltk.core.refactoring.participants.RefactoringProcessor;
import org.eclipse.ltk.core.refactoring.participants.RenameProcessor;
import org.eclipse.ltk.core.refactoring.participants.RenameRefactoring;

import org.zenframework.z8.pde.refactoring.IScriptableRefactoring;
import org.zenframework.z8.pde.refactoring.messages.Messages;
import org.zenframework.z8.pde.refactoring.messages.RefactoringMessages;

public class Z8RenameRefactoring extends RenameRefactoring implements IScriptableRefactoring {
    public Z8RenameRefactoring(RenameProcessor processor) {
        super(processor);
    }

    @Override
    public RefactoringStatus initialize(RefactoringArguments arguments) {
        final RefactoringProcessor processor = getProcessor();

        if(processor instanceof IScriptableRefactoring) {
            return ((IScriptableRefactoring)processor).initialize(arguments);
        }

        return RefactoringStatus.createFatalErrorStatus(Messages.format(
                RefactoringMessages.ProcessorBasedRefactoring_error_unsupported_initialization, getProcessor()
                        .getIdentifier()));
    }
}
