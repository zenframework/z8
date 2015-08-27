package org.zenframework.z8.pde.refactoring;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.ui.refactoring.RefactoringWizard;
import org.eclipse.swt.widgets.Shell;

public class UserInterfaceStarter {
    private RefactoringWizard fWizard;

    public void initialize(RefactoringWizard wizard) {
        fWizard = wizard;
    }

    public void activate(Refactoring refactoring, Shell parent, boolean mustSaveEditors) throws CoreException {
        new RefactoringStarter().activate(refactoring, fWizard, parent, fWizard.getDefaultPageTitle(), mustSaveEditors);
    }
}
