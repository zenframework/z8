package org.zenframework.z8.pde.refactoring;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.ui.refactoring.RefactoringWizard;
import org.eclipse.ltk.ui.refactoring.RefactoringWizardOpenOperation;
import org.eclipse.swt.widgets.Shell;

public class RefactoringStarter {
    private RefactoringSaveHelper fSaveHelper = new RefactoringSaveHelper();
    private RefactoringStatus fStatus;

    public void activate(Refactoring refactoring, RefactoringWizard wizard, Shell parent, String dialogTitle,
            boolean mustSaveEditors) throws CoreException {
        if(!canActivate(mustSaveEditors, parent)) {
            return;
        }

        try {
            RefactoringWizardOpenOperation op = new RefactoringWizardOpenOperation(wizard);
            int result = op.run(parent, dialogTitle);
            fStatus = op.getInitialConditionCheckingStatus();
            if(result == IDialogConstants.CANCEL_ID
                    || result == RefactoringWizardOpenOperation.INITIAL_CONDITION_CHECKING_FAILED)
                fSaveHelper.triggerBuild();
        }
        catch(InterruptedException e) {}
    }

    public RefactoringStatus getInitialConditionCheckingStatus() {
        return fStatus;
    }

    private boolean canActivate(boolean mustSaveEditors, Shell shell) {
        return !mustSaveEditors || fSaveHelper.saveEditors(shell);
    }
}
