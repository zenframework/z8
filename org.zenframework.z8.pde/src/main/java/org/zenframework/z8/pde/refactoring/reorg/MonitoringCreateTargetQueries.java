package org.zenframework.z8.pde.refactoring.reorg;

public final class MonitoringCreateTargetQueries implements ICreateTargetQueries {
    private final ICreateTargetQueries fDelegate;
    private final CreateTargetExecutionLog fLog;

    public MonitoringCreateTargetQueries(ICreateTargetQueries delegate, CreateTargetExecutionLog log) {
        fDelegate = delegate;
        fLog = log;
    }

    @Override
    public ICreateTargetQuery createNewFolderQuery() {
        return new ICreateTargetQuery() {
            @Override
            public Object getCreatedTarget(Object selection) {
                final Object target = fDelegate.createNewFolderQuery().getCreatedTarget(selection);
                fLog.markAsCreated(selection, target);
                return target;
            }

            @Override
            public String getNewButtonLabel() {
                return fDelegate.createNewFolderQuery().getNewButtonLabel();
            }
        };
    }

    public CreateTargetExecutionLog getCreateTargetExecutionLog() {
        return fLog;
    }

    public ICreateTargetQueries getDelegate() {
        return fDelegate;
    }
}
