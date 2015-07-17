package org.zenframework.z8.pde.refactoring.modifications;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CopyArguments;
import org.eclipse.ltk.core.refactoring.participants.IParticipantDescriptorFilter;
import org.eclipse.ltk.core.refactoring.participants.ParticipantManager;
import org.eclipse.ltk.core.refactoring.participants.RefactoringArguments;
import org.eclipse.ltk.core.refactoring.participants.RefactoringParticipant;
import org.eclipse.ltk.core.refactoring.participants.RefactoringProcessor;
import org.eclipse.ltk.core.refactoring.participants.SharableParticipants;

import org.zenframework.z8.compiler.core.ILanguageElement;
import org.zenframework.z8.compiler.workspace.CompilationUnit;
import org.zenframework.z8.compiler.workspace.Folder;

public class CopyModifications extends RefactoringModifications {
    private List<Object> fCopies;
    private List<RefactoringArguments> fCopyArguments;
    private List<IParticipantDescriptorFilter> fParticipantDescriptorFilter;

    public CopyModifications() {
        fCopies = new ArrayList<Object>();
        fCopyArguments = new ArrayList<RefactoringArguments>();
        fParticipantDescriptorFilter = new ArrayList<IParticipantDescriptorFilter>();
    }

    public void copy(IResource resource, CopyArguments args) {
        add(resource, args, null);
    }

    public void copy(ILanguageElement element, CopyArguments args, CopyArguments resourceArgs) throws CoreException {
        if(element instanceof Folder)
            copy((Folder)element, args, resourceArgs);
        else if(element instanceof CompilationUnit)
            copy((CompilationUnit)element, args, resourceArgs);
        else
            add(element, args, null);
    }

    public void copy(Folder folder, CopyArguments args, CopyArguments resourceArgs) throws CoreException {
        add(folder, args, null);

        Folder destination = (Folder)args.getDestination();

        if(destination.getResource() == null)
            return;

        Folder newFolder = destination.getFolder(folder.getName());

        if(!folder.hasSubfolders() && (!newFolder.getResource().isAccessible() || folder.equals(newFolder))) {
            IContainer resourceDestination = newFolder.getResource().getParent();
            createIncludingParents(resourceDestination);
        }
        else {
            IContainer resourceDestination = (IContainer)newFolder.getResource();
            createIncludingParents(resourceDestination);
            CopyArguments arguments = new CopyArguments(resourceDestination, resourceArgs.getExecutionLog());
            IResource[] resourcesToCopy = collectResourcesOfInterest(folder);
            for(int i = 0; i < resourcesToCopy.length; i++) {
                IResource toCopy = resourcesToCopy[i];
                getResourceModifications().addCopyDelta(toCopy, arguments);
            }
        }
    }

    public void copy(CompilationUnit unit, CopyArguments javaArgs, CopyArguments resourceArgs) throws CoreException {
        add(unit, javaArgs, null);

        if(unit.getResource() != null) {
            getResourceModifications().addCopyDelta(unit.getResource(), resourceArgs);
        }
    }

    @Override
    public RefactoringParticipant[] loadParticipants(RefactoringStatus status, RefactoringProcessor owner, String[] natures,
            SharableParticipants shared) {
        List<RefactoringParticipant> result = new ArrayList<RefactoringParticipant>();

        for(int i = 0; i < fCopies.size(); i++) {
            result.addAll(Arrays.asList(ParticipantManager.loadCopyParticipants(status, owner, fCopies.get(i),
                    (CopyArguments)fCopyArguments.get(i), (IParticipantDescriptorFilter)fParticipantDescriptorFilter.get(i),
                    natures, shared)));
        }

        result.addAll(Arrays.asList(getResourceModifications().getParticipants(status, owner, natures, shared)));
        return result.toArray(new RefactoringParticipant[result.size()]);
    }

    private void add(Object element, RefactoringArguments args, IParticipantDescriptorFilter filter) {
        fCopies.add(element);
        fCopyArguments.add(args);
        fParticipantDescriptorFilter.add(filter);
    }
}
