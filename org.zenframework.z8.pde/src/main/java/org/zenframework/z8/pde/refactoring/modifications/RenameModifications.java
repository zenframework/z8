package org.zenframework.z8.pde.refactoring.modifications;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.mapping.IResourceChangeDescriptionFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.MoveArguments;
import org.eclipse.ltk.core.refactoring.participants.ParticipantManager;
import org.eclipse.ltk.core.refactoring.participants.RefactoringArguments;
import org.eclipse.ltk.core.refactoring.participants.RefactoringParticipant;
import org.eclipse.ltk.core.refactoring.participants.RefactoringProcessor;
import org.eclipse.ltk.core.refactoring.participants.RenameArguments;
import org.eclipse.ltk.core.refactoring.participants.SharableParticipants;
import org.eclipse.ltk.core.refactoring.participants.ValidateEditChecker;

import org.zenframework.z8.compiler.core.IType;
import org.zenframework.z8.compiler.workspace.CompilationUnit;
import org.zenframework.z8.compiler.workspace.Folder;
import org.zenframework.z8.compiler.workspace.Project;

public class RenameModifications extends RefactoringModifications {
    private List<Object> m_rename;
    private List<Object> m_renameArguments;

    public RenameModifications() {
        m_rename = new ArrayList<Object>();
        m_renameArguments = new ArrayList<Object>();
    }

    public void rename(IResource resource, RenameArguments args) {
        add(resource, args);
    }

    public void rename(Project project, RenameArguments args) {
        add(project, args);

        IProject iProject = (IProject)project.getResource();

        if(iProject != null) {
            getResourceModifications().addRename(iProject, args);
        }
    }

    public void rename(Folder sourceFolder, RenameArguments arguments) {
        add(sourceFolder, arguments);

        if(sourceFolder.getResource() != null) {
            getResourceModifications().addRename(sourceFolder.getResource(), arguments);
        }
    }

    public void rename(Folder rootFolder, RenameArguments args, boolean renameSubFolders) throws CoreException {
        add(rootFolder, args);

        Folder[] allSubFolders = rootFolder.getFolders();

        if(renameSubFolders) {
            for(Folder folder : allSubFolders) {
                RenameArguments subArgs = new RenameArguments(getNewFolderName(rootFolder, args.getNewName(),
                        folder.getName()), args.getUpdateReferences());
                add(folder, subArgs);
            }
        }

        IContainer container = (IContainer)rootFolder.getResource();
        if(container == null)
            return;

        IContainer target = (IContainer)rootFolder.getFolder().getFolder(args.getNewName()).getResource();

        if((allSubFolders.length == 0 || renameSubFolders) && canMove(container, target)) {
            createIncludingParents(target.getParent());

            if(container.getParent().equals(target.getParent())) {
                getResourceModifications().addRename(container,
                        new RenameArguments(target.getName(), args.getUpdateReferences()));
            }
            else {
                try {
                    getResourceModifications().ignoreForDelta();
                    addAllResourceModifications(rootFolder, args, renameSubFolders, allSubFolders);
                }
                finally {
                    getResourceModifications().trackForDelta();
                }
                getResourceModifications().addDelta(
                        new ResourceModifications.MoveDescription(container, target.getFullPath()));
            }
        }
        else {
            addAllResourceModifications(rootFolder, args, renameSubFolders, allSubFolders);
        }
    }

    public void rename(CompilationUnit unit, RenameArguments args) {
        add(unit, args);

        if(unit.getResource() != null) {
            getResourceModifications().addRename(unit.getResource(),
                    new RenameArguments(args.getNewName(), args.getUpdateReferences()));
        }
    }

    public void rename(IType type, RenameArguments args) {
        add(type, args);
    }

    public void buildDelta(IResourceChangeDescriptionFactory builder) {
        for(int i = 0; i < m_rename.size(); i++) {
            Object element = m_rename.get(i);

            if(element instanceof IResource) {
                ResourceModifications.buildMoveDelta(builder, (IResource)element, (RenameArguments)m_renameArguments.get(i));
            }
        }
        getResourceModifications().buildDelta(builder);
    }

    @Override
    public void buildValidateEdits(ValidateEditChecker checker) {
        for(Iterator<Object> iter = m_rename.iterator(); iter.hasNext();) {
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
        List<RefactoringParticipant> result = new ArrayList<RefactoringParticipant>();

        for(int i = 0; i < m_rename.size(); i++) {
            result.addAll(Arrays.asList(ParticipantManager.loadRenameParticipants(status, owner, m_rename.get(i),
                    (RenameArguments)m_renameArguments.get(i), natures, shared)));
        }

        result.addAll(Arrays.asList(getResourceModifications().getParticipants(status, owner, natures, shared)));

        return result.toArray(new RefactoringParticipant[result.size()]);
    }

    private void add(Object element, RefactoringArguments args) {
        m_rename.add(element);
        m_renameArguments.add(args);
    }

    private void addAllResourceModifications(Folder rootFolder, RenameArguments args, boolean renameSubFolders,
            Folder[] subFolders) throws CoreException {
        addResourceModifications(rootFolder, args, rootFolder, renameSubFolders);

        if(renameSubFolders) {
            for(Folder folder : subFolders) {
                addResourceModifications(rootFolder, args, folder, renameSubFolders);
            }
        }
    }

    private void addResourceModifications(Folder root, RenameArguments args, Folder folder, boolean renameSubFolders)
            throws CoreException {
        IContainer container = (IContainer)folder.getResource();

        if(container == null)
            return;

        IFolder target = computeTargetFolder(root, args, folder);

        createIncludingParents(target);

        MoveArguments arguments = new MoveArguments(target, args.getUpdateReferences());

        IResource[] resourcesToMove = collectResourcesOfInterest(folder);

        Set<IResource> allMembers = new HashSet<IResource>(Arrays.asList(container.members()));

        for(IResource resource : resourcesToMove) {
            getResourceModifications().addMove(resource, arguments);
            allMembers.remove(resource);
        }

        for(Iterator<IResource> iter = allMembers.iterator(); iter.hasNext();) {
            IResource element = iter.next();
            if(element instanceof IFile) {
                getResourceModifications().addDelete(element);
                iter.remove();
            }
        }

        if(renameSubFolders && root.equals(folder) || !renameSubFolders && allMembers.isEmpty()) {
            getResourceModifications().addDelete(container);
        }
    }

    private boolean canMove(IContainer source, IContainer target) {
        return !target.exists() && !source.getFullPath().isPrefixOf(target.getFullPath());
    }

    private IFolder computeTargetFolder(Folder root, RenameArguments args, Folder folder) {
        IPath path = folder.getFolder().getPath();
        path = path.append(getNewFolderName(root, args.getNewName(), folder.getName()).replace('.', IPath.SEPARATOR));
        IFolder target = ResourcesPlugin.getWorkspace().getRoot().getFolder(path);
        return target;
    }

    private String getNewFolderName(Folder rootFolder, String newFolderName, String oldSubFolderName) {
        String oldFolderName = rootFolder.getName();
        return newFolderName + oldSubFolderName.substring(oldFolderName.length());
    }
}
