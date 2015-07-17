package org.zenframework.z8.pde.refactoring.rename;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

public interface INameUpdating {
    public void setNewElementName(String newName);

    public String getNewElementName();

    public String getCurrentElementName();

    public Object[] getElements();

    public Object getNewElement() throws CoreException;

    public RefactoringStatus checkNewElementName(String newName) throws CoreException;
}
