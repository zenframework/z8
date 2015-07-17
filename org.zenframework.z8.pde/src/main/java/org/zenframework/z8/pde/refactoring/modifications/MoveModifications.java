package org.zenframework.z8.pde.refactoring.modifications;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.IParticipantDescriptorFilter;
import org.eclipse.ltk.core.refactoring.participants.MoveArguments;
import org.eclipse.ltk.core.refactoring.participants.ParticipantManager;
import org.eclipse.ltk.core.refactoring.participants.RefactoringArguments;
import org.eclipse.ltk.core.refactoring.participants.RefactoringParticipant;
import org.eclipse.ltk.core.refactoring.participants.RefactoringProcessor;
import org.eclipse.ltk.core.refactoring.participants.SharableParticipants;
import org.eclipse.ltk.core.refactoring.participants.ValidateEditChecker;

import org.zenframework.z8.compiler.core.IType;
import org.zenframework.z8.compiler.workspace.CompilationUnit;
import org.zenframework.z8.compiler.workspace.Folder;

public class MoveModifications extends RefactoringModifications {
    private List<Object> fMoves;
    private List<Object> fMoveArguments;
    private List<Object> fParticipantDescriptorFilter;

    public MoveModifications() {
        fMoves = new ArrayList<Object>();
        fMoveArguments = new ArrayList<Object>();
        fParticipantDescriptorFilter = new ArrayList<Object>();
    }

    public void move(IResource resource, MoveArguments args) {
        add(resource, args, null);
    }

    public void move(Folder folder, MoveArguments args) throws CoreException {
        add(folder, args, null);

        if(folder.getResource() == null)
            return;

        Folder destination = (Folder)args.getDestination();

        if(destination.getResource() == null)
            return;

        if(!folder.hasSubfolders() && !destination.getResource().isAccessible()) {
            createIncludingParents((IContainer)destination.getResource());
            getResourceModifications().addMove(folder.getResource(),
                    new MoveArguments(destination, args.getUpdateReferences()));
        }
        else {
            IContainer resourceSource = (IContainer)folder.getResource();

            createIncludingParents((IContainer)destination.getResource());

            MoveArguments arguments = new MoveArguments(destination, args.getUpdateReferences());

            IResource[] resourcesToMove = collectResourcesOfInterest(folder);

            Set<IResource> allMembers = new HashSet<IResource>(Arrays.asList(resourceSource.members()));

            for(int i = 0; i < resourcesToMove.length; i++) {
                IResource toMove = resourcesToMove[i];
                getResourceModifications().addMove(toMove, arguments);
                allMembers.remove(toMove);
            }

            for(Iterator<IResource> iter = allMembers.iterator(); iter.hasNext();) {
                IResource element = iter.next();

                if(element instanceof IFile) {
                    getResourceModifications().addDelete(element);
                    iter.remove();
                }
            }

            if(allMembers.isEmpty()) {
                getResourceModifications().addDelete(resourceSource);
            }
        }
    }

    public void move(CompilationUnit unit, MoveArguments args) throws CoreException {
        add(unit, args, null);

        IType type = unit.getReconciledType();

        if(type != null) {
            add(type, args, null);
        }

        getResourceModifications().addMove(unit.getResource(),
                new MoveArguments(args.getDestination(), args.getUpdateReferences()));
    }

    @Override
    public void buildValidateEdits(ValidateEditChecker checker) {
        for(Iterator<Object> iter = fMoves.iterator(); iter.hasNext();) {
            Object element = iter.next();

            if(element instanceof CompilationUnit) {
                CompilationUnit unit = (CompilationUnit)element;

                IResource resource = unit.getResource();

                if(resource != null && resource.getType() == IResource.FILE) {
                    checker.addFile((IFile)resource);
                }
            }
        }
    }

    @Override
    public RefactoringParticipant[] loadParticipants(RefactoringStatus status, RefactoringProcessor owner, String[] natures,
            SharableParticipants shared) {
        List<Object> result = new ArrayList<Object>();

        for(int i = 0; i < fMoves.size(); i++) {
            result.addAll(Arrays.asList(ParticipantManager.loadMoveParticipants(status, owner, fMoves.get(i),
                    (MoveArguments)fMoveArguments.get(i), (IParticipantDescriptorFilter)fParticipantDescriptorFilter.get(i),
                    natures, shared)));
        }

        result.addAll(Arrays.asList(getResourceModifications().getParticipants(status, owner, natures, shared)));

        return (RefactoringParticipant[])result.toArray(new RefactoringParticipant[result.size()]);
    }

    private void add(Object element, RefactoringArguments args, IParticipantDescriptorFilter filter) {
        fMoves.add(element);
        fMoveArguments.add(args);
        fParticipantDescriptorFilter.add(filter);
    }
}
