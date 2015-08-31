package org.zenframework.z8.pde.refactoring.operations;

import java.util.ArrayList;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourceAttributes;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.TextEdit;

import org.zenframework.z8.compiler.core.ILanguageElement;
import org.zenframework.z8.compiler.core.IType;
import org.zenframework.z8.compiler.workspace.CompilationUnit;
import org.zenframework.z8.compiler.workspace.Folder;
import org.zenframework.z8.compiler.workspace.Resource;
import org.zenframework.z8.pde.Plugin;
import org.zenframework.z8.pde.refactoring.Z8Status;
import org.zenframework.z8.pde.refactoring.Z8StatusConstants;
import org.zenframework.z8.pde.refactoring.messages.Messages;
import org.zenframework.z8.pde.refactoring.messages.RefactoringMessages;

public class CopyResourceElementsOperation extends MultiOperation {
    protected ArrayList<ILanguageElement> createdElements;

    public CopyResourceElementsOperation(ILanguageElement[] resourcesToCopy, ILanguageElement destContainer, boolean force) {
        this(resourcesToCopy, new ILanguageElement[] { destContainer }, force);
    }

    public CopyResourceElementsOperation(ILanguageElement[] resourcesToCopy, ILanguageElement[] destContainers, boolean force) {
        super(resourcesToCopy, destContainers, force);
    }

    @Override
    protected String getMainTaskName() {
        return RefactoringMessages.operation_copyResourceProgress;
    }

    @Override
    protected void verify(ILanguageElement element) throws CoreException {
        IResource resource = element instanceof Resource ? ((Resource)element).getResource() : null;

        if(resource == null || !resource.exists()) {
            error(Z8StatusConstants.ELEMENT_DOES_NOT_EXIST, element);
        }

        if(resource.getResourceAttributes().isReadOnly() && (isRename() || isMove())) {
            error(Z8StatusConstants.READ_ONLY, element);
        }

        if(resource instanceof IFolder) {
            if(resource.isLinked()) {
                error(Z8StatusConstants.INVALID_RESOURCE, element);
            }
        }

        if(element instanceof CompilationUnit) {
            CompilationUnit compilationUnit = (CompilationUnit)element;
            IType type = compilationUnit.getReconciledType();

            if(isMove() && type != null && type.isNative())
                error(Z8StatusConstants.INVALID_ELEMENT_TYPES, element);
        }
        else if(!(element instanceof Folder)) {
            error(Z8StatusConstants.INVALID_ELEMENT_TYPES, element);
        }

        ILanguageElement destination = null;

        if(element instanceof Resource) {
            destination = getDestinationParent(element);
        }

        verifyDestination(element, destination);

        if(m_renamings != null) {
            verifyRenaming(element);
        }
    }

    @Override
    protected void processElements() throws CoreException {
        createdElements = new ArrayList<ILanguageElement>(m_elementsToProcess.length);

        try {
            super.processElements();
        }
        finally {
            m_resultElements = new ILanguageElement[createdElements.size()];
            createdElements.toArray(m_resultElements);
        }
    }

    private IResource[] collectResourcesOfInterest(Folder source) throws CoreException {
        Resource[] children = source.getMembers();

        IResource[] resources = new IResource[children.length];

        for(int i = 0; i < children.length; i++) {
            resources[i] = children[i].getResource();
        }

        return resources;
    }

    @Override
    protected void processElement(ILanguageElement element) throws CoreException {
        ILanguageElement dest = getDestinationParent(element);

        if(element instanceof CompilationUnit) {
            CompilationUnit compilationUnit = (CompilationUnit)element;
            Folder folder = (Folder)dest;

            processCompilationUnitResource(compilationUnit, folder);
            createdElements.add(((Folder)dest).getCompilationUnit(compilationUnit.getName()));
        }
        else if(element instanceof Folder) {
            processFolderResource((Folder)element, (Folder)dest, getNewNameFor(element));
        }
        else {
            throw new CoreException(new Status(IStatus.ERROR, Plugin.PLUGIN_ID, 0, "Invalid element type", null));
        }
    }

    private TextEdit updateContent(CompilationUnit cu, Folder dest, String newName) throws CoreException {
        if(dest == cu.getFolder() && newName == null) {
            return null; // nothing to change
        }
        else {
            MultiTextEdit edit = new MultiTextEdit();

            int index = newName.lastIndexOf('.');

            if(index != -1) {
                newName = newName.substring(0, index);
            }

            //			cu.rename(edit, cu.getType(), newName);
            return edit;
        }
    }

    private void processCompilationUnitResource(CompilationUnit source, Folder destination) throws CoreException {
        String newCUName = getNewNameFor(source);
        String destinationName = (newCUName != null) ? newCUName : source.getName();

        TextEdit edits = updateContent(source, destination, newCUName);

        IFile sourceResource = (IFile)source.getResource();

        IContainer destinationFolder = (IContainer)destination.getResource();
        IFile destinationFile = destinationFolder.getFile(new Path(destinationName));

        if(!destinationFile.equals(sourceResource)) {
            if(destinationFile.exists()) {
                if(m_force) {
                    deleteResource(destinationFile, IResource.KEEP_HISTORY);
                }
                else {
                    throw new CoreException(new Z8Status(Z8StatusConstants.NAME_COLLISION, Messages.format(
                            RefactoringMessages.status_nameCollision, destinationFile.getFullPath().toString())));
                }
            }

            int flags = m_force ? IResource.FORCE : IResource.NONE;

            if(isMove()) {
                flags |= IResource.KEEP_HISTORY;
                sourceResource.move(destinationFile.getFullPath(), flags, getSubProgressMonitor(1));
            }
            else {
                if(edits != null) {
                    flags |= IResource.KEEP_HISTORY;
                }
                sourceResource.copy(destinationFile.getFullPath(), flags, getSubProgressMonitor(1));
            }

            setAttribute(HAS_MODIFIED_RESOURCE_ATTR, TRUE);

            if(edits != null) {
                boolean wasReadOnly = destinationFile.isReadOnly();

                try {
                    saveContent(destination, destinationName, edits, null, destinationFile);
                }
                finally {
                    setReadOnly(destinationFile, wasReadOnly);
                }
            }
        }
        else {
            if(!m_force) {
                throw new CoreException(new Z8Status(Z8StatusConstants.NAME_COLLISION, Messages.format(
                        RefactoringMessages.status_nameCollision, destinationFile.getFullPath().toString())));
            }

            if(edits != null) {
                saveContent(destination, destinationName, edits, null, destinationFile);
            }
        }
    }

    public static void setReadOnly(IResource resource, boolean readOnly) {
        ResourceAttributes resourceAttributes = resource.getResourceAttributes();

        if(resourceAttributes == null)
            return;

        resourceAttributes.setReadOnly(readOnly);

        try {
            resource.setResourceAttributes(resourceAttributes);
        }
        catch(CoreException e) {}
    }

    public static boolean isReadOnly(IResource resource) {
        ResourceAttributes resourceAttributes = resource.getResourceAttributes();

        if(resourceAttributes == null)
            return false;

        return resourceAttributes.isReadOnly();
    }

    private void saveContent(Folder dest, String destName, TextEdit edits, String sourceEncoding, IFile destFile)
            throws CoreException {}

    private void processFolderResource(Folder source, Folder root, String newName) throws CoreException {
        if(newName == null)
            newName = source.getName();

        IContainer container = (IContainer)root.getResource();
        IFolder newFolder = container.getFolder(new Path(newName));

        IResource[] resources = collectResourcesOfInterest(source);

        boolean shouldMoveFolder = isMove() && !newFolder.exists();

        IFolder srcFolder = (IFolder)source.getResource();

        IPath destPath = newFolder.getFullPath();//.`.getAbsolutePath();//.getProject().getPath();

        if(shouldMoveFolder) {
            if(srcFolder.getFullPath().isPrefixOf(destPath)) {
                shouldMoveFolder = false;
            }
        }

        boolean containsReadOnlySubFolderFragments = createNeededFolders((IContainer)source.getFolder().getResource(), root,
                newName, shouldMoveFolder);
        boolean sourceIsReadOnly = isReadOnly(srcFolder);

        if(shouldMoveFolder) {
            if(sourceIsReadOnly) {
                setReadOnly(srcFolder, false);
            }

            srcFolder.move(destPath, m_force, true /* keep history */, getSubProgressMonitor(1));

            if(sourceIsReadOnly) {
                setReadOnly(srcFolder, true);
            }

            setAttribute(HAS_MODIFIED_RESOURCE_ATTR, TRUE);
        }
        else {
            if(resources.length > 0) {
                if(isRename()) {
                    if(!destPath.equals(source.getPath())) {
                        moveResources(resources, destPath);
                    }
                }
                else if(isMove()) {
                    for(int i = 0, max = resources.length; i < max; i++) {
                        IResource destinationResource = ResourcesPlugin.getWorkspace().getRoot()
                                .findMember(destPath.append(resources[i].getName()));

                        if(destinationResource != null) {
                            if(m_force) {
                                deleteResource(destinationResource, IResource.KEEP_HISTORY);
                            }
                            else {
                                throw new CoreException(new Z8Status(Z8StatusConstants.NAME_COLLISION, Messages.format(
                                        RefactoringMessages.status_nameCollision, destinationResource.getFullPath()
                                                .toString())));
                            }
                        }
                    }
                    moveResources(resources, destPath);
                }
                else {
                    for(int i = 0, max = resources.length; i < max; i++) {
                        IResource destinationResource = ResourcesPlugin.getWorkspace().getRoot()
                                .findMember(destPath.append(resources[i].getName()));

                        if(destinationResource != null) {
                            if(m_force) {
                                deleteResource(destinationResource, IResource.KEEP_HISTORY);
                            }
                            else {
                                throw new CoreException(new Z8Status(Z8StatusConstants.NAME_COLLISION, Messages.format(
                                        RefactoringMessages.status_nameCollision, destinationResource.getFullPath()
                                                .toString())));
                            }
                        }
                    }
                    copyResources(resources, destPath);
                }
            }
        }

        boolean isEmpty = true;

        if(isMove()) {
            if(srcFolder.exists()) {
                IResource[] remaining = srcFolder.members();
                for(int i = 0, length = remaining.length; i < length; i++) {
                    IResource file = remaining[i];
                    if(file instanceof IFile) {
                        if(isReadOnly(file)) {
                            setReadOnly(file, false);
                        }
                        deleteResource(file, IResource.FORCE | IResource.KEEP_HISTORY);
                    }
                    else {
                        isEmpty = false;
                    }
                }
            }
            if(isEmpty) {
                IResource rootResource;

                if(destPath.isPrefixOf(srcFolder.getFullPath())) {
                    rootResource = newFolder;
                }
                else {
                    rootResource = source.getFolder().getResource();
                }

                deleteEmptyFolderFragment(source, false, rootResource);
            }
        }
        else if(containsReadOnlySubFolderFragments) {}
    }

    private boolean createNeededFolders(IContainer sourceFolder, Folder root, String subFolderName, boolean moveFolder)
            throws CoreException {
        boolean containsReadOnlyFolderFragment = false;

        IContainer parentFolder = (IContainer)root.getResource();

        IResource subFolder = parentFolder.findMember(subFolderName);

        if(subFolder == null) {
            if(!moveFolder) {
                createFolder(parentFolder, subFolderName, m_force);
            }

            parentFolder = parentFolder.getFolder(new Path(subFolderName));
            sourceFolder = sourceFolder.getFolder(new Path(subFolderName));

            if(isReadOnly(sourceFolder)) {
                containsReadOnlyFolderFragment = true;
            }

            Folder sideEffectFolder = root.getFolder(subFolderName);

            createdElements.add(sideEffectFolder);
        }
        else {
            parentFolder = (IContainer)subFolder;
        }

        return containsReadOnlyFolderFragment;
    }
}
