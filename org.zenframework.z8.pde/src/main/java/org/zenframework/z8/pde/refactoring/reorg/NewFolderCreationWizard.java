package org.zenframework.z8.pde.refactoring.reorg;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import org.zenframework.z8.compiler.core.ILanguageElement;
import org.zenframework.z8.pde.Plugin;
import org.zenframework.z8.pde.PluginImages;
import org.zenframework.z8.pde.refactoring.messages.RefactoringMessages;

public class NewFolderCreationWizard extends NewElementWizard {
    private NewFolderWizardPage fPage;

    public NewFolderCreationWizard() {
        super();
        setDefaultPageImageDescriptor(PluginImages.DESC_WIZBAN_NEWFOLDER);
        setDialogSettings(Plugin.getDefault().getDialogSettings());
        setWindowTitle(RefactoringMessages.NewFolderCreationWizard_title);
    }

    @Override
    public void addPages() {
        super.addPages();
        fPage = new NewFolderWizardPage();
        addPage(fPage);
        fPage.init(getSelection());
    }

    @Override
    protected void finishPage(IProgressMonitor monitor) throws InterruptedException, CoreException {
        fPage.createFolder(monitor); // use the full progress monitor
    }

    @Override
    public boolean performFinish() {
        boolean res = super.performFinish();
        if(res) {
            selectAndReveal(fPage.getModifiedResource());
        }
        return res;
    }

    @Override
    public ILanguageElement getCreatedElement() {
        return fPage.getNewFolder();
    }
}
