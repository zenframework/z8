package org.zenframework.z8.pde.refactoring.changes;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.ContentStamp;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.text.edits.UndoEdit;

import org.zenframework.z8.compiler.workspace.CompilationUnit;
import org.zenframework.z8.pde.Plugin;

public class CompilationUnitChange extends TextFileChange {
    private CompilationUnit m_compilationUnit;

    public CompilationUnitChange(String name, CompilationUnit compilationUnit) {
        super(name, (IFile)compilationUnit.getResource());
        m_compilationUnit = compilationUnit;
        setTextType("bl");
    }

    @Override
    public Object getModifiedElement() {
        return m_compilationUnit;
    }

    public CompilationUnit getCompilationUnit() {
        return m_compilationUnit;
    }

    @Override
    protected IDocument acquireDocument(IProgressMonitor pm) throws CoreException {
        pm.beginTask("", 1);
        return super.acquireDocument(new SubProgressMonitor(pm, 1));
    }

    @Override
    protected void releaseDocument(IDocument document, IProgressMonitor pm) throws CoreException {
        super.releaseDocument(document, pm);
    }

    @Override
    protected Change createUndoChange(UndoEdit edit, ContentStamp stampToRestore) {
        try {
            return new UndoCompilationUnitChange(getName(), m_compilationUnit, edit, stampToRestore, getSaveMode());
        }
        catch(CoreException e) {
            Plugin.log(e);
            return null;
        }
    }

    @Override
    @SuppressWarnings("rawtypes")
    public Object getAdapter(Class adapter) {
        if(CompilationUnit.class.equals(adapter)) {
            return m_compilationUnit;
        }
        return super.getAdapter(adapter);
    }
}
