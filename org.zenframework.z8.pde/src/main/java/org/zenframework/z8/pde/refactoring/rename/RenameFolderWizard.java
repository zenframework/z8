package org.zenframework.z8.pde.refactoring.rename;

import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import org.zenframework.z8.pde.PluginImages;
import org.zenframework.z8.pde.refactoring.messages.RefactoringMessages;

public class RenameFolderWizard extends RenameRefactoringWizard {
    public RenameFolderWizard(Refactoring refactoring) {
        super(refactoring, RefactoringMessages.RenameFolderWizard_defaultPageTitle,
                RefactoringMessages.RenameFolderWizard_inputPage_description, PluginImages.DESC_WIZBAN_REFACTOR_FOLDER, null);
    }

    @Override
    protected RenameInputWizardPage createInputPage(String message, String initialSetting) {
        return new RenameFolderInputWizardPage(message, null, initialSetting) {
            @Override
            protected RefactoringStatus validateTextField(String text) {
                return validateNewName(text);
            }
        };
    }

    private static class RenameFolderInputWizardPage extends RenameInputWizardPage {
        public RenameFolderInputWizardPage(String message, String contextHelpId, String initialValue) {
            super(message, contextHelpId, true, initialValue);
        }

        @Override
        protected void addAdditionalOptions(Composite composite, RowLayouter layouter) {
            Label separator = new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL);
            separator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            layouter.perform(separator);
        }
    }
}
