package org.zenframework.z8.pde.refactoring;

import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.RefactoringArguments;

public interface IScriptableRefactoring {
    public RefactoringStatus initialize(RefactoringArguments arguments);
}
