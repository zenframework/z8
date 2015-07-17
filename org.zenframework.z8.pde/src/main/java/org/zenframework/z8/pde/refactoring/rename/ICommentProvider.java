package org.zenframework.z8.pde.refactoring.rename;

public interface ICommentProvider {
    public boolean canEnableComment();

    public String getComment();

    public void setComment(String comment);
}
