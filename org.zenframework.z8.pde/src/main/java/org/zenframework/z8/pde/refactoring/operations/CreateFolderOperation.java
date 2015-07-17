package org.zenframework.z8.pde.refactoring.operations;

import java.util.ArrayList;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;

import org.zenframework.z8.compiler.core.ILanguageElement;
import org.zenframework.z8.compiler.workspace.Folder;
import org.zenframework.z8.compiler.workspace.Workspace;
import org.zenframework.z8.pde.debug.util.CharOperation;
import org.zenframework.z8.pde.refactoring.LanguageConventions;
import org.zenframework.z8.pde.refactoring.Z8Status;
import org.zenframework.z8.pde.refactoring.Z8StatusConstants;
import org.zenframework.z8.pde.refactoring.messages.Messages;
import org.zenframework.z8.pde.refactoring.messages.RefactoringMessages;

public class CreateFolderOperation extends Z8Operation {
    protected String[] m_folderName;

    public CreateFolderOperation(Folder parentElement, String folderName, boolean force) {
        super(null, new ILanguageElement[] { parentElement }, force);
        m_folderName = folderName == null ? null : folderName.split(".");
    }

    public static final String[] arrayConcat(String[] first, String second) {
        if(second == null)
            return first;
        if(first == null)
            return new String[] { second };
        int length = first.length;
        if(first.length == 0) {
            return new String[] { second };
        }

        String[] result = new String[length + 1];
        System.arraycopy(first, 0, result, 0, length);
        result[length] = second;
        return result;
    }

    @Override
    protected void executeOperation() throws CoreException {
        try {
            Folder root = (Folder)getParentElement();
            beginTask(RefactoringMessages.operation_createFolderProgress, m_folderName.length);
            IContainer parentFolder = (IContainer)root.getResource();
            String[] sideEffectFolderName = CharOperation.NO_STRINGS;

            ArrayList<ILanguageElement> results = new ArrayList<ILanguageElement>(m_folderName.length);

            for(int i = 0; i < m_folderName.length; i++) {
                String subFolderName = m_folderName[i];

                sideEffectFolderName = arrayConcat(sideEffectFolderName, subFolderName);

                IResource subFolder = parentFolder.findMember(subFolderName);

                if(subFolder == null) {
                    createFolder(parentFolder, subFolderName, m_force);
                    parentFolder = parentFolder.getFolder(new Path(subFolderName));
                    Folder addedFrag = Workspace.getInstance().getFolder(parentFolder).getFolder(subFolderName);
                    results.add(addedFrag);
                }
                else {
                    parentFolder = (IContainer)subFolder;
                }
                worked(1);
            }

            if(results.size() > 0) {
                m_resultElements = new ILanguageElement[results.size()];
                results.toArray(m_resultElements);
            }
        }
        finally {
            done();
        }
    }

    public static final String concatWith(String[] array, char separator) {
        StringBuffer buffer = new StringBuffer();
        for(int i = 0, length = array.length; i < length; i++) {
            buffer.append(array[i]);
            if(i < length - 1)
                buffer.append(separator);
        }
        return buffer.toString();
    }

    @Override
    public IStatus verify() {
        ILanguageElement parentElement = getParentElement();

        if(parentElement == null) {
            return new Z8Status(Z8StatusConstants.NO_ELEMENTS_TO_PROCESS);
        }

        String folderName = m_folderName == null ? null : concatWith(m_folderName, '.');

        if(m_folderName == null
                || (m_folderName.length > 0 && LanguageConventions.validateFolderName(folderName).getSeverity() == IStatus.ERROR)) {
            return new Z8Status(Z8StatusConstants.INVALID_NAME, folderName);
        }

        IContainer parentFolder = (IContainer)((Folder)getParentElement()).getResource();

        for(int i = 0; i < m_folderName.length; i++) {
            IResource subFolder = parentFolder.findMember(m_folderName[i]);

            if(subFolder != null) {
                if(subFolder.getType() != IResource.FOLDER) {
                    return new Z8Status(Z8StatusConstants.NAME_COLLISION, Messages.format(
                            RefactoringMessages.status_nameCollision, subFolder.getFullPath().toString()));
                }
                parentFolder = (IContainer)subFolder;
            }
        }
        return Z8Status.VERIFIED_OK;
    }
}
