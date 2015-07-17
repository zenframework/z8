package org.zenframework.z8.pde.refactoring.reorg;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

public class TypedViewerFilter extends ViewerFilter {
    private Class<? extends Object>[] m_acceptedTypes;
    private Object[] m_rejectedElements;

    public TypedViewerFilter(Class<? extends Object>[] acceptedTypes) {
        this(acceptedTypes, null);
    }

    public TypedViewerFilter(Class<? extends Object>[] acceptedTypes, Object[] rejectedElements) {
        m_acceptedTypes = acceptedTypes;
        m_rejectedElements = rejectedElements;
    }

    @Override
    public boolean select(Viewer viewer, Object parentElement, Object element) {
        if(m_rejectedElements != null) {
            for(int i = 0; i < m_rejectedElements.length; i++) {
                if(element.equals(m_rejectedElements[i])) {
                    return false;
                }
            }
        }
        for(int i = 0; i < m_acceptedTypes.length; i++) {
            if(m_acceptedTypes[i].isInstance(element)) {
                return true;
            }
        }
        return false;
    }
}
