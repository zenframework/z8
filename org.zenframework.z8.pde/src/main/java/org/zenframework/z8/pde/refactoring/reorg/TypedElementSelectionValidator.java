package org.zenframework.z8.pde.refactoring.reorg;

import java.util.Collection;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.ui.dialogs.ISelectionStatusValidator;

public class TypedElementSelectionValidator implements ISelectionStatusValidator {
    private IStatus m_errorStatus = new StatusInfo(IStatus.ERROR, "");
    private IStatus m_OKStatus = new StatusInfo();
    private Class<? extends Object>[] m_acceptedTypes;
    private boolean m_allowMultipleSelection;
    private Collection<? extends Object> m_rejectedElements;

    public TypedElementSelectionValidator(Class<? extends Object>[] acceptedTypes, boolean allowMultipleSelection) {
        this(acceptedTypes, allowMultipleSelection, null);
    }

    public TypedElementSelectionValidator(Class<? extends Object>[] acceptedTypes, boolean allowMultipleSelection,
            Collection<? extends Object> rejectedElements) {
        m_acceptedTypes = acceptedTypes;
        m_allowMultipleSelection = allowMultipleSelection;
        m_rejectedElements = rejectedElements;
    }

    @Override
    public IStatus validate(Object[] elements) {
        if(isValid(elements)) {
            return m_OKStatus;
        }
        return m_errorStatus;
    }

    private boolean isOfAcceptedType(Object o) {
        for(int i = 0; i < m_acceptedTypes.length; i++) {
            if(m_acceptedTypes[i].isInstance(o)) {
                return true;
            }
        }
        return false;
    }

    private boolean isRejectedElement(Object elem) {
        return (m_rejectedElements != null) && m_rejectedElements.contains(elem);
    }

    protected boolean isSelectedValid(Object elem) {
        return true;
    }

    private boolean isValid(Object[] selection) {
        if(selection.length == 0) {
            return false;
        }
        if(!m_allowMultipleSelection && selection.length != 1) {
            return false;
        }
        for(int i = 0; i < selection.length; i++) {
            Object o = selection[i];
            if(!isOfAcceptedType(o) || isRejectedElement(o) || !isSelectedValid(o)) {
                return false;
            }
        }
        return true;
    }
}
