package org.zenframework.z8.pde.refactoring.changes;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.ContentStamp;
import org.eclipse.ltk.core.refactoring.UndoTextFileChange;
import org.eclipse.text.edits.UndoEdit;

import org.zenframework.z8.compiler.workspace.CompilationUnit;
import org.zenframework.z8.pde.Plugin;
import org.zenframework.z8.pde.refactoring.messages.Messages;
import org.zenframework.z8.pde.refactoring.messages.RefactoringMessages;

public class UndoCompilationUnitChange extends UndoTextFileChange {
    private CompilationUnit m_compilationUnit;

    public UndoCompilationUnitChange(String name, CompilationUnit compilationUnit, UndoEdit undo,
            ContentStamp stampToRestore, int saveMode) throws CoreException {
        super(name, getFile(compilationUnit), undo, stampToRestore, saveMode);
        m_compilationUnit = compilationUnit;
    }

    private static IFile getFile(CompilationUnit compilationUnit) throws CoreException {
        IFile file = (IFile)compilationUnit.getResource();
        if(file == null)
            throw new CoreException(new Status(IStatus.ERROR, Plugin.PLUGIN_ID, IStatus.ERROR, Messages.format(
                    RefactoringMessages.UndoCompilationUnitChange_no_resource, compilationUnit.getName()), null));
        return file;
    }

    @Override
    public Object getModifiedElement() {
        return m_compilationUnit;
    }

    @Override
    protected Change createUndoChange(UndoEdit edit, ContentStamp stampToRestore) throws CoreException {
        return new UndoCompilationUnitChange(getName(), m_compilationUnit, edit, stampToRestore, getSaveMode());
    }
}
