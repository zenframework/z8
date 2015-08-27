package org.zenframework.z8.pde.refactoring.reorg;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.DialogPage;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.wizard.WizardPage;

public abstract class NewElementWizardPage extends WizardPage {
    private IStatus fCurrStatus;
    private boolean fPageVisible;

    public NewElementWizardPage(String name) {
        super(name);
        fPageVisible = false;
        fCurrStatus = new StatusInfo();
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        fPageVisible = visible;
        // policy: wizards are not allowed to come up with an error message
        if(visible && fCurrStatus.matches(IStatus.ERROR)) {
            StatusInfo status = new StatusInfo();
            status.setError("");
            fCurrStatus = status;
        }
        updateStatus(fCurrStatus);
    }

    protected void updateStatus(IStatus status) {
        fCurrStatus = status;
        setPageComplete(!status.matches(IStatus.ERROR));
        if(fPageVisible) {
            applyToStatusLine(this, status);
        }
    }

    public static void applyToStatusLine(DialogPage page, IStatus status) {
        String message = status.getMessage();
        switch(status.getSeverity()) {
        case IStatus.OK:
            page.setMessage(message, IMessageProvider.NONE);
            page.setErrorMessage(null);
            break;
        case IStatus.WARNING:
            page.setMessage(message, IMessageProvider.WARNING);
            page.setErrorMessage(null);
            break;
        case IStatus.INFO:
            page.setMessage(message, IMessageProvider.INFORMATION);
            page.setErrorMessage(null);
            break;
        default:
            if(message.length() == 0) {
                message = null;
            }
            page.setMessage(null);
            page.setErrorMessage(message);
            break;
        }
    }

    public static IStatus getMostSevere(IStatus[] status) {
        IStatus max = null;
        for(int i = 0; i < status.length; i++) {
            IStatus curr = status[i];
            if(curr.matches(IStatus.ERROR)) {
                return curr;
            }
            if(max == null || curr.getSeverity() > max.getSeverity()) {
                max = curr;
            }
        }
        return max;
    }

    protected void updateStatus(IStatus[] status) {
        updateStatus(getMostSevere(status));
    }
}
