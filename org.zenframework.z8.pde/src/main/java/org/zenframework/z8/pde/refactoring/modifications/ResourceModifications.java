package org.zenframework.z8.pde.refactoring.modifications;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.mapping.IResourceChangeDescriptionFactory;
import org.eclipse.core.runtime.IPath;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CopyArguments;
import org.eclipse.ltk.core.refactoring.participants.CopyParticipant;
import org.eclipse.ltk.core.refactoring.participants.CreateArguments;
import org.eclipse.ltk.core.refactoring.participants.CreateParticipant;
import org.eclipse.ltk.core.refactoring.participants.DeleteArguments;
import org.eclipse.ltk.core.refactoring.participants.DeleteParticipant;
import org.eclipse.ltk.core.refactoring.participants.MoveArguments;
import org.eclipse.ltk.core.refactoring.participants.MoveParticipant;
import org.eclipse.ltk.core.refactoring.participants.ParticipantManager;
import org.eclipse.ltk.core.refactoring.participants.RefactoringParticipant;
import org.eclipse.ltk.core.refactoring.participants.RefactoringProcessor;
import org.eclipse.ltk.core.refactoring.participants.RenameArguments;
import org.eclipse.ltk.core.refactoring.participants.RenameParticipant;
import org.eclipse.ltk.core.refactoring.participants.SharableParticipants;

import org.zenframework.z8.compiler.workspace.Folder;

public class ResourceModifications {
    private List<IResource> m_create;
    private List<IResource> m_delete;
    private List<IResource> m_move;
    private List<MoveArguments> m_moveArguments;
    private List<IResource> m_rename;
    private List<RenameArguments> m_renameArguments;
    private List<IResource> m_copy;
    private List<CopyArguments> m_copyArguments;
    private int m_ignoreCount;
    private List<DeltaDescription> m_deltaDescriptions;

    public static abstract class DeltaDescription {
        protected IResource fResource;

        public DeltaDescription(IResource resource) {
            fResource = resource;
        }

        public abstract void buildDelta(IResourceChangeDescriptionFactory builder);

        public abstract IPath getDestinationPath();
    }

    public static class DeleteDescription extends DeltaDescription {
        public DeleteDescription(IResource resource) {
            super(resource);
        }

        @Override
        public void buildDelta(IResourceChangeDescriptionFactory builder) {
            builder.delete(fResource);
        }

        @Override
        public IPath getDestinationPath() {
            return null;
        }
    }

    public static class ChangedDescription extends DeltaDescription {
        public ChangedDescription(IFile resource) {
            super(resource);
        }

        @Override
        public void buildDelta(IResourceChangeDescriptionFactory builder) {
            builder.change((IFile)fResource);
        }

        @Override
        public IPath getDestinationPath() {
            return null;
        }
    }

    public static class CreateDescription extends DeltaDescription {
        public CreateDescription(IResource resource) {
            super(resource);
        }

        @Override
        public void buildDelta(IResourceChangeDescriptionFactory builder) {
            builder.create(fResource);
        }

        @Override
        public IPath getDestinationPath() {
            return fResource.getFullPath();
        }
    }

    public static class MoveDescription extends DeltaDescription {
        private IPath fDestination;

        public MoveDescription(IResource resource, IPath destination) {
            super(resource);
            fDestination = destination;
        }

        @Override
        public void buildDelta(IResourceChangeDescriptionFactory builder) {
            builder.move(fResource, fDestination);
        }

        @Override
        public IPath getDestinationPath() {
            return fDestination;
        }
    }

    public static class CopyDescription extends DeltaDescription {
        private IPath fDestination;

        public CopyDescription(IResource resource, IPath destination) {
            super(resource);
            fDestination = destination;
        }

        @Override
        public void buildDelta(IResourceChangeDescriptionFactory builder) {
            builder.copy(fResource, fDestination);
        }

        @Override
        public IPath getDestinationPath() {
            return fDestination;
        }
    }

    public void addChanged(IFile file) {
        if(m_ignoreCount == 0) {
            internalAdd(new ChangedDescription(file));
        }
    }

    public void addCreate(IResource create) {
        if(m_create == null)
            m_create = new ArrayList<IResource>(2);

        m_create.add(create);

        if(m_ignoreCount == 0) {
            internalAdd(new CreateDescription(create));
        }
    }

    public void addDelete(IResource delete) {
        if(m_delete == null)
            m_delete = new ArrayList<IResource>(2);

        m_delete.add(delete);

        if(m_ignoreCount == 0) {
            internalAdd(new DeleteDescription(delete));
        }
    }

    public void addMove(IResource move, MoveArguments arguments) {
        if(m_move == null) {
            m_move = new ArrayList<IResource>(2);
            m_moveArguments = new ArrayList<MoveArguments>(2);
        }

        m_move.add(move);
        m_moveArguments.add(arguments);

        if(m_ignoreCount == 0) {
            Folder folder = (Folder)arguments.getDestination();
            IPath destination = folder.getResource().getFullPath().append(move.getName());
            internalAdd(new MoveDescription(move, destination));
        }
    }

    public void addCopy(IResource copy, CopyArguments arguments) {
        if(m_copy == null) {
            m_copy = new ArrayList<IResource>(2);
            m_copyArguments = new ArrayList<CopyArguments>(2);
        }

        m_copy.add(copy);
        m_copyArguments.add(arguments);

        addCopyDelta(copy, arguments);
    }

    public void addRename(IResource rename, RenameArguments arguments) {
        if(m_rename == null) {
            m_rename = new ArrayList<IResource>(2);
            m_renameArguments = new ArrayList<RenameArguments>(2);
        }

        m_rename.add(rename);
        m_renameArguments.add(arguments);

        if(m_ignoreCount == 0) {
            IPath newPath = rename.getFullPath().removeLastSegments(1).append(arguments.getNewName());
            internalAdd(new MoveDescription(rename, newPath));
        }
    }

    public RefactoringParticipant[] getParticipants(RefactoringStatus status, RefactoringProcessor processor,
            String[] natures, SharableParticipants shared) {
        List<RefactoringParticipant> result = new ArrayList<RefactoringParticipant>(5);

        if(m_delete != null) {
            DeleteArguments arguments = new DeleteArguments();

            for(Iterator<IResource> iter = m_delete.iterator(); iter.hasNext();) {
                DeleteParticipant[] deletes = ParticipantManager.loadDeleteParticipants(status, processor, iter.next(),
                        arguments, natures, shared);
                result.addAll(Arrays.asList(deletes));
            }
        }

        if(m_create != null) {
            CreateArguments arguments = new CreateArguments();
            for(Iterator<IResource> iter = m_create.iterator(); iter.hasNext();) {
                CreateParticipant[] creates = ParticipantManager.loadCreateParticipants(status, processor, iter.next(),
                        arguments, natures, shared);
                result.addAll(Arrays.asList(creates));
            }
        }

        if(m_move != null) {
            for(int i = 0; i < m_move.size(); i++) {
                Object element = m_move.get(i);
                MoveArguments arguments = (MoveArguments)m_moveArguments.get(i);
                MoveParticipant[] moves = ParticipantManager.loadMoveParticipants(status, processor, element, arguments,
                        natures, shared);
                result.addAll(Arrays.asList(moves));
            }
        }

        if(m_copy != null) {
            for(int i = 0; i < m_copy.size(); i++) {
                Object element = m_copy.get(i);
                CopyArguments arguments = (CopyArguments)m_copyArguments.get(i);
                CopyParticipant[] copies = ParticipantManager.loadCopyParticipants(status, processor, element, arguments,
                        natures, shared);
                result.addAll(Arrays.asList(copies));
            }
        }

        if(m_rename != null) {
            for(int i = 0; i < m_rename.size(); i++) {
                Object resource = m_rename.get(i);
                RenameArguments arguments = (RenameArguments)m_renameArguments.get(i);
                RenameParticipant[] renames = ParticipantManager.loadRenameParticipants(status, processor, resource,
                        arguments, natures, shared);
                result.addAll(Arrays.asList(renames));
            }
        }

        return result.toArray(new RefactoringParticipant[result.size()]);
    }

    public void ignoreForDelta() {
        m_ignoreCount++;
    }

    public void trackForDelta() {
        m_ignoreCount--;
    }

    public void addDelta(DeltaDescription description) {
        if(m_ignoreCount > 0)
            return;
        internalAdd(description);
    }

    public void addCopyDelta(IResource copy, CopyArguments arguments) {
        if(m_ignoreCount == 0) {
            IPath destination = ((IResource)arguments.getDestination()).getFullPath().append(copy.getName());
            internalAdd(new CopyDescription(copy, destination));
        }
    }

    public boolean willExist(IResource resource) {
        if(m_deltaDescriptions == null)
            return false;

        IPath fullPath = resource.getFullPath();

        for(Iterator<DeltaDescription> iter = m_deltaDescriptions.iterator(); iter.hasNext();) {
            DeltaDescription delta = iter.next();
            if(fullPath.equals(delta.getDestinationPath()))
                return true;
        }

        return false;
    }

    public void buildDelta(IResourceChangeDescriptionFactory builder) {
        if(m_deltaDescriptions == null)
            return;

        for(Iterator<DeltaDescription> iter = m_deltaDescriptions.iterator(); iter.hasNext();) {
            iter.next().buildDelta(builder);
        }
    }

    public static void buildMoveDelta(IResourceChangeDescriptionFactory builder, IResource resource, RenameArguments args) {
        IPath newPath = resource.getFullPath().removeLastSegments(1).append(args.getNewName());
        builder.move(resource, newPath);
    }

    public static void buildMoveDelta(IResourceChangeDescriptionFactory builder, IResource resource, MoveArguments args) {
        IPath destination = ((IResource)args.getDestination()).getFullPath().append(resource.getName());
        builder.move(resource, destination);
    }

    public static void buildCopyDelta(IResourceChangeDescriptionFactory builder, IResource resource, CopyArguments args) {
        IPath destination = ((IResource)args.getDestination()).getFullPath().append(resource.getName());
        builder.copy(resource, destination);
    }

    private void internalAdd(DeltaDescription description) {
        if(m_deltaDescriptions == null)
            m_deltaDescriptions = new ArrayList<DeltaDescription>();

        m_deltaDescriptions.add(description);
    }
}
