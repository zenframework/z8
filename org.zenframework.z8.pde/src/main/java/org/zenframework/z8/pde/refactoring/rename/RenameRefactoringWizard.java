package org.zenframework.z8.pde.refactoring.rename;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.ui.refactoring.RefactoringWizard;

import org.zenframework.z8.pde.Plugin;
import org.zenframework.z8.pde.refactoring.messages.RefactoringMessages;

public class RenameRefactoringWizard extends RefactoringWizard {
    private String fInputPageDescription;
    private String fPageContextHelpId;
    private ImageDescriptor fInputPageImageDescriptor;

    public RenameRefactoringWizard(Refactoring refactoring, String defaultPageTitle, String inputPageDescription,
            ImageDescriptor inputPageImageDescriptor, String pageContextHelpId) {
        super(refactoring, DIALOG_BASED_USER_INTERFACE);
        setDefaultPageTitle(defaultPageTitle);
        fInputPageDescription = inputPageDescription;
        fInputPageImageDescriptor = inputPageImageDescriptor;
        fPageContextHelpId = pageContextHelpId;
    }

    @Override
    protected void addUserInputPages() {
        String initialSetting = getNameUpdating().getCurrentElementName();
        RenameInputWizardPage inputPage = createInputPage(fInputPageDescription, initialSetting);
        inputPage.setImageDescriptor(fInputPageImageDescriptor);
        addPage(inputPage);
    }

    private INameUpdating getNameUpdating() {
        return (INameUpdating)getRefactoring().getAdapter(INameUpdating.class);
    }

    protected RenameInputWizardPage createInputPage(String message, String initialSetting) {
        return new RenameInputWizardPage(message, fPageContextHelpId, true, initialSetting) {
            @Override
            protected RefactoringStatus validateTextField(String text) {
                return validateNewName(text);
            }
        };
    }

    protected RefactoringStatus validateNewName(String newName) {
        INameUpdating ref = getNameUpdating();
        ref.setNewElementName(newName);
        try {
            return ref.checkNewElementName(newName);
        }
        catch(CoreException e) {
            Plugin.log(e);
            return RefactoringStatus.createFatalErrorStatus(RefactoringMessages.RenameRefactoringWizard_internal_error);
        }
    }
}
