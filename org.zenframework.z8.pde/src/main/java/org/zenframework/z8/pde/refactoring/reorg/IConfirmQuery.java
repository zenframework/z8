package org.zenframework.z8.pde.refactoring.reorg;

import org.eclipse.core.runtime.OperationCanceledException;

public interface IConfirmQuery {
    public boolean confirm(String question) throws OperationCanceledException;

    public boolean confirm(String question, Object[] elements) throws OperationCanceledException;
}
