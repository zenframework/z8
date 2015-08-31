package org.zenframework.z8.pde.refactoring.reorg;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWizard;

import org.zenframework.z8.pde.Plugin;
import org.zenframework.z8.pde.refactoring.messages.RefactoringMessages;

public class CreateTargetQueries implements ICreateTargetQueries {
    private final Wizard fWizard;
    private final Shell fShell;

    public CreateTargetQueries(Wizard wizard) {
        fWizard = wizard;
        fShell = null;
    }

    public CreateTargetQueries(Shell shell) {
        fShell = shell;
        fWizard = null;
    }

    private Shell getShell() {
        if(fWizard != null)
            return fWizard.getContainer().getShell();
        else if(fShell != null)
            return fShell;
        else
            return Plugin.getActiveWorkbenchShell();
    }

    @Override
    public ICreateTargetQuery createNewFolderQuery() {
        return new ICreateTargetQuery() {
            @Override
            public Object getCreatedTarget(Object selection) {
                IWorkbenchWizard folderCreationWizard = new NewFolderCreationWizard();
                IWizardPage[] pages = openNewElementWizard(folderCreationWizard, getShell(), selection);
                NewFolderWizardPage page = (NewFolderWizardPage)pages[0];
                return page.getNewFolder();
            }

            @Override
            public String getNewButtonLabel() {
                return RefactoringMessages.ReorgMoveWizard_newFolder;
            }
        };
    }

    private IWizardPage[] openNewElementWizard(IWorkbenchWizard wizard, Shell shell, Object selection) {
        wizard.init(Plugin.getDefault().getWorkbench(), new StructuredSelection(selection));
        WizardDialog dialog = new WizardDialog(shell, wizard);
        PixelConverter converter = new PixelConverter(JFaceResources.getDialogFont());
        dialog.setMinimumPageSize(converter.convertWidthInCharsToPixels(70), converter.convertHeightInCharsToPixels(20));
        dialog.create();
        dialog.open();
        IWizardPage[] pages = wizard.getPages();
        return pages;
    }
}
