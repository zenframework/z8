package org.zenframework.z8.pde.refactoring.reorg;

import org.eclipse.ltk.internal.ui.refactoring.util.SWTUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

@SuppressWarnings("restriction")
public class StringButtonDialogField extends StringDialogField {
    private Button fBrowseButton;
    private String fBrowseButtonLabel;
    private IStringButtonAdapter fStringButtonAdapter;
    private boolean fButtonEnabled;

    public StringButtonDialogField(IStringButtonAdapter adapter) {
        super();
        fStringButtonAdapter = adapter;
        fBrowseButtonLabel = "!Browse...!";
        fButtonEnabled = true;
    }

    public void setButtonLabel(String label) {
        fBrowseButtonLabel = label;
    }

    public void changeControlPressed() {
        fStringButtonAdapter.changeControlPressed(this);
    }

    @Override
    public Control[] doFillIntoGrid(Composite parent, int nColumns) {
        Label label = getLabelControl(parent);
        label.setLayoutData(gridDataForLabel(1));
        Text text = getTextControl(parent);
        text.setLayoutData(gridDataForText(nColumns - 2));
        Button button = getChangeControl(parent);
        button.setLayoutData(gridDataForButton(button, 1));
        return new Control[] { label, text, button };
    }

    @Override
    public int getNumberOfControls() {
        return 3;
    }

    protected static GridData gridDataForButton(Button button, int span) {
        GridData gd = new GridData();
        gd.horizontalAlignment = GridData.FILL;
        gd.grabExcessHorizontalSpace = false;
        gd.horizontalSpan = span;
        gd.widthHint = SWTUtil.getButtonWidthHint(button);
        return gd;
    }

    public Button getChangeControl(Composite parent) {
        if(fBrowseButton == null) {
            fBrowseButton = new Button(parent, SWT.PUSH);
            fBrowseButton.setFont(parent.getFont());
            fBrowseButton.setText(fBrowseButtonLabel);
            fBrowseButton.setEnabled(isEnabled() && fButtonEnabled);
            fBrowseButton.addSelectionListener(new SelectionListener() {
                @Override
                public void widgetDefaultSelected(SelectionEvent e) {
                    changeControlPressed();
                }

                @Override
                public void widgetSelected(SelectionEvent e) {
                    changeControlPressed();
                }
            });
        }
        return fBrowseButton;
    }

    public void enableButton(boolean enable) {
        if(isOkToUse(fBrowseButton)) {
            fBrowseButton.setEnabled(isEnabled() && enable);
        }
        fButtonEnabled = enable;
    }

    @Override
    protected void updateEnableState() {
        super.updateEnableState();

        if(isOkToUse(fBrowseButton)) {
            fBrowseButton.setEnabled(isEnabled() && fButtonEnabled);
        }
    }
}
