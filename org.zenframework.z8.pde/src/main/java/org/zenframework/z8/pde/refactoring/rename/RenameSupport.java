package org.zenframework.z8.pde.refactoring.rename;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.RenameRefactoring;
import org.eclipse.swt.widgets.Shell;

import org.zenframework.z8.compiler.core.IType;
import org.zenframework.z8.compiler.workspace.CompilationUnit;
import org.zenframework.z8.compiler.workspace.Folder;
import org.zenframework.z8.pde.Plugin;
import org.zenframework.z8.pde.refactoring.IReferenceUpdating;
import org.zenframework.z8.pde.refactoring.ITextUpdating;
import org.zenframework.z8.pde.refactoring.RefactoringExecutionHelper;
import org.zenframework.z8.pde.refactoring.UserInterfaceStarter;
import org.zenframework.z8.pde.refactoring.messages.RefactoringMessages;
import org.zenframework.z8.pde.refactoring.processors.RenameFolderProcessor;
import org.zenframework.z8.pde.refactoring.processors.rename.Z8RenameProcessor;
import org.zenframework.z8.pde.refactoring.processors.rename.RenameCompilationUnitProcessor;
import org.zenframework.z8.pde.refactoring.processors.rename.RenameTypeProcessor;

public class RenameSupport {
    private RenameRefactoring m_refactoring;
    private RefactoringStatus m_preCheckStatus;

    public IStatus preCheck() throws CoreException {
        ensureChecked();

        if(m_preCheckStatus.hasFatalError()) {
            return m_preCheckStatus.getEntryMatchingSeverity(RefactoringStatus.FATAL).toStatus();
        }
        else {
            return new Status(IStatus.OK, Plugin.PLUGIN_ID, 0, "", null);
        }
    }

    public void openDialog(Shell parent) throws CoreException {
        ensureChecked();

        if(m_preCheckStatus.hasFatalError()) {
            showInformation(parent, m_preCheckStatus);
            return;
        }

        UserInterfaceStarter starter = RenameUserInterfaceManager.getDefault().getStarter(m_refactoring);
        starter.activate(m_refactoring, parent, getRenameProcessor().needsSavedEditors());
    }

    public void perform(Shell parent, IRunnableContext context) throws InterruptedException, InvocationTargetException {
        try {
            ensureChecked();

            if(m_preCheckStatus.hasFatalError()) {
                showInformation(parent, m_preCheckStatus);
                return;
            }
        }
        catch(CoreException e) {
            throw new InvocationTargetException(e);
        }

        RefactoringExecutionHelper helper = new RefactoringExecutionHelper(m_refactoring,
                RefactoringCore.getConditionCheckingFailedSeverity(), getRenameProcessor().needsSavedEditors(), parent,
                context);
        helper.perform(false);
    }

    public static final int NONE = 0;
    public static final int UPDATE_REFERENCES = 1 << 0;
    public static final int UPDATE_TEXTUAL_MATCHES = 1 << 6;

    private RenameSupport(Z8RenameProcessor processor, String newName, int flags) throws CoreException {
        m_refactoring = new Z8RenameRefactoring(processor);
        initialize(m_refactoring, newName, flags);
    }

    private Z8RenameProcessor getRenameProcessor() {
        return (Z8RenameProcessor)m_refactoring.getProcessor();
    }

    public static RenameSupport create(Folder folder, String newName, int flags) throws CoreException {
        Z8RenameProcessor processor = new RenameFolderProcessor(folder);
        return new RenameSupport(processor, newName, flags);
    }

    public static RenameSupport create(CompilationUnit unit, String newName, int flags) throws CoreException {
        Z8RenameProcessor processor = new RenameCompilationUnitProcessor(unit);
        return new RenameSupport(processor, newName, flags);
    }

    public static RenameSupport create(IType type, String newName, int flags) throws CoreException {
        Z8RenameProcessor processor = new RenameTypeProcessor(type.getCompilationUnit());
        return new RenameSupport(processor, newName, flags);
    }

    private static void initialize(RenameRefactoring refactoring, String newName, int flags) {
        if(refactoring.getProcessor() == null)
            return;
        setNewName((INameUpdating)refactoring.getAdapter(INameUpdating.class), newName);
        IReferenceUpdating reference = (IReferenceUpdating)refactoring.getAdapter(IReferenceUpdating.class);
        if(reference != null) {
            reference.setUpdateReferences(updateReferences(flags));
        }
        ITextUpdating text = (ITextUpdating)refactoring.getAdapter(ITextUpdating.class);
        if(text != null) {
            text.setUpdateTextualMatches(updateTextualMatches(flags));
        }
    }

    private static void setNewName(INameUpdating refactoring, String newName) {
        if(newName != null)
            refactoring.setNewElementName(newName);
    }

    private static boolean updateReferences(int flags) {
        return (flags & UPDATE_REFERENCES) != 0;
    }

    private static boolean updateTextualMatches(int flags) {
        int TEXT_UPDATES = UPDATE_TEXTUAL_MATCHES;
        return (flags & TEXT_UPDATES) != 0;
    }

    private void ensureChecked() throws CoreException {
        if(m_preCheckStatus == null) {
            if(!m_refactoring.isApplicable()) {
                m_preCheckStatus = RefactoringStatus.createFatalErrorStatus(RefactoringMessages.RenameSupport_not_available);
            }
            else {
                m_preCheckStatus = new RefactoringStatus();
            }
        }
    }

    private void showInformation(Shell parent, RefactoringStatus status) {
        String message = status.getMessageMatchingSeverity(RefactoringStatus.FATAL);
        MessageDialog.openInformation(parent, RefactoringMessages.RenameSupport_dialog_title, message);
    }
}
