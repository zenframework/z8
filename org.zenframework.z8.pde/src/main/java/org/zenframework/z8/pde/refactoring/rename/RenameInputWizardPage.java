package org.zenframework.z8.pde.refactoring.rename;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

import org.zenframework.z8.pde.refactoring.IReferenceUpdating;
import org.zenframework.z8.pde.refactoring.ITextUpdating;
import org.zenframework.z8.pde.refactoring.messages.RefactoringMessages;

abstract class RenameInputWizardPage extends TextInputWizardPage {
    private String m_helpContextID;
    private Button m_updateReferences;
    private Button m_updateTextualMatches;
    private Button m_leaveDelegateCheckBox;
    private Button m_deprecateDelegateCheckBox;

    private static final String UPDATE_TEXTUAL_MATCHES = "updateTextualMatches";

    public RenameInputWizardPage(String description, String contextHelpId, boolean isLastUserPage, String initialValue) {
        super(description, isLastUserPage, initialValue);
        m_helpContextID = contextHelpId;
    }

    @Override
    public void createControl(Composite parent) {
        Composite superComposite = new Composite(parent, SWT.NONE);
        setControl(superComposite);
        initializeDialogUnits(superComposite);
        superComposite.setLayout(new GridLayout());
        Composite composite = new Composite(superComposite, SWT.NONE);
        composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        composite.setLayout(layout);
        RowLayouter layouter = new RowLayouter(2);
        Label label = new Label(composite, SWT.NONE);
        label.setText(getLabelText());
        Text text = createTextInputField(composite);
        text.selectAll();
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.widthHint = convertWidthInCharsToPixels(25);
        text.setLayoutData(gd);
        layouter.perform(label, text, 1);
        Label separator = new Label(composite, SWT.NONE);
        GridData gridData = new GridData(SWT.FILL, SWT.FILL, false, false);
        gridData.heightHint = 2;
        separator.setLayoutData(gridData);
        int indent = convertWidthInCharsToPixels(2);
        addOptionalUpdateReferencesCheckbox(composite, layouter);
        addAdditionalOptions(composite, layouter);
        addOptionalUpdateTextualMatches(composite, layouter);
        addOptionalLeaveDelegateCheckbox(composite, layouter);
        addOptionalDeprecateDelegateCheckbox(composite, layouter, indent);
        updateForcePreview();
        Dialog.applyDialogFont(superComposite);
        PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), m_helpContextID);
    }

    protected void addAdditionalOptions(Composite composite, RowLayouter layouter) {}

    @Override
    public void setVisible(boolean visible) {
        if(visible) {
            INameUpdating nameUpdating = (INameUpdating)getRefactoring().getAdapter(INameUpdating.class);
            if(nameUpdating != null) {
                String newName = getNewName(nameUpdating);
                if(newName != null && newName.length() > 0 && !newName.equals(getInitialValue())) {
                    Text textField = getTextField();
                    textField.setText(newName);
                    textField.setSelection(0, newName.length());
                }
            }
        }
        super.setVisible(visible);
    }

    protected String getNewName(INameUpdating nameUpdating) {
        return nameUpdating.getNewElementName();
    }

    protected boolean saveSettings() {
        if(getContainer() instanceof Dialog)
            return ((Dialog)getContainer()).getReturnCode() == IDialogConstants.OK_ID;
        return true;
    }

    @Override
    public void dispose() {
        if(saveSettings()) {
            saveBooleanSetting(UPDATE_TEXTUAL_MATCHES, m_updateTextualMatches);
            DelegateUIHelper.saveLeaveDelegateSetting(m_leaveDelegateCheckBox);
            DelegateUIHelper.saveDeprecateDelegateSetting(m_deprecateDelegateCheckBox);
        }
        super.dispose();
    }

    private void addOptionalUpdateReferencesCheckbox(Composite result, RowLayouter layouter) {
        final IReferenceUpdating ref = (IReferenceUpdating)getRefactoring().getAdapter(IReferenceUpdating.class);
        if(ref == null || !ref.canEnableUpdateReferences())
            return;
        String title = RefactoringMessages.RenameInputWizardPage_update_references;
        boolean defaultValue = true;
        m_updateReferences = createCheckbox(result, title, defaultValue, layouter);
        ref.setUpdateReferences(m_updateReferences.getSelection());
        m_updateReferences.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                ref.setUpdateReferences(m_updateReferences.getSelection());
            }
        });
    }

    private void addOptionalUpdateTextualMatches(Composite result, RowLayouter layouter) {
        final ITextUpdating refactoring = (ITextUpdating)getRefactoring().getAdapter(ITextUpdating.class);
        if(refactoring == null || !refactoring.canEnableTextUpdating())
            return;
        String title = RefactoringMessages.RenameInputWizardPage_update_textual_matches;
        boolean defaultValue = getBooleanSetting(UPDATE_TEXTUAL_MATCHES, refactoring.getUpdateTextualMatches());
        m_updateTextualMatches = createCheckbox(result, title, defaultValue, layouter);
        refactoring.setUpdateTextualMatches(m_updateTextualMatches.getSelection());
        m_updateTextualMatches.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                refactoring.setUpdateTextualMatches(m_updateTextualMatches.getSelection());
                updateForcePreview();
            }
        });
    }

    private void addOptionalLeaveDelegateCheckbox(Composite result, RowLayouter layouter) {
        final IDelegateUpdating refactoring = (IDelegateUpdating)getRefactoring().getAdapter(IDelegateUpdating.class);
        if(refactoring == null || !refactoring.canEnableDelegateUpdating())
            return;
        m_leaveDelegateCheckBox = createCheckbox(result, refactoring.getDelegateUpdatingTitle(false),
                DelegateUIHelper.loadLeaveDelegateSetting(refactoring), layouter);
        refactoring.setDelegateUpdating(m_leaveDelegateCheckBox.getSelection());
        m_leaveDelegateCheckBox.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                refactoring.setDelegateUpdating(m_leaveDelegateCheckBox.getSelection());
            }
        });
    }

    private void addOptionalDeprecateDelegateCheckbox(Composite result, RowLayouter layouter, int marginWidth) {
        final IDelegateUpdating refactoring = (IDelegateUpdating)getRefactoring().getAdapter(IDelegateUpdating.class);
        if(refactoring == null || !refactoring.canEnableDelegateUpdating())
            return;
        m_deprecateDelegateCheckBox = new Button(result, SWT.CHECK);
        GridData data = new GridData();
        data.horizontalAlignment = GridData.FILL;
        data.horizontalIndent = (marginWidth + m_deprecateDelegateCheckBox.computeSize(SWT.DEFAULT, SWT.DEFAULT).x);
        m_deprecateDelegateCheckBox.setLayoutData(data);
        m_deprecateDelegateCheckBox.setText(DelegateUIHelper.getDeprecateDelegateCheckBoxTitle());
        m_deprecateDelegateCheckBox.setSelection(DelegateUIHelper.loadDeprecateDelegateSetting(refactoring));
        layouter.perform(m_deprecateDelegateCheckBox);
        refactoring.setDeprecateDelegates(m_deprecateDelegateCheckBox.getSelection());
        m_deprecateDelegateCheckBox.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                refactoring.setDeprecateDelegates(m_deprecateDelegateCheckBox.getSelection());
            }
        });
        if(m_leaveDelegateCheckBox != null) {
            m_deprecateDelegateCheckBox.setEnabled(m_leaveDelegateCheckBox.getSelection());
            m_leaveDelegateCheckBox.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    m_deprecateDelegateCheckBox.setEnabled(m_leaveDelegateCheckBox.getSelection());
                }
            });
        }
    }

    protected void updateLeaveDelegateCheckbox(int delegateCount) {
        if(m_leaveDelegateCheckBox == null)
            return;
        final IDelegateUpdating refactoring = (IDelegateUpdating)getRefactoring().getAdapter(IDelegateUpdating.class);
        m_leaveDelegateCheckBox.setEnabled(delegateCount > 0);
        m_leaveDelegateCheckBox.setText(refactoring.getDelegateUpdatingTitle(delegateCount > 1));
        if(delegateCount == 0) {
            m_leaveDelegateCheckBox.setSelection(false);
            refactoring.setDelegateUpdating(false);
        }
    }

    protected String getLabelText() {
        return RefactoringMessages.RenameInputWizardPage_new_name;
    }

    protected boolean getBooleanSetting(String key, boolean defaultValue) {
        String update = getRefactoringSettings().get(key);
        if(update != null)
            return Boolean.valueOf(update).booleanValue();
        else
            return defaultValue;
    }

    protected void saveBooleanSetting(String key, Button checkBox) {
        if(checkBox != null)
            getRefactoringSettings().put(key, checkBox.getSelection());
    }

    private static Button createCheckbox(Composite parent, String title, boolean value, RowLayouter layouter) {
        Button checkBox = new Button(parent, SWT.CHECK);
        checkBox.setText(title);
        checkBox.setSelection(value);
        layouter.perform(checkBox);
        return checkBox;
    }

    private void updateForcePreview() {
        boolean forcePreview = false;
        Refactoring refactoring = getRefactoring();
        ITextUpdating tu = (ITextUpdating)refactoring.getAdapter(ITextUpdating.class);

        if(tu != null) {
            forcePreview = tu.getUpdateTextualMatches();
        }

        getRefactoringWizard().setForcePreviewReview(forcePreview);
    }
}
