package org.zenframework.z8.pde.refactoring.reorg;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

public class DialogField {
    private Label fLabel;
    protected String fLabelText;
    private IDialogFieldListener fDialogFieldListener;
    private boolean fEnabled;

    public DialogField() {
        fEnabled = true;
        fLabel = null;
        fLabelText = "";
    }

    public void setLabelText(String labeltext) {
        fLabelText = labeltext;
        if(isOkToUse(fLabel)) {
            fLabel.setText(labeltext);
        }
    }

    public final void setDialogFieldListener(IDialogFieldListener listener) {
        fDialogFieldListener = listener;
    }

    public void dialogFieldChanged() {
        if(fDialogFieldListener != null) {
            fDialogFieldListener.dialogFieldChanged(this);
        }
    }

    public boolean setFocus() {
        return false;
    }

    public void postSetFocusOnDialogField(Display display) {
        if(display != null) {
            display.asyncExec(new Runnable() {
                @Override
                public void run() {
                    setFocus();
                }
            });
        }
    }

    public Control[] doFillIntoGrid(Composite parent, int nColumns) {
        Label label = getLabelControl(parent);
        label.setLayoutData(gridDataForLabel(nColumns));
        return new Control[] { label };
    }

    public int getNumberOfControls() {
        return 1;
    }

    protected static GridData gridDataForLabel(int span) {
        GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        gd.horizontalSpan = span;
        return gd;
    }

    public Label getLabelControl(Composite parent) {
        if(fLabel == null) {
            fLabel = new Label(parent, SWT.LEFT | SWT.WRAP);
            fLabel.setFont(parent.getFont());
            fLabel.setEnabled(fEnabled);
            if(fLabelText != null && !"".equals(fLabelText)) {
                fLabel.setText(fLabelText);
            }
            else {
                fLabel.setText(".");
                fLabel.setVisible(false);
            }
        }
        return fLabel;
    }

    public static Control createEmptySpace(Composite parent) {
        return createEmptySpace(parent, 1);
    }

    public static Control createEmptySpace(Composite parent, int span) {
        Label label = new Label(parent, SWT.LEFT);
        GridData gd = new GridData();
        gd.horizontalAlignment = GridData.BEGINNING;
        gd.grabExcessHorizontalSpace = false;
        gd.horizontalSpan = span;
        gd.horizontalIndent = 0;
        gd.widthHint = 0;
        gd.heightHint = 0;
        label.setLayoutData(gd);
        return label;
    }

    protected final boolean isOkToUse(Control control) {
        return (control != null) && (Display.getCurrent() != null) && !control.isDisposed();
    }

    public final void setEnabled(boolean enabled) {
        if(enabled != fEnabled) {
            fEnabled = enabled;
            updateEnableState();
        }
    }

    protected void updateEnableState() {
        if(fLabel != null) {
            fLabel.setEnabled(fEnabled);
        }
    }

    public void refresh() {
        updateEnableState();
    }

    public final boolean isEnabled() {
        return fEnabled;
    }
}
