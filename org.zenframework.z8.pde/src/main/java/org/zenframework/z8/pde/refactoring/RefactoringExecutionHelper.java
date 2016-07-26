package org.zenframework.z8.pde.refactoring;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IThreadListener;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.PerformChangeOperation;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.internal.ui.refactoring.ChangeExceptionHandler;
import org.eclipse.ltk.internal.ui.refactoring.WorkbenchRunnableAdapter;
import org.eclipse.ltk.ui.refactoring.RefactoringUI;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Shell;

import org.zenframework.z8.pde.refactoring.messages.Messages;
import org.zenframework.z8.pde.refactoring.messages.RefactoringMessages;

@SuppressWarnings({ "restriction", "deprecation" })
public class RefactoringExecutionHelper {
    private final Refactoring m_refactoring;
    private final Shell m_parent;
    private final IRunnableContext m_execContext;
    private final int m_stopSeverity;
    private final boolean m_needsSavedEditors;

    private class Operation implements IWorkspaceRunnable {
        public Change m_change;
        public PerformChangeOperation m_performChangeOperation;

        @Override
        public void run(IProgressMonitor pm) throws CoreException {
            try {
                pm.beginTask("", 11);
                pm.subTask("");
                RefactoringStatus status = m_refactoring.checkAllConditions(new SubProgressMonitor(pm, 4,
                        SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK));
                if(status.getSeverity() >= m_stopSeverity) {
                    Dialog dialog = RefactoringUI.createRefactoringStatusDialog(status, m_parent, m_refactoring.getName(),
                            false);
                    if(dialog.open() == IDialogConstants.CANCEL_ID) {
                        throw new OperationCanceledException();
                    }
                }
                m_change = m_refactoring.createChange(new SubProgressMonitor(pm, 2,
                        SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK));
                m_change.initializeValidationData(new SubProgressMonitor(pm, 1,
                        SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK));
                m_performChangeOperation = RefactoringUI.createUIAwareChangeOperation(m_change);
                m_performChangeOperation.setUndoManager(RefactoringCore.getUndoManager(), m_refactoring.getName());
                m_performChangeOperation
                        .run(new SubProgressMonitor(pm, 4, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK));
            }
            finally {
                pm.done();
            }
        }
    }

    public RefactoringExecutionHelper(Refactoring refactoring, int stopSevertity, boolean needsSavedEditors, Shell parent,
            IRunnableContext context) {
        super();
        m_refactoring = refactoring;
        m_stopSeverity = stopSevertity;
        m_parent = parent;
        m_execContext = context;
        m_needsSavedEditors = needsSavedEditors;
    }

    public void perform(boolean cancelable) throws InterruptedException, InvocationTargetException {
        final IJobManager manager = Job.getJobManager();
        final IWorkspaceRoot rule = ResourcesPlugin.getWorkspace().getRoot();

        class OperationRunner extends WorkbenchRunnableAdapter implements IThreadListener {
            public OperationRunner(IWorkspaceRunnable runnable, ISchedulingRule schedulingRule) {
                super(runnable, schedulingRule);
            }

            @Override
            public void threadChange(Thread thread) {
                manager.transferRule(getSchedulingRule(), thread);
            }
        }

        try {
            try {
                Runnable r = new Runnable() {
                    @Override
                    public void run() {
                        manager.beginRule(rule, null);
                    }
                };
                BusyIndicator.showWhile(m_parent.getDisplay(), r);
            }
            catch(OperationCanceledException e) {
                throw new InterruptedException(e.getMessage());
            }
            RefactoringSaveHelper saveHelper = new RefactoringSaveHelper();
            if(m_needsSavedEditors && !saveHelper.saveEditors(m_parent))
                throw new InterruptedException();
            Operation op = new Operation();
            m_refactoring.setValidationContext(m_parent);
            try {
                m_execContext.run(false, cancelable, new OperationRunner(op, rule));
                RefactoringStatus validationStatus = op.m_performChangeOperation.getValidationStatus();
                if(validationStatus != null && validationStatus.hasFatalError()) {
                    MessageDialog.openError(
                            m_parent,
                            m_refactoring.getName(),
                            Messages.format(RefactoringMessages.RefactoringExecutionHelper_cannot_execute,
                                    validationStatus.getMessageMatchingSeverity(RefactoringStatus.FATAL)));
                    return;
                }
            }
            catch(InvocationTargetException e) {
                PerformChangeOperation pco = op.m_performChangeOperation;
                if(pco != null && pco.changeExecutionFailed()) {
                    ChangeExceptionHandler handler = new ChangeExceptionHandler(m_parent, m_refactoring);
                    Throwable inner = e.getTargetException();
                    if(inner instanceof RuntimeException) {
                        handler.handle(pco.getChange(), (RuntimeException)inner);
                    }
                    else if(inner instanceof CoreException) {
                        handler.handle(pco.getChange(), (CoreException)inner);
                    }
                    else {
                        throw e;
                    }
                }
                else {
                    throw e;
                }
            }
            catch(OperationCanceledException e) {
                throw new InterruptedException(e.getMessage());
            }
            finally {
                saveHelper.triggerBuild();
            }
        }
        finally {
            manager.endRule(rule);
            m_refactoring.setValidationContext(null);
        }
    }
}
