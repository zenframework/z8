package org.zenframework.z8.pde.refactoring.changes;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.participants.ReorgExecutionLog;

import org.zenframework.z8.compiler.workspace.CompilationUnit;
import org.zenframework.z8.compiler.workspace.Folder;
import org.zenframework.z8.pde.refactoring.reorg.INewNameQuery;

abstract class CompilationUnitReorgChange extends Z8Change {
    private CompilationUnit m_compilationUnit;
    private Folder m_oldFolder;
    private Folder m_newFolder;
    private INewNameQuery m_newNameQuery;

    CompilationUnitReorgChange(CompilationUnit cu, Folder dest, INewNameQuery newNameQuery) {
        m_compilationUnit = cu;
        m_newFolder = dest;
        m_newNameQuery = newNameQuery;
        m_oldFolder = cu.getFolder();
    }

    CompilationUnitReorgChange(CompilationUnit cu, Folder dest) {
        this(cu, dest, null);
    }

    @Override
    public final Change perform(IProgressMonitor pm) throws CoreException, OperationCanceledException {
        pm.beginTask(getName(), 1);

        try {
            CompilationUnit unit = getCu();
            Change result = doPerformReorg(new SubProgressMonitor(pm, 1));
            markAsExecuted(unit);
            return result;
        }
        finally {
            pm.done();
        }
    }

    abstract Change doPerformReorg(IProgressMonitor pm) throws CoreException, OperationCanceledException;

    @Override
    public Object getModifiedElement() {
        return getCu();
    }

    CompilationUnit getCu() {
        return m_compilationUnit;
    }

    Folder getOldFolder() {
        return m_oldFolder;
    }

    Folder getDestinationFolder() {
        return m_newFolder;
    }

    String getNewName() throws OperationCanceledException {
        if(m_newNameQuery == null)
            return null;
        return m_newNameQuery.getNewName();
    }

    private void markAsExecuted(CompilationUnit unit) {
        ReorgExecutionLog log = (ReorgExecutionLog)getAdapter(ReorgExecutionLog.class);

        if(log != null) {
            log.markAsProcessed(unit);
        }
    }
}
