package org.zenframework.z8.pde.refactoring;

public interface ITextUpdating {
    public boolean canEnableTextUpdating();

    public boolean getUpdateTextualMatches();

    public void setUpdateTextualMatches(boolean update);

    public String getCurrentElementName();

    public String getCurrentElementQualifier();

    public String getNewElementName();
}
