package org.zenframework.z8.pde.refactoring.rename;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.RefactoringProcessor;
import org.eclipse.ltk.core.refactoring.participants.RenameRefactoring;

import org.zenframework.z8.pde.PluginImages;
import org.zenframework.z8.pde.refactoring.messages.RefactoringMessages;
import org.zenframework.z8.pde.refactoring.processors.rename.RenameCompilationUnitProcessor;
import org.zenframework.z8.pde.refactoring.processors.rename.RenameTypeProcessor;

public class RenameTypeWizard extends RenameRefactoringWizard {
    public RenameTypeWizard(Refactoring refactoring) {
        this(refactoring, RefactoringMessages.RenameTypeWizard_defaultPageTitle,
                RefactoringMessages.RenameTypeWizardInputPage_description, PluginImages.DESC_WIZBAN_REFACTOR_TYPE, null);
    }

    public RenameTypeWizard(Refactoring refactoring, String defaultPageTitle, String inputPageDescription,
            ImageDescriptor inputPageImageDescriptor, String pageContextHelpId) {
        super(refactoring, defaultPageTitle, inputPageDescription, inputPageImageDescriptor, pageContextHelpId);
    }

    @Override
    protected void addUserInputPages() {
        super.addUserInputPages();

        //		if(isRenameType())
        //			addPage(new RenameTypeWizardSimilarElementsPage());
    }

    public RenameTypeProcessor getRenameTypeProcessor() {
        RefactoringProcessor proc = ((RenameRefactoring)getRefactoring()).getProcessor();

        if(proc instanceof RenameTypeProcessor) {
            return (RenameTypeProcessor)proc;
        }
        else if(proc instanceof RenameCompilationUnitProcessor) {
            RenameCompilationUnitProcessor rcu = (RenameCompilationUnitProcessor)proc;
            return rcu.getRenameTypeProcessor();
        }
        return null;
    }

    protected boolean isRenameType() {
        return true;
    }

    @Override
    protected RenameInputWizardPage createInputPage(String message, String initialSetting) {
        return new RenameTypeWizardInputPage(message, null, true, initialSetting) {
            @Override
            protected RefactoringStatus validateTextField(String text) {
                return validateNewName(text);
            }
        };
    }
}
