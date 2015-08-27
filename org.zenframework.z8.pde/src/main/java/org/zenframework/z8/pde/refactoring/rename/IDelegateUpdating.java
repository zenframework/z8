package org.zenframework.z8.pde.refactoring.rename;

public interface IDelegateUpdating {
    public boolean canEnableDelegateUpdating();

    public boolean getDelegateUpdating();

    public String getDelegateUpdatingTitle(boolean plural);

    public boolean getDeprecateDelegates();

    public void setDelegateUpdating(boolean updating);

    public void setDeprecateDelegates(boolean deprecate);
}
