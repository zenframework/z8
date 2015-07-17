package org.zenframework.z8.pde.refactoring;

public interface IReferenceUpdating {
    public boolean canEnableUpdateReferences();

    public void setUpdateReferences(boolean update);

    public boolean getUpdateReferences();
}
