package org.zenframework.z8.pde.refactoring;

import java.util.List;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class ListContentProvider implements IStructuredContentProvider {
    List<Object> m_contents;

    public ListContentProvider() {}

    @Override
    public Object[] getElements(Object input) {
        if(m_contents != null && m_contents == input)
            return m_contents.toArray();
        return new Object[0];
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        if(newInput instanceof List)
            m_contents = (List)newInput;
        else
            m_contents = null;
    }

    @Override
    public void dispose() {}

    public boolean isDeleted(Object o) {
        return m_contents != null && !m_contents.contains(o);
    }
}
