package org.zenframework.z8.pde.refactoring.processors.move;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.ChangeDescriptor;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.TextEditBasedChange;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.MoveProcessor;
import org.eclipse.ltk.core.refactoring.participants.RefactoringArguments;
import org.eclipse.ltk.core.refactoring.participants.RefactoringParticipant;
import org.eclipse.ltk.core.refactoring.participants.SharableParticipants;

import org.zenframework.z8.compiler.core.ILanguageElement;
import org.zenframework.z8.compiler.workspace.Resource;
import org.zenframework.z8.pde.refactoring.IScriptableRefactoring;
import org.zenframework.z8.pde.refactoring.Z8RefactoringArguments;
import org.zenframework.z8.pde.refactoring.Resources;
import org.zenframework.z8.pde.refactoring.changes.DynamicValidationStateChange;
import org.zenframework.z8.pde.refactoring.messages.RefactoringMessages;
import org.zenframework.z8.pde.refactoring.processors.Z8Processors;
import org.zenframework.z8.pde.refactoring.processors.ResourceProcessors;
import org.zenframework.z8.pde.refactoring.rename.ICommentProvider;
import org.zenframework.z8.pde.refactoring.reorg.CreateTargetExecutionLog;
import org.zenframework.z8.pde.refactoring.reorg.ICreateTargetQueries;
import org.zenframework.z8.pde.refactoring.reorg.ICreateTargetQuery;
import org.zenframework.z8.pde.refactoring.reorg.IReorgDestinationValidator;
import org.zenframework.z8.pde.refactoring.reorg.IReorgPolicy.IMovePolicy;
import org.zenframework.z8.pde.refactoring.reorg.IReorgQueries;
import org.zenframework.z8.pde.refactoring.reorg.LoggedCreateTargetQueries;
import org.zenframework.z8.pde.refactoring.reorg.MonitoringCreateTargetQueries;
import org.zenframework.z8.pde.refactoring.reorg.NullReorgQueries;
import org.zenframework.z8.pde.refactoring.reorg.ReorgPolicyFactory;

public final class Z8MoveProcessor extends MoveProcessor implements IScriptableRefactoring, ICommentProvider,
        IReorgDestinationValidator {
    public static final String IDENTIFIER = "org.zenframework.z8.refactoring.MoveProcessor";
    private String fComment;
    private ICreateTargetQueries fCreateTargetQueries;
    private IMovePolicy fMovePolicy;
    private IReorgQueries fReorgQueries;
    private boolean fWasCanceled;

    public Z8MoveProcessor(IMovePolicy policy) {
        fMovePolicy = policy;
    }

    @Override
    public boolean canChildrenBeDestinations(ILanguageElement element) {
        return fMovePolicy.canChildrenBeDestinations(element);
    }

    @Override
    public boolean canChildrenBeDestinations(IResource resource) {
        return fMovePolicy.canChildrenBeDestinations(resource);
    }

    @Override
    public boolean canElementBeDestination(ILanguageElement element) {
        return fMovePolicy.canElementBeDestination(element);
    }

    @Override
    public boolean canElementBeDestination(IResource resource) {
        return fMovePolicy.canElementBeDestination(resource);
    }

    @Override
    public boolean canEnableComment() {
        return true;
    }

    public boolean canUpdateReferences() {
        return fMovePolicy.canUpdateReferences();
    }

    @Override
    public RefactoringStatus checkFinalConditions(IProgressMonitor pm, CheckConditionsContext context) throws CoreException {
        try {
            Assert.isNotNull(fReorgQueries);
            fWasCanceled = false;
            return fMovePolicy.checkFinalConditions(pm, context, fReorgQueries);
        }
        catch(OperationCanceledException e) {
            fWasCanceled = true;
            throw e;
        }
    }

    public IResource getResource(ILanguageElement element) {
        if(element instanceof Resource)
            return ((Resource)element).getResource();
        else
            return element.getCompilationUnit().getResource();
    }

    public IResource[] getResources(ILanguageElement[] elements) {
        IResource[] result = new IResource[elements.length];

        for(int i = 0; i < elements.length; i++) {
            result[i] = getResource(elements[i]);
        }
        return result;
    }

    public static IResource[] getNotNulls(IResource[] resources) {
        List<IResource> result = new ArrayList<IResource>(resources.length);

        for(int i = 0; i < resources.length; i++) {
            IResource resource = resources[i];

            if(resource != null && !result.contains(resource))
                result.add(resource);
        }
        return result.toArray(new IResource[result.size()]);
    }

    @Override
    public RefactoringStatus checkInitialConditions(IProgressMonitor pm) throws CoreException {
        pm.beginTask("", 1);

        try {
            RefactoringStatus result = new RefactoringStatus();
            result.merge(RefactoringStatus.create(Resources.checkInSync(getNotNulls(fMovePolicy.getResources()))));
            IResource[] resources = getResources(fMovePolicy.getElements());
            result.merge(RefactoringStatus.create(Resources.checkInSync(getNotNulls(resources))));
            return result;
        }
        finally {
            pm.done();
        }
    }

    @Override
    public Change createChange(IProgressMonitor pm) throws CoreException {
        try {
            final DynamicValidationStateChange result = new DynamicValidationStateChange(
                    RefactoringMessages.MoveProcessor_change_name) {
                @Override
                public ChangeDescriptor getDescriptor() {
                    return fMovePolicy.getDescriptor();
                }

                @Override
                public Change perform(IProgressMonitor pm2) throws CoreException {
                    Change change = super.perform(pm2);
                    Change[] changes = getChildren();
                    for(int index = 0; index < changes.length; index++) {
                        if(!(changes[index] instanceof TextEditBasedChange))
                            return null;
                    }
                    return change;
                }
            };

            Change change = fMovePolicy.createChange(pm);

            if(change instanceof CompositeChange) {
                CompositeChange subComposite = (CompositeChange)change;
                result.merge(subComposite);
            }
            else {
                result.add(change);
            }
            return result;
        }
        finally {
            pm.done();
        }
    }

    private String[] getAffectedProjectNatures() throws CoreException {
        String[] jNatures = Z8Processors.computeAffectedNaturs(fMovePolicy.getElements());
        String[] rNatures = ResourceProcessors.computeAffectedNatures(fMovePolicy.getResources());

        Set<String> result = new HashSet<String>();

        result.addAll(Arrays.asList(jNatures));
        result.addAll(Arrays.asList(rNatures));

        return result.toArray(new String[result.size()]);
    }

    @Override
    public String getComment() {
        return fComment;
    }

    public Object getCommonParentForInputElements() {
        return new ParentChecker(fMovePolicy.getResources(), fMovePolicy.getElements()).getCommonParent();
    }

    public ICreateTargetQuery getCreateTargetQuery() {
        return fMovePolicy.getCreateTargetQuery(fCreateTargetQueries);
    }

    protected Object getDestination() {
        ILanguageElement element = fMovePolicy.getElementDestination();

        if(element != null)
            return element;

        return fMovePolicy.getResourceDestination();
    }

    @Override
    public Object[] getElements() {
        List<Object> result = new ArrayList<Object>();
        result.addAll(Arrays.asList(fMovePolicy.getElements()));
        result.addAll(Arrays.asList(fMovePolicy.getResources()));
        return result.toArray();
    }

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    public ILanguageElement[] getLanguageElements() {
        return fMovePolicy.getElements();
    }

    @Override
    public String getProcessorName() {
        return RefactoringMessages.MoveRefactoring_0;
    }

    public IResource[] getResources() {
        return fMovePolicy.getResources();
    }

    public boolean getUpdateReferences() {
        if(!canUpdateReferences())
            return false;
        return fMovePolicy.getUpdateReferences();
    }

    public boolean hasAllInputSet() {
        return fMovePolicy.hasAllInputSet();
    }

    public boolean hasDestinationSet() {
        return fMovePolicy.getElementDestination() != null || fMovePolicy.getResourceDestination() != null;
    }

    @Override
    public RefactoringStatus initialize(RefactoringArguments arguments) {
        setReorgQueries(new NullReorgQueries());

        final RefactoringStatus status = new RefactoringStatus();

        if(arguments instanceof Z8RefactoringArguments) {
            final Z8RefactoringArguments extended = (Z8RefactoringArguments)arguments;

            fMovePolicy = ReorgPolicyFactory.createMovePolicy(status, arguments);

            if(fMovePolicy != null && !status.hasFatalError()) {
                final CreateTargetExecutionLog log = ReorgPolicyFactory.loadCreateTargetExecutionLog(status, extended);

                if(log != null && !status.hasFatalError()) {
                    fMovePolicy.setDestinationCheck(false);
                    fCreateTargetQueries = new MonitoringCreateTargetQueries(new LoggedCreateTargetQueries(log), log);
                }
                status.merge(fMovePolicy.initialize(arguments));
            }
        }
        else
            return RefactoringStatus
                    .createFatalErrorStatus(RefactoringMessages.InitializableRefactoring_inacceptable_arguments);
        return status;
    }

    @Override
    public boolean isApplicable() throws CoreException {
        return fMovePolicy.canEnable();
    }

    public boolean isTextualMove() {
        return fMovePolicy.isTextualMove();
    }

    @Override
    public RefactoringParticipant[] loadParticipants(RefactoringStatus status, SharableParticipants shared)
            throws CoreException {
        return fMovePolicy.loadParticipants(status, this, getAffectedProjectNatures(), shared);
    }

    @Override
    public Change postCreateChange(Change[] participantChanges, IProgressMonitor pm) throws CoreException {
        return fMovePolicy.postCreateChange(participantChanges, pm);
    }

    @Override
    public void setComment(String comment) {
        fComment = comment;
    }

    public void setCreateTargetQueries(ICreateTargetQueries queries) {
        fCreateTargetQueries = new MonitoringCreateTargetQueries(queries, fMovePolicy.getCreateTargetExecutionLog());
    }

    public RefactoringStatus setDestination(ILanguageElement destination) throws CoreException {
        return fMovePolicy.setDestination(destination);
    }

    public RefactoringStatus setDestination(IResource destination) throws CoreException {
        return fMovePolicy.setDestination(destination);
    }

    public void setReorgQueries(IReorgQueries queries) {
        Assert.isNotNull(queries);
        fReorgQueries = queries;
    }

    public void setUpdateReferences(boolean update) {
        fMovePolicy.setUpdateReferences(update);
    }

    public boolean wasCanceled() {
        return fWasCanceled;
    }
}
