package org.zenframework.z8.pde.refactoring.changes;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension4;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import org.zenframework.z8.compiler.core.ILanguageElement;
import org.zenframework.z8.compiler.workspace.Resource;
import org.zenframework.z8.pde.refactoring.Resources;
import org.zenframework.z8.pde.refactoring.messages.Messages;
import org.zenframework.z8.pde.refactoring.messages.RefactoringMessages;

@SuppressWarnings("deprecation")
public abstract class Z8Change extends Change {
    private long m_modificationStamp;
    private boolean m_readOnly;

    private static class ValidationState {
        private IResource m_resource;
        private int m_kind;
        private boolean m_dirty;
        private boolean m_readOnly;
        private long m_modificationStamp;
        private ITextFileBuffer m_textFileBuffer;
        public static final int RESOURCE = 1;
        public static final int DOCUMENT = 2;

        public ValidationState(IResource resource) {
            m_resource = resource;

            if(resource instanceof IFile) {
                initializeFile((IFile)resource);
            }
            else {
                initializeResource(resource);
            }
        }

        public void checkDirty(RefactoringStatus status, long stampToMatch, IProgressMonitor pm) throws CoreException {
            if(m_dirty) {
                if(m_kind == DOCUMENT && m_textFileBuffer != null && stampToMatch == m_modificationStamp) {
                    m_textFileBuffer.commit(pm, false);
                }
                else {
                    status.addFatalError(Messages.format(RefactoringMessages.Change_is_unsaved, m_resource.getFullPath()
                            .toString()));
                }
            }
        }

        public void checkDirty(RefactoringStatus status) {
            if(m_dirty) {
                status.addFatalError(Messages.format(RefactoringMessages.Change_is_unsaved, m_resource.getFullPath()
                        .toString()));
            }
        }

        public void checkReadOnly(RefactoringStatus status) {
            if(m_readOnly) {
                status.addFatalError(Messages.format(RefactoringMessages.Change_is_read_only, m_resource.getFullPath()
                        .toString()));
            }
        }

        public void checkSameReadOnly(RefactoringStatus status, boolean valueToMatch) {
            if(m_readOnly != valueToMatch) {
                status.addFatalError(Messages.format(RefactoringMessages.Change_same_read_only, m_resource.getFullPath()
                        .toString()));
            }
        }

        public void checkModificationStamp(RefactoringStatus status, long stampToMatch) {
            if(m_kind == DOCUMENT) {
                if(stampToMatch != IDocumentExtension4.UNKNOWN_MODIFICATION_STAMP && m_modificationStamp != stampToMatch) {
                    status.addFatalError(Messages.format(RefactoringMessages.Change_has_modifications, m_resource
                            .getFullPath().toString()));
                }
            }
            else {
                if(stampToMatch != IResource.NULL_STAMP && m_modificationStamp != stampToMatch) {
                    status.addFatalError(Messages.format(RefactoringMessages.Change_has_modifications, m_resource
                            .getFullPath().toString()));
                }
            }
        }

        private void initializeFile(IFile file) {
            m_textFileBuffer = getBuffer(file);
            if(m_textFileBuffer == null) {
                initializeResource(file);
            }
            else {
                IDocument document = m_textFileBuffer.getDocument();
                m_dirty = m_textFileBuffer.isDirty();
                m_readOnly = Resources.isReadOnly(file);
                if(document instanceof IDocumentExtension4) {
                    m_kind = DOCUMENT;
                    m_modificationStamp = ((IDocumentExtension4)document).getModificationStamp();
                }
                else {
                    m_kind = RESOURCE;
                    m_modificationStamp = file.getModificationStamp();
                }
            }
        }

        private void initializeResource(IResource resource) {
            m_kind = RESOURCE;
            m_dirty = false;
            m_readOnly = Resources.isReadOnly(resource);
            m_modificationStamp = resource.getModificationStamp();
        }
    }

    protected static final int NONE = 0;
    protected static final int READ_ONLY = 1 << 0;
    protected static final int DIRTY = 1 << 1;
    private static final int SAVE = 1 << 2;
    protected static final int SAVE_IF_DIRTY = SAVE | DIRTY;

    protected Z8Change() {
        m_modificationStamp = IResource.NULL_STAMP;
        m_readOnly = false;
    }

    @Override
    public void initializeValidationData(IProgressMonitor pm) {
        IResource resource = getResource(getModifiedElement());
        if(resource != null) {
            m_modificationStamp = getModificationStamp(resource);
            m_readOnly = Resources.isReadOnly(resource);
        }
    }

    protected final RefactoringStatus isValid(IProgressMonitor pm, int flags) throws CoreException {
        pm.beginTask("", 2);

        try {
            RefactoringStatus result = new RefactoringStatus();
            Object modifiedElement = getModifiedElement();

            checkExistence(result, modifiedElement);

            if(result.hasFatalError())
                return result;
            if(flags == NONE)
                return result;

            IResource resource = getResource(modifiedElement);

            if(resource != null) {
                ValidationState state = new ValidationState(resource);
                state.checkModificationStamp(result, m_modificationStamp);

                if(result.hasFatalError())
                    return result;

                state.checkSameReadOnly(result, m_readOnly);

                if(result.hasFatalError())
                    return result;

                if((flags & READ_ONLY) != 0) {
                    state.checkReadOnly(result);
                    if(result.hasFatalError())
                        return result;
                }

                if((flags & DIRTY) != 0) {
                    if((flags & SAVE) != 0) {
                        state.checkDirty(result, m_modificationStamp, new SubProgressMonitor(pm, 1));
                    }
                    else {
                        state.checkDirty(result);
                    }
                }
            }
            return result;
        }
        finally {
            pm.done();
        }
    }

    protected final RefactoringStatus isValid(int flags) throws CoreException {
        return isValid(new NullProgressMonitor(), flags);
    }

    protected static void checkIfModifiable(RefactoringStatus status, Object element, int flags) {
        checkIfModifiable(status, getResource(element), flags);
    }

    protected static void checkIfModifiable(RefactoringStatus result, IResource resource, int flags) {
        checkExistence(result, resource);

        if(result.hasFatalError())
            return;

        if(flags == NONE)
            return;

        ValidationState state = new ValidationState(resource);

        if((flags & READ_ONLY) != 0) {
            state.checkReadOnly(result);
            if(result.hasFatalError())
                return;
        }
        if((flags & DIRTY) != 0) {
            state.checkDirty(result);
        }
    }

    protected static void checkExistence(RefactoringStatus status, Object element) {
        if(element == null) {
            status.addFatalError(RefactoringMessages.DynamicValidationStateChange_workspace_changed);
        }
        else if(element instanceof IResource && !((IResource)element).exists()) {
            status.addFatalError(Messages.format(RefactoringMessages.Change_does_not_exist, ((IResource)element)
                    .getFullPath().toString()));
        }
    }

    private static IResource getResource(Object element) {
        if(element instanceof IResource) {
            return (IResource)element;
        }
        if(element instanceof Resource) {
            return ((Resource)element).getResource();
        }
        if(element instanceof ILanguageElement) {
            return ((ILanguageElement)element).getCompilationUnit().getResource();
        }
        if(element instanceof IAdaptable) {
            return (IResource)((IAdaptable)element).getAdapter(IResource.class);
        }
        return null;
    }

    @Override
    public String toString() {
        return getName();
    }

    public long getModificationStamp(IResource resource) {
        if(!(resource instanceof IFile))
            return resource.getModificationStamp();

        IFile file = (IFile)resource;
        ITextFileBuffer buffer = getBuffer(file);

        if(buffer == null) {
            return file.getModificationStamp();
        }
        else {
            IDocument document = buffer.getDocument();
            if(document instanceof IDocumentExtension4) {
                return ((IDocumentExtension4)document).getModificationStamp();
            }
            else {
                return file.getModificationStamp();
            }
        }
    }

    private static ITextFileBuffer getBuffer(IFile file) {
        ITextFileBufferManager manager = FileBuffers.getTextFileBufferManager();
        return manager.getTextFileBuffer(file.getFullPath());
    }
}
