package org.zenframework.z8.pde.refactoring.reorg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.ide.ResourceUtil;

import org.zenframework.z8.compiler.core.ILanguageElement;
import org.zenframework.z8.compiler.workspace.CompilationUnit;
import org.zenframework.z8.compiler.workspace.Folder;
import org.zenframework.z8.compiler.workspace.Resource;
import org.zenframework.z8.pde.refactoring.messages.Messages;
import org.zenframework.z8.pde.refactoring.messages.RefactoringMessages;

class OverwriteHelper {
    private Object m_destination;
    private IFile[] m_files = new IFile[0];
    private CompilationUnit[] m_compilationUnits = new CompilationUnit[0];
    private Folder[] m_folders = new Folder[0];

    public void setFiles(IFile[] files) {
        m_files = files;
    }

    public void setFolders(Folder[] folders) {
        m_folders = folders;
    }

    public void setCus(CompilationUnit[] compilationUnits) {
        m_compilationUnits = compilationUnits;
    }

    public IFile[] getFilesWithoutUnconfirmedOnes() {
        return m_files;
    }

    public Folder[] getFoldersWithoutUnconfirmedOnes() {
        return m_folders;
    }

    public CompilationUnit[] getCusWithoutUnconfirmedOnes() {
        return m_compilationUnits;
    }

    public void confirmOverwriting(IReorgQueries reorgQueries, ILanguageElement destination) {
        m_destination = destination;
        confirmOverwritting(reorgQueries);
    }

    public void confirmOverwriting(IReorgQueries reorgQueries, IResource destination) {
        m_destination = destination;
        confirmOverwritting(reorgQueries);
    }

    private void confirmOverwritting(IReorgQueries reorgQueries) {
        IConfirmQuery overwriteQuery = reorgQueries.createYesYesToAllNoNoToAllQuery(RefactoringMessages.OverwriteHelper_0,
                true, IReorgQueries.CONFIRM_OVERWRITING);
        confirmFileOverwritting(overwriteQuery);
        confirmCuOverwritting(overwriteQuery);
        confirmFolderOverwritting(overwriteQuery);
    }

    private void confirmCuOverwritting(IConfirmQuery overwriteQuery) {
        List<CompilationUnit> cusToNotOverwrite = new ArrayList<CompilationUnit>(1);
        for(int i = 0; i < m_compilationUnits.length; i++) {
            CompilationUnit cu = m_compilationUnits[i];

            if(canOverwrite(cu) && !overwrite(cu, overwriteQuery))
                cusToNotOverwrite.add(cu);
        }

        CompilationUnit[] cus = cusToNotOverwrite.toArray(new CompilationUnit[cusToNotOverwrite.size()]);
        m_compilationUnits = setMinus(m_compilationUnits, cus);
    }

    private void confirmFolderOverwritting(IConfirmQuery overwriteQuery) {
        List<Folder> foldersToNotOverwrite = new ArrayList<Folder>(1);

        for(Folder folder : m_folders) {
            if(canOverwrite(folder) && !skip(folder.getName(), overwriteQuery))
                foldersToNotOverwrite.add(folder);
        }

        Folder[] folders = foldersToNotOverwrite.toArray(new Folder[foldersToNotOverwrite.size()]);
        m_folders = setMinus(m_folders, folders);
    }

    public static CompilationUnit[] setMinus(CompilationUnit[] setToRemoveFrom, CompilationUnit[] elementsToRemove) {
        Set<CompilationUnit> setMinus = new HashSet<CompilationUnit>(setToRemoveFrom.length - setToRemoveFrom.length);
        setMinus.addAll(Arrays.asList(setToRemoveFrom));
        setMinus.removeAll(Arrays.asList(elementsToRemove));
        return setMinus.toArray(new CompilationUnit[setMinus.size()]);
    }

    public static Folder[] setMinus(Folder[] setToRemoveFrom, Folder[] elementsToRemove) {
        Set<Folder> setMinus = new HashSet<Folder>(setToRemoveFrom.length - setToRemoveFrom.length);
        setMinus.addAll(Arrays.asList(setToRemoveFrom));
        setMinus.removeAll(Arrays.asList(elementsToRemove));
        return setMinus.toArray(new Folder[setMinus.size()]);
    }

    public static IFolder[] setMinus(IFolder[] setToRemoveFrom, IFolder[] elementsToRemove) {
        Set<IFolder> setMinus = new HashSet<IFolder>(setToRemoveFrom.length - setToRemoveFrom.length);
        setMinus.addAll(Arrays.asList(setToRemoveFrom));
        setMinus.removeAll(Arrays.asList(elementsToRemove));
        return setMinus.toArray(new IFolder[setMinus.size()]);
    }

    public static IFile[] setMinus(IFile[] setToRemoveFrom, IFile[] elementsToRemove) {
        Set<IFile> setMinus = new HashSet<IFile>(setToRemoveFrom.length - setToRemoveFrom.length);
        setMinus.addAll(Arrays.asList(setToRemoveFrom));
        setMinus.removeAll(Arrays.asList(elementsToRemove));
        return setMinus.toArray(new IFile[setMinus.size()]);
    }

    private void confirmFileOverwritting(IConfirmQuery overwriteQuery) {
        List<IFile> filesToNotOverwrite = new ArrayList<IFile>(1);
        for(int i = 0; i < m_files.length; i++) {
            IFile file = m_files[i];
            if(canOverwrite(file) && !overwrite(file, overwriteQuery))
                filesToNotOverwrite.add(file);
        }
        IFile[] files = filesToNotOverwrite.toArray(new IFile[filesToNotOverwrite.size()]);
        m_files = (IFile[])setMinus(m_files, files);
    }

    private boolean canOverwrite(Folder source) {
        Folder destination = (Folder)m_destination;
        Folder newFolder = destination.getFolder(source.getName());
        return !destination.equals(source.getParent()) && newFolder != null && newFolder.getResource().exists();
    }

    public static IResource getResource(Object object) {
        if(object instanceof Resource) {
            return ((Resource)object).getResource();
        }
        return ResourceUtil.getResource(object);
    }

    private boolean canOverwrite(IResource resource) {
        if(resource == null)
            return false;

        IResource destinationResource = getResource(m_destination);

        if(destinationResource.equals(resource.getParent()))
            return false;

        if(destinationResource instanceof IContainer) {
            IContainer container = (IContainer)destinationResource;
            IResource member = container.findMember(resource.getName());
            if(member == null || !member.exists())
                return false;
            if(member instanceof IContainer) {
                try {
                    if(((IContainer)member).members().length == 0)
                        return false;
                }
                catch(CoreException e) {
                    return true;
                }
            }
            return true;
        }
        return false;
    }

    private boolean canOverwrite(CompilationUnit cu) {
        if(m_destination instanceof Folder) {
            Folder destination = (Folder)m_destination;
            CompilationUnit compilationUnit = destination.getCompilationUnit(cu.getName());
            return !destination.equals(cu.getParent()) && compilationUnit != null
                    && compilationUnit.getResource().isAccessible();
        }
        else {
            return canOverwrite(cu.getResource());
        }
    }

    private static boolean overwrite(IResource resource, IConfirmQuery overwriteQuery) {
        return overwrite(resource.getName(), overwriteQuery);
    }

    private static boolean overwrite(ILanguageElement element, IConfirmQuery overwriteQuery) {
        return overwrite(((Resource)element).getName(), overwriteQuery);
    }

    private static boolean overwrite(String name, IConfirmQuery overwriteQuery) {
        String question = Messages.format(RefactoringMessages.OverwriteHelper_1, name);
        return overwriteQuery.confirm(question);
    }

    private static boolean skip(String name, IConfirmQuery overwriteQuery) {
        String question = Messages.format(RefactoringMessages.OverwriteHelper_3, name);
        return overwriteQuery.confirm(question);
    }
}
