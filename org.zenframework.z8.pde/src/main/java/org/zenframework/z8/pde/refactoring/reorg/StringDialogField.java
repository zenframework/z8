package org.zenframework.z8.pde.refactoring.reorg;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class StringDialogField extends DialogField {
    private String m_text;
    private Text m_textControl;
    private ModifyListener m_modifyListener;

    public StringDialogField() {
        super();
        m_text = "";
    }

    @Override
    public Control[] doFillIntoGrid(Composite parent, int nColumns) {
        Label label = getLabelControl(parent);
        label.setLayoutData(gridDataForLabel(1));
        Text text = getTextControl(parent);
        text.setLayoutData(gridDataForText(nColumns - 1));
        return new Control[] { label, text };
    }

    @Override
    public int getNumberOfControls() {
        return 2;
    }

    protected static GridData gridDataForText(int span) {
        GridData gd = new GridData();
        gd.horizontalAlignment = GridData.FILL;
        gd.grabExcessHorizontalSpace = false;
        gd.horizontalSpan = span;
        return gd;
    }

    @Override
    public boolean setFocus() {
        if(isOkToUse(m_textControl)) {
            m_textControl.setFocus();
            m_textControl.setSelection(0, m_textControl.getText().length());
        }
        return true;
    }

    public Text getTextControl(Composite parent) {
        if(m_textControl == null) {
            m_modifyListener = new ModifyListener() {
                @Override
                public void modifyText(ModifyEvent e) {
                    doModifyText(e);
                }
            };

            m_textControl = new Text(parent, SWT.SINGLE | SWT.BORDER);

            m_textControl.setText(m_text);
            m_textControl.setFont(parent.getFont());
            m_textControl.addModifyListener(m_modifyListener);
            m_textControl.setEnabled(isEnabled());
        }
        return m_textControl;
    }

    private void doModifyText(ModifyEvent e) {
        if(isOkToUse(m_textControl)) {
            m_text = m_textControl.getText();
        }
        dialogFieldChanged();
    }

    @Override
    protected void updateEnableState() {
        super.updateEnableState();
        if(isOkToUse(m_textControl)) {
            m_textControl.setEnabled(isEnabled());
        }
    }

    public String getText() {
        return m_text;
    }

    public void setText(String text) {
        m_text = text;

        if(isOkToUse(m_textControl)) {
            m_textControl.setText(text);
        }
        else {
            dialogFieldChanged();
        }
    }

    public void setTextWithoutUpdate(String text) {
        m_text = text;

        if(isOkToUse(m_textControl)) {
            m_textControl.removeModifyListener(m_modifyListener);
            m_textControl.setText(text);
            m_textControl.addModifyListener(m_modifyListener);
        }
    }

    @Override
    public void refresh() {
        super.refresh();

        if(isOkToUse(m_textControl)) {
            setTextWithoutUpdate(m_text);
        }
    }
}
