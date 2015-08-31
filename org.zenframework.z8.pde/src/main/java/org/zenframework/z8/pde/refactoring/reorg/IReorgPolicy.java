package org.zenframework.z8.pde.refactoring.reorg;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.ChangeDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.RefactoringParticipant;
import org.eclipse.ltk.core.refactoring.participants.RefactoringProcessor;
import org.eclipse.ltk.core.refactoring.participants.ReorgExecutionLog;
import org.eclipse.ltk.core.refactoring.participants.SharableParticipants;

import org.zenframework.z8.compiler.core.ILanguageElement;
import org.zenframework.z8.pde.refactoring.IReferenceUpdating;
import org.zenframework.z8.pde.refactoring.IScriptableRefactoring;

public interface IReorgPolicy extends IReferenceUpdating, IScriptableRefactoring {
    public ChangeDescriptor getDescriptor();

    public RefactoringStatus checkFinalConditions(IProgressMonitor monitor, CheckConditionsContext context,
            IReorgQueries queries) throws CoreException;

    public RefactoringStatus setDestination(IResource resource) throws CoreException;

    public RefactoringStatus setDestination(ILanguageElement eement) throws CoreException;

    public boolean canEnable() throws CoreException;

    public boolean canChildrenBeDestinations(IResource resource);

    public boolean canChildrenBeDestinations(ILanguageElement javaElement);

    public boolean canElementBeDestination(IResource resource);

    public boolean canElementBeDestination(ILanguageElement javaElement);

    public IResource[] getResources();

    public ILanguageElement[] getElements();

    public IResource getResourceDestination();

    public ILanguageElement getElementDestination();

    public boolean hasAllInputSet();

    public boolean canUpdateReferences();

    public boolean canUpdateQualifiedNames();

    public RefactoringParticipant[] loadParticipants(RefactoringStatus status, RefactoringProcessor processor,
            String[] natures, SharableParticipants shared) throws CoreException;

    public String getPolicyId();

    public static interface ICopyPolicy extends IReorgPolicy {
        public Change createChange(IProgressMonitor monitor, INewNameQueries queries) throws CoreException;

        public ReorgExecutionLog getReorgExecutionLog();
    }

    public static interface IMovePolicy extends IReorgPolicy {
        public Change createChange(IProgressMonitor monitor) throws CoreException;

        public Change postCreateChange(Change[] participantChanges, IProgressMonitor monitor) throws CoreException;

        public ICreateTargetQuery getCreateTargetQuery(ICreateTargetQueries createQueries);

        public boolean isTextualMove();

        public CreateTargetExecutionLog getCreateTargetExecutionLog();

        public void setDestinationCheck(boolean check);
    }
}
