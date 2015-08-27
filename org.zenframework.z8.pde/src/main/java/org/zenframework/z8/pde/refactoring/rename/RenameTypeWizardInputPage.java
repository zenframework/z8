package org.zenframework.z8.pde.refactoring.rename;

import org.eclipse.jface.wizard.IWizardPage;

class RenameTypeWizardInputPage extends RenameInputWizardPage {
    public RenameTypeWizardInputPage(String description, String contextHelpId, boolean isLastUserPage, String initialValue) {
        super(description, contextHelpId, isLastUserPage, initialValue);
    }

    @Override
    public boolean canFlipToNextPage() {
        return isPageComplete();
    }

    @Override
    protected boolean performFinish() {
        boolean returner = super.performFinish();
        if(!returner && getContainer().getCurrentPage() != null)
            getContainer().getCurrentPage().setPreviousPage(this);
        return returner;
    }

    @Override
    public IWizardPage getNextPage() {
        IWizardPage nextPage = computeSuccessorPage();

        nextPage.setPreviousPage(this);
        return nextPage;
    }
}
