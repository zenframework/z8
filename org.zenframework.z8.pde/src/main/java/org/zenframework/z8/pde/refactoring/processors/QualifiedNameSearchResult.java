package org.zenframework.z8.pde.refactoring.processors;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.TextChange;
import org.eclipse.ltk.core.refactoring.TextFileChange;

import org.zenframework.z8.pde.refactoring.messages.RefactoringMessages;

public class QualifiedNameSearchResult {
    private Map<IFile, TextChange> m_changes;

    public QualifiedNameSearchResult() {
        m_changes = new HashMap<IFile, TextChange>();
    }

    public TextChange getChange(IFile file) {
        TextChange result = m_changes.get(file);
        if(result == null) {
            result = new TextFileChange(file.getName(), file);
            m_changes.put(file, result);
        }
        return result;
    }

    public TextChange[] getAllChanges() {
        Collection<TextChange> values = m_changes.values();
        return values.toArray(new TextChange[values.size()]);
    }

    public IFile[] getAllFiles() {
        Set<IFile> keys = m_changes.keySet();
        return keys.toArray(new IFile[keys.size()]);
    }

    public Change getSingleChange(IFile[] alreadyTouchedFiles) {
        Collection<TextChange> values = m_changes.values();

        if(values.size() == 0)
            return null;

        CompositeChange result = new CompositeChange(RefactoringMessages.QualifiedNameSearchResult_change_name);
        result.markAsSynthetic();

        List<IFile> files = Arrays.asList(alreadyTouchedFiles);

        for(Iterator<TextChange> iter = values.iterator(); iter.hasNext();) {
            TextFileChange change = (TextFileChange)iter.next();

            if(!files.contains(change.getFile())) {
                result.add(change);
            }
        }
        return result;
    }
}
