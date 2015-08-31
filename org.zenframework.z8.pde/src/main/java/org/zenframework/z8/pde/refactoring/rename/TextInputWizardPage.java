package org.zenframework.z8.pde.refactoring.rename;

import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

public abstract class TextInputWizardPage extends UserInputWizardPage {
    private String fInitialValue;
    private Text fTextField;

    public static final String PAGE_NAME = "TextInputPage";

    public TextInputWizardPage(String description, boolean isLastUserPage) {
        this(description, isLastUserPage, "");
    }

    public TextInputWizardPage(String description, boolean isLastUserPage, String initialValue) {
        super(PAGE_NAME);
        setDescription(description);
        fInitialValue = initialValue;
    }

    protected boolean isInitialInputValid() {
        return false;
    }

    protected boolean isEmptyInputValid() {
        return false;
    }

    protected String getText() {
        if(fTextField == null)
            return null;
        return fTextField.getText();
    }

    protected void setText(String text) {
        if(fTextField == null)
            return;
        fTextField.setText(text);
    }

    protected Text getTextField() {
        return fTextField;
    }

    public String getInitialValue() {
        return fInitialValue;
    }

    protected RefactoringStatus validateTextField(String text) {
        return null;
    }

    protected Text createTextInputField(Composite parent) {
        return createTextInputField(parent, SWT.BORDER);
    }

    protected Text createTextInputField(Composite parent, int style) {
        fTextField = new Text(parent, style);
        fTextField.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                textModified(getText());
            }
        });
        fTextField.setText(fInitialValue);
        TextFieldNavigationHandler.install(fTextField);
        return fTextField;
    }

    protected void textModified(String text) {
        if(!isEmptyInputValid() && "".equals(text)) {
            setPageComplete(false);
            setErrorMessage(null);
            restoreMessage();
            return;
        }

        if((!isInitialInputValid()) && fInitialValue.equals(text)) {
            setPageComplete(false);
            setErrorMessage(null);
            restoreMessage();
            return;
        }

        RefactoringStatus status = validateTextField(text);

        if(status == null)
            status = new RefactoringStatus();

        setPageComplete(status);
    }

    protected void restoreMessage() {
        setMessage(null);
    }

    @Override
    public void dispose() {
        fTextField = null;
    }

    @Override
    public void setVisible(boolean visible) {
        if(visible) {
            textModified(getText());
        }
        super.setVisible(visible);
        if(visible && fTextField != null) {
            fTextField.setFocus();
        }
    }
}
