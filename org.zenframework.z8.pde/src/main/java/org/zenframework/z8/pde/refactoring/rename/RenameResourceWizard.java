package org.zenframework.z8.pde.refactoring.rename;

import org.eclipse.ltk.core.refactoring.Refactoring;

import org.zenframework.z8.pde.PluginImages;
import org.zenframework.z8.pde.refactoring.messages.RefactoringMessages;

public class RenameResourceWizard extends RenameRefactoringWizard {
    public RenameResourceWizard(Refactoring refactoring) {
        super(refactoring, RefactoringMessages.RenameResourceWizard_defaultPageTitle,
                RefactoringMessages.RenameResourceWizard_inputPage_description, PluginImages.DESC_WIZBAN_REFACTOR, null);
    }
}
