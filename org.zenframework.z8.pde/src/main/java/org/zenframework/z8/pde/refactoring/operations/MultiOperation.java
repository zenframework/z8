package org.zenframework.z8.pde.refactoring.operations;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;

import org.zenframework.z8.compiler.core.ILanguageElement;
import org.zenframework.z8.compiler.workspace.CompilationUnit;
import org.zenframework.z8.compiler.workspace.Folder;
import org.zenframework.z8.pde.refactoring.LanguageConventions;
import org.zenframework.z8.pde.refactoring.Z8Status;
import org.zenframework.z8.pde.refactoring.Z8StatusConstants;

public abstract class MultiOperation extends Z8Operation {
    protected Map<ILanguageElement, ILanguageElement> m_insertBeforeElements = new HashMap<ILanguageElement, ILanguageElement>(
            1);
    protected Map<ILanguageElement, ILanguageElement> m_newParents;

    protected Map<ILanguageElement, String> m_renamings;

    protected String[] m_renamingsList = null;

    protected MultiOperation(ILanguageElement[] elementsToProcess, boolean force) {
        super(elementsToProcess, force);
    }

    protected MultiOperation(ILanguageElement[] elementsToProcess, ILanguageElement[] parentElements, boolean force) {
        super(elementsToProcess, parentElements, force);

        m_newParents = new HashMap<ILanguageElement, ILanguageElement>(elementsToProcess.length);

        if(elementsToProcess.length == parentElements.length) {
            for(int i = 0; i < elementsToProcess.length; i++) {
                m_newParents.put(elementsToProcess[i], parentElements[i]);
            }
        }
        else {
            for(int i = 0; i < elementsToProcess.length; i++) {
                m_newParents.put(elementsToProcess[i], parentElements[0]);
            }
        }
    }

    protected void error(int code, ILanguageElement element) throws CoreException {
        throw new CoreException(new Z8Status(code, element));
    }

    @Override
    protected void executeOperation() throws CoreException {
        processElements();
    }

    protected ILanguageElement getDestinationParent(ILanguageElement child) {
        return m_newParents.get(child);
    }

    protected abstract String getMainTaskName();

    protected String getNewNameFor(ILanguageElement element) throws CoreException {
        String newName = null;

        if(m_renamings != null) {
            newName = m_renamings.get(element);
        }

        return newName;
    }

    private void initializeRenamings() {
        if(m_renamingsList != null && m_renamingsList.length == m_elementsToProcess.length) {
            m_renamings = new HashMap<ILanguageElement, String>(m_renamingsList.length);

            for(int i = 0; i < m_renamingsList.length; i++) {
                if(m_renamingsList[i] != null) {
                    m_renamings.put(m_elementsToProcess[i], m_renamingsList[i]);
                }
            }
        }
    }

    protected boolean isMove() {
        return false;
    }

    protected boolean isRename() {
        return false;
    }

    protected abstract void processElement(ILanguageElement element) throws CoreException;

    protected void processElements() throws CoreException {
        beginTask(getMainTaskName(), m_elementsToProcess.length);

        IStatus[] errors = new IStatus[3];

        int errorsCounter = 0;

        for(int i = 0; i < m_elementsToProcess.length; i++) {
            try {
                verify(m_elementsToProcess[i]);
                processElement(m_elementsToProcess[i]);
            }
            catch(CoreException exception) {
                if(errorsCounter == errors.length) {
                    System.arraycopy(errors, 0, (errors = new IStatus[errorsCounter * 2]), 0, errorsCounter);
                }
                errors[errorsCounter++] = exception.getStatus();
            }
            finally {
                worked(1);
            }
        }

        done();

        if(errorsCounter == 1) {
            throw new CoreException(errors[0]);
        }
        else if(errorsCounter > 1) {
            if(errorsCounter != errors.length) {
                System.arraycopy(errors, 0, (errors = new IStatus[errorsCounter]), 0, errorsCounter);
            }
            throw new CoreException(Z8Status.newMultiStatus(errors));
        }
    }

    public void setInsertBefore(ILanguageElement modifiedElement, ILanguageElement newSibling) {
        m_insertBeforeElements.put(modifiedElement, newSibling);
    }

    public void setRenamings(String[] renamingsList) {
        m_renamingsList = renamingsList;
        initializeRenamings();
    }

    protected abstract void verify(ILanguageElement element) throws CoreException;

    protected void verifyDestination(ILanguageElement element, ILanguageElement destination) throws CoreException {
        Folder folder = null;

        if(destination instanceof Folder) {
            folder = (Folder)destination;
        }

        if(destination == null || folder != null && !folder.getResource().exists()) {
            error(Z8StatusConstants.ELEMENT_DOES_NOT_EXIST, destination);
        }

        if(element instanceof CompilationUnit)

            if(destination instanceof Folder) {}
            else {
                error(Z8StatusConstants.INVALID_DESTINATION, element);
            }
    }

    protected void verifyRenaming(ILanguageElement element) throws CoreException {
        String newName = getNewNameFor(element);

        boolean isValid = true;

        if(element instanceof Folder) {
            isValid = LanguageConventions.validateFolderName(newName).getSeverity() != IStatus.ERROR;
        }
        else if(element instanceof CompilationUnit) {
            isValid = LanguageConventions.validateCompilationUnitName(newName).getSeverity() != IStatus.ERROR;
        }
        else {
            isValid = LanguageConventions.validateIdentifier(newName).getSeverity() != IStatus.ERROR;
        }

        if(!isValid) {
            throw new CoreException(new Z8Status(Z8StatusConstants.INVALID_NAME, element, newName));
        }
    }

    protected void verifySibling(ILanguageElement element, ILanguageElement destination) throws CoreException {
        ILanguageElement insertBeforeElement = (ILanguageElement)m_insertBeforeElements.get(element);

        if(insertBeforeElement != null) {}
    }
}
