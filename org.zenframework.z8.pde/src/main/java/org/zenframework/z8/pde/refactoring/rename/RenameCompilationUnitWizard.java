package org.zenframework.z8.pde.refactoring.rename;

import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.RenameRefactoring;

import org.zenframework.z8.pde.PluginImages;
import org.zenframework.z8.pde.refactoring.messages.RefactoringMessages;
import org.zenframework.z8.pde.refactoring.processors.rename.RenameCompilationUnitProcessor;

public class RenameCompilationUnitWizard extends RenameTypeWizard {
    public RenameCompilationUnitWizard(Refactoring refactoring) {
        super(refactoring, RefactoringMessages.RenameCompilationUnitWizard_defaultPageTitle,
                RefactoringMessages.RenameCompilationUnitWizard_inputPage_description, PluginImages.DESC_WIZBAN_REFACTOR_CU,
                null);
    }

    @Override
    protected RefactoringStatus validateNewName(String newName) {
        String fullName = newName + ".bl";
        return super.validateNewName(fullName);
    }

    @Override
    protected RenameInputWizardPage createInputPage(String message, String initialSetting) {
        return new RenameTypeWizardInputPage(message, null, true, initialSetting) {
            @Override
            protected RefactoringStatus validateTextField(String text) {
                return validateNewName(text);
            }

            @Override
            protected String getNewName(INameUpdating nameUpdating) {
                String result = nameUpdating.getNewElementName();

                int index = result.lastIndexOf('.');

                if(index != -1) {
                    return result.substring(0, index);
                }

                return result;
            }
        };
    }

    @Override
    protected boolean isRenameType() {
        return getCompilationUnitProcessor().isWillRenameType();
    }

    private RenameCompilationUnitProcessor getCompilationUnitProcessor() {
        return ((RenameCompilationUnitProcessor)((RenameRefactoring)getRefactoring()).getProcessor());
    }
}
