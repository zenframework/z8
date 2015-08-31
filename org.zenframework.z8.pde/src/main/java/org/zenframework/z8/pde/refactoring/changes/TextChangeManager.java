package org.zenframework.z8.pde.refactoring.changes;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.ltk.core.refactoring.TextChange;

import org.zenframework.z8.compiler.workspace.CompilationUnit;

public class TextChangeManager {
    private Map<CompilationUnit, TextChange> m_map = new HashMap<CompilationUnit, TextChange>(10);

    private final boolean m_keepExecutedTextEdits;

    public TextChangeManager() {
        this(false);
    }

    public TextChangeManager(boolean keepExecutedTextEdits) {
        m_keepExecutedTextEdits = keepExecutedTextEdits;
    }

    public void manage(CompilationUnit cu, TextChange change) {
        m_map.put(cu, change);
    }

    public TextChange get(CompilationUnit cu) {
        TextChange result = m_map.get(cu);

        if(result == null) {
            result = new CompilationUnitChange(cu.getName(), cu);
            result.setKeepPreviewEdits(m_keepExecutedTextEdits);
            m_map.put(cu, result);
        }
        return result;
    }

    public TextChange remove(CompilationUnit unit) {
        return (TextChange)m_map.remove(unit);
    }

    public TextChange[] getAllChanges() {
        return m_map.values().toArray(new TextChange[m_map.values().size()]);
    }

    public CompilationUnit[] getAllCompilationUnits() {
        return m_map.keySet().toArray(new CompilationUnit[m_map.keySet().size()]);
    }

    public IFile[] getFiles() {
        CompilationUnit[] compilationUnits = getAllCompilationUnits();

        IFile[] files = new IFile[compilationUnits.length];

        for(int i = 0; i < compilationUnits.length; i++) {
            files[i] = (IFile)compilationUnits[i].getResource();
        }

        return files;
    }

    public void clear() {
        m_map.clear();
    }

    public boolean containsChangesIn(CompilationUnit cu) {
        return m_map.containsKey(cu);
    }
}
