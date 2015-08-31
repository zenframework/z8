package org.zenframework.z8.pde.refactoring.reorg;

import org.eclipse.core.runtime.OperationCanceledException;

public final class NullReorgQueries implements IReorgQueries {
    private static final class NullConfirmQuery implements IConfirmQuery {
        @Override
        public boolean confirm(String question) throws OperationCanceledException {
            return true;
        }

        @Override
        public boolean confirm(String question, Object[] elements) throws OperationCanceledException {
            return true;
        }
    }

    private static final IConfirmQuery NULL_QUERY = new NullConfirmQuery();

    @Override
    public IConfirmQuery createSkipQuery(String queryTitle, int queryID) {
        return NULL_QUERY;
    }

    @Override
    public IConfirmQuery createYesNoQuery(String queryTitle, boolean allowCancel, int queryID) {
        return NULL_QUERY;
    }

    @Override
    public IConfirmQuery createYesYesToAllNoNoToAllQuery(String queryTitle, boolean allowCancel, int queryID) {
        return NULL_QUERY;
    }
}
