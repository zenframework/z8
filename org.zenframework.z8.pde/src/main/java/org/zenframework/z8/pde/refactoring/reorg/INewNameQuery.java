package org.zenframework.z8.pde.refactoring.reorg;

import org.eclipse.core.runtime.OperationCanceledException;

public interface INewNameQuery {
    public String getNewName() throws OperationCanceledException;
}
