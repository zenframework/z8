package org.zenframework.z8.pde.refactoring.reorg;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import org.zenframework.z8.pde.refactoring.LanguageElementLabelProvider;
import org.zenframework.z8.pde.refactoring.messages.RefactoringMessages;

public class ReorgQueries implements IReorgQueries {
    private final Wizard fWizard;
    private final Shell fShell;

    public ReorgQueries(Wizard wizard) {
        fWizard = wizard;
        fShell = null;
    }

    public ReorgQueries(Shell shell) {
        fWizard = null;
        fShell = shell;
    }

    private Shell getShell() {
        if(fWizard != null)
            return fWizard.getContainer().getShell();
        else
            return fShell;
    }

    @Override
    public IConfirmQuery createYesYesToAllNoNoToAllQuery(String dialogTitle, boolean allowCancel, int queryID) {
        return new YesYesToAllNoNoToAllQuery(getShell(), allowCancel, dialogTitle);
    }

    @Override
    public IConfirmQuery createYesNoQuery(String dialogTitle, boolean allowCancel, int queryID) {
        return new YesNoQuery(getShell(), allowCancel, dialogTitle);
    }

    @Override
    public IConfirmQuery createSkipQuery(String dialogTitle, int queryID) {
        return new SkipQuery(getShell(), dialogTitle);
    }

    private static class YesYesToAllNoNoToAllQuery implements IConfirmQuery {
        private final boolean fAllowCancel;
        private boolean fYesToAll = false;
        private boolean fNoToAll = false;
        private final Shell fShell;
        private final String fDialogTitle;

        YesYesToAllNoNoToAllQuery(Shell parent, boolean allowCancel, String dialogTitle) {
            fShell = parent;
            fDialogTitle = dialogTitle;
            fAllowCancel = allowCancel;
        }

        @Override
        public boolean confirm(final String question) throws OperationCanceledException {
            if(fYesToAll)
                return true;
            if(fNoToAll)
                return false;
            final int[] result = new int[1];
            fShell.getDisplay().syncExec(createQueryRunnable(question, result));
            return getResult(result);
        }

        @Override
        public boolean confirm(String question, Object[] elements) throws OperationCanceledException {
            if(fYesToAll)
                return true;
            if(fNoToAll)
                return false;
            final int[] result = new int[1];
            fShell.getDisplay().syncExec(createQueryRunnable(question, elements, result));
            return getResult(result);
        }

        private Runnable createQueryRunnable(final String question, final int[] result) {
            return new Runnable() {
                @Override
                public void run() {
                    int[] resultId = getResultIDs();
                    MessageDialog dialog = new MessageDialog(fShell, fDialogTitle, null, question, MessageDialog.QUESTION,
                            getButtonLabels(), 0);
                    dialog.open();
                    if(dialog.getReturnCode() == -1) {
                        result[0] = fAllowCancel ? IDialogConstants.CANCEL_ID : IDialogConstants.NO_ID;
                    }
                    else {
                        result[0] = resultId[dialog.getReturnCode()];
                    }
                }

                private String[] getButtonLabels() {
                    if(YesYesToAllNoNoToAllQuery.this.fAllowCancel)
                        return new String[] { IDialogConstants.YES_LABEL, IDialogConstants.YES_TO_ALL_LABEL,
                                IDialogConstants.NO_LABEL, IDialogConstants.NO_TO_ALL_LABEL, IDialogConstants.CANCEL_LABEL };
                    else
                        return new String[] { IDialogConstants.YES_LABEL, IDialogConstants.YES_TO_ALL_LABEL,
                                IDialogConstants.NO_LABEL, IDialogConstants.NO_TO_ALL_LABEL };
                }

                private int[] getResultIDs() {
                    if(YesYesToAllNoNoToAllQuery.this.fAllowCancel)
                        return new int[] { IDialogConstants.YES_ID, IDialogConstants.YES_TO_ALL_ID, IDialogConstants.NO_ID,
                                IDialogConstants.NO_TO_ALL_ID, IDialogConstants.CANCEL_ID };
                    else
                        return new int[] { IDialogConstants.YES_ID, IDialogConstants.YES_TO_ALL_ID, IDialogConstants.NO_ID,
                                IDialogConstants.NO_TO_ALL_ID };
                }
            };
        }

        private Runnable createQueryRunnable(final String question, final Object[] elements, final int[] result) {
            return new Runnable() {
                @Override
                public void run() {
                    ListDialog dialog = new YesNoListDialog(fShell, true);
                    dialog.setAddCancelButton(false);
                    dialog.setBlockOnOpen(true);
                    dialog.setContentProvider(new ArrayContentProvider());
                    dialog.setLabelProvider(new LanguageElementLabelProvider());
                    dialog.setTitle(fDialogTitle);
                    dialog.setMessage(question);
                    dialog.setInput(elements);
                    dialog.open();
                    result[0] = dialog.getReturnCode();
                }
            };
        }

        private boolean getResult(int[] result) throws OperationCanceledException {
            switch(result[0]) {
            case IDialogConstants.YES_TO_ALL_ID:
                fYesToAll = true;
                return true;
            case IDialogConstants.YES_ID:
                return true;
            case IDialogConstants.CANCEL_ID:
                throw new OperationCanceledException();
            case IDialogConstants.NO_ID:
                return false;
            case IDialogConstants.NO_TO_ALL_ID:
                fNoToAll = true;
                return false;
            default:
                Assert.isTrue(false);
                return false;
            }
        }
    }

    private static class YesNoQuery implements IConfirmQuery {
        private final Shell fShell;
        private final String fDialogTitle;
        private final boolean fAllowCancel;

        YesNoQuery(Shell parent, boolean allowCancel, String dialogTitle) {
            fShell = parent;
            fDialogTitle = dialogTitle;
            fAllowCancel = allowCancel;
        }

        @Override
        public boolean confirm(String question) throws OperationCanceledException {
            final int[] result = new int[1];
            fShell.getDisplay().syncExec(createQueryRunnable(question, result));
            return getResult(result);
        }

        @Override
        public boolean confirm(String question, Object[] elements) throws OperationCanceledException {
            final int[] result = new int[1];
            fShell.getDisplay().syncExec(createQueryRunnable(question, elements, result));
            return getResult(result);
        }

        private Runnable createQueryRunnable(final String question, final int[] result) {
            return new Runnable() {
                @Override
                public void run() {
                    MessageDialog dialog = new MessageDialog(fShell, fDialogTitle, null, question, MessageDialog.QUESTION,
                            getButtonLabels(), 0);
                    dialog.open();
                    switch(dialog.getReturnCode()) {
                    case -1:
                        result[0] = fAllowCancel ? IDialogConstants.CANCEL_ID : IDialogConstants.NO_ID;
                        break;
                    case 0:
                        result[0] = IDialogConstants.YES_ID;
                        break;
                    case 1:
                        result[0] = IDialogConstants.NO_ID;
                        break;
                    case 2:
                        if(fAllowCancel)
                            result[0] = IDialogConstants.CANCEL_ID;
                        else
                            Assert.isTrue(false);
                        break;
                    default:
                        Assert.isTrue(false);
                        break;
                    }
                }

                private String[] getButtonLabels() {
                    if(fAllowCancel)
                        return new String[] { IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL,
                                IDialogConstants.CANCEL_LABEL };
                    else
                        return new String[] { IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL };
                }
            };
        }

        private Runnable createQueryRunnable(final String question, final Object[] elements, final int[] result) {
            return new Runnable() {
                @Override
                public void run() {
                    ListDialog dialog = new YesNoListDialog(fShell, false);
                    dialog.setAddCancelButton(false);
                    dialog.setBlockOnOpen(true);
                    dialog.setContentProvider(new ArrayContentProvider());
                    dialog.setLabelProvider(new LanguageElementLabelProvider());
                    dialog.setTitle(fDialogTitle);
                    dialog.setMessage(question);
                    dialog.setInput(elements);
                    dialog.open();
                    result[0] = dialog.getReturnCode();
                }
            };
        }

        private boolean getResult(int[] result) throws OperationCanceledException {
            switch(result[0]) {
            case IDialogConstants.YES_ID:
                return true;
            case IDialogConstants.CANCEL_ID:
                throw new OperationCanceledException();
            case IDialogConstants.NO_ID:
                return false;
            default:
                Assert.isTrue(false);
                return false;
            }
        }
    }

    private static class SkipQuery implements IConfirmQuery {
        private final Shell fShell;
        private final String fDialogTitle;
        private boolean fSkipAll;

        SkipQuery(Shell parent, String dialogTitle) {
            fShell = parent;
            fDialogTitle = dialogTitle;
            fSkipAll = false;
        }

        @Override
        public boolean confirm(String question) throws OperationCanceledException {
            if(fSkipAll)
                return false;
            final int[] result = new int[1];
            fShell.getDisplay().syncExec(createQueryRunnable(question, result));
            return getResult(result);
        }

        @Override
        public boolean confirm(String question, Object[] elements) throws OperationCanceledException {
            throw new UnsupportedOperationException("Not supported for skip queries");
        }

        private Runnable createQueryRunnable(final String question, final int[] result) {
            return new Runnable() {
                @Override
                public void run() {
                    MessageDialog dialog = new MessageDialog(fShell, fDialogTitle, null, question, MessageDialog.QUESTION,
                            getButtonLabels(), 0);
                    dialog.open();
                    switch(dialog.getReturnCode()) {
                    case -1:
                        result[0] = IDialogConstants.CANCEL_ID;
                        break;
                    default:
                        result[0] = dialog.getReturnCode();
                    }
                }

                private String[] getButtonLabels() {
                    return new String[] { IDialogConstants.SKIP_LABEL, RefactoringMessages.ReorgQueries_skip_all,
                            IDialogConstants.CANCEL_LABEL };
                }
            };
        }

        private boolean getResult(int[] result) throws OperationCanceledException {
            switch(result[0]) {
            case 0:
                return false;
            case 1:
                fSkipAll = true;
                return false;
            case 2:
                throw new OperationCanceledException();
            default:
                return false;
            }
        }
    }

    private static final class YesNoListDialog extends ListDialog {
        private final boolean fYesToAllNoToAll;

        private YesNoListDialog(Shell parent, boolean includeYesToAllNoToAll) {
            super(parent, SWT.TITLE | SWT.BORDER | SWT.RESIZE | SWT.APPLICATION_MODAL);
            fYesToAllNoToAll = includeYesToAllNoToAll;
        }

        @Override
        protected void buttonPressed(int buttonId) {
            super.buttonPressed(buttonId);
            setReturnCode(buttonId);
            close();
        }

        @Override
        protected void createButtonsForButtonBar(Composite parent) {
            createButton(parent, IDialogConstants.YES_ID, IDialogConstants.YES_LABEL, true);
            if(fYesToAllNoToAll)
                createButton(parent, IDialogConstants.YES_TO_ALL_ID, IDialogConstants.YES_TO_ALL_LABEL, false);
            createButton(parent, IDialogConstants.NO_ID, IDialogConstants.NO_LABEL, false);
            if(fYesToAllNoToAll)
                createButton(parent, IDialogConstants.NO_TO_ALL_ID, IDialogConstants.NO_TO_ALL_LABEL, false);
            createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
        }
    }
}
