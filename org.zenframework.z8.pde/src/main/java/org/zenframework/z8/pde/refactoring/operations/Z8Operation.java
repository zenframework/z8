package org.zenframework.z8.pde.refactoring.operations;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.internal.filebuffers.FileBuffersPlugin;
import org.eclipse.core.internal.filebuffers.TextFileBufferManager;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jface.text.IDocument;

import org.zenframework.z8.compiler.core.ILanguageElement;
import org.zenframework.z8.compiler.workspace.CompilationUnit;
import org.zenframework.z8.compiler.workspace.Folder;
import org.zenframework.z8.pde.Plugin;
import org.zenframework.z8.pde.refactoring.messages.RefactoringMessages;

@SuppressWarnings("restriction")
public abstract class Z8Operation implements IWorkspaceRunnable, IProgressMonitor {
    protected interface IPostAction {
        String getID();

        void run() throws CoreException;
    }

    protected static final int APPEND = 1;
    protected static final int REMOVEALL_APPEND = 2;
    protected static final int KEEP_EXISTING = 3;

    protected static boolean POST_ACTION_VERBOSE;

    protected IPostAction[] m_actions;
    protected int m_actionsStart = 0;
    protected int m_actionsEnd = -1;

    protected HashMap<Object, Object> m_attributes;

    public static final String HAS_MODIFIED_RESOURCE_ATTR = "hasModifiedResource";
    public static final String TRUE = "true";

    protected static ILanguageElement[] NO_ELEMENTS = new ILanguageElement[] {};

    protected ILanguageElement[] m_elementsToProcess;
    protected ILanguageElement[] m_parentElements;
    protected ILanguageElement[] m_resultElements = NO_ELEMENTS;
    public IProgressMonitor m_progressMonitor = null;

    protected boolean m_isNested = false;
    protected boolean m_force = false;

    protected static ThreadLocal<List<Z8Operation>> m_operationStacks = new ThreadLocal<List<Z8Operation>>();

    protected Z8Operation() {}

    protected Z8Operation(ILanguageElement[] elements) {
        m_elementsToProcess = elements;
    }

    protected Z8Operation(ILanguageElement[] elementsToProcess, ILanguageElement[] parentElements) {
        m_elementsToProcess = elementsToProcess;
        m_parentElements = parentElements;
    }

    protected Z8Operation(ILanguageElement[] elementsToProcess, ILanguageElement[] parentElements, boolean force) {
        m_elementsToProcess = elementsToProcess;
        m_parentElements = parentElements;
        m_force = force;
    }

    protected Z8Operation(ILanguageElement[] elements, boolean force) {
        m_elementsToProcess = elements;
        m_force = force;
    }

    protected Z8Operation(ILanguageElement element) {
        m_elementsToProcess = new ILanguageElement[] { element };
    }

    protected Z8Operation(ILanguageElement element, boolean force) {
        m_elementsToProcess = new ILanguageElement[] { element };
        m_force = force;
    }

    protected void addAction(IPostAction action) {
        int length = m_actions.length;

        if(length == ++m_actionsEnd) {
            System.arraycopy(m_actions, 0, m_actions = new IPostAction[length * 2], 0, length);
        }

        m_actions[m_actionsEnd] = action;
    }

    @Override
    public void beginTask(String name, int totalWork) {
        if(m_progressMonitor != null) {
            m_progressMonitor.beginTask(name, totalWork);
        }
    }

    protected boolean canModifyRoots() {
        return false;
    }

    protected void checkCanceled() {
        if(isCanceled()) {
            throw new OperationCanceledException(RefactoringMessages.operation_cancelled);
        }
    }

    protected IStatus commonVerify() {
        if(m_elementsToProcess == null || m_elementsToProcess.length == 0) {
            return new Status(IStatus.ERROR, Plugin.PLUGIN_ID, 0, "JavaModelStatus", null);
        }
        for(int i = 0; i < m_elementsToProcess.length; i++) {
            if(m_elementsToProcess[i] == null) {
                return new Status(IStatus.ERROR, Plugin.PLUGIN_ID, 0, "JavaModelStatus", null);
            }
        }
        return Status.OK_STATUS;
    }

    protected void copyResources(IResource[] resources, IPath destinationPath) throws CoreException {
        IProgressMonitor subProgressMonitor = getSubProgressMonitor(resources.length);
        IWorkspace workspace = resources[0].getWorkspace();

        workspace.copy(resources, destinationPath, false, subProgressMonitor);
        setAttribute(HAS_MODIFIED_RESOURCE_ATTR, TRUE);
    }

    protected void createFile(IContainer folder, String name, InputStream contents, boolean forceFlag) throws CoreException {
        IFile file = folder.getFile(new Path(name));
        file.create(contents, forceFlag ? IResource.FORCE | IResource.KEEP_HISTORY : IResource.KEEP_HISTORY,
                getSubProgressMonitor(1));
        setAttribute(HAS_MODIFIED_RESOURCE_ATTR, TRUE);
    }

    protected void createFolder(IContainer parentFolder, String name, boolean forceFlag) throws CoreException {
        IFolder folder = parentFolder.getFolder(new Path(name));
        folder.create(forceFlag ? IResource.FORCE | IResource.KEEP_HISTORY : IResource.KEEP_HISTORY, true,
                getSubProgressMonitor(1));
        setAttribute(HAS_MODIFIED_RESOURCE_ATTR, TRUE);
    }

    protected void deleteEmptyFolderFragment(Folder fragment, boolean forceFlag, IResource rootResource)
            throws CoreException {
        IContainer resource = (IContainer)fragment.getResource();

        resource.delete(forceFlag ? IResource.FORCE | IResource.KEEP_HISTORY : IResource.KEEP_HISTORY,
                getSubProgressMonitor(1));

        setAttribute(HAS_MODIFIED_RESOURCE_ATTR, TRUE);

        while(resource instanceof IFolder) {
            resource = resource.getParent();

            if(!resource.equals(rootResource) && resource.members().length == 0) {
                resource.delete(forceFlag ? IResource.FORCE | IResource.KEEP_HISTORY : IResource.KEEP_HISTORY,
                        getSubProgressMonitor(1));
                setAttribute(HAS_MODIFIED_RESOURCE_ATTR, TRUE);
            }
        }
    }

    protected void deleteResource(IResource resource, int flags) throws CoreException {
        resource.delete(flags, getSubProgressMonitor(1));
        setAttribute(HAS_MODIFIED_RESOURCE_ATTR, TRUE);
    }

    protected void deleteResources(IResource[] resources, boolean forceFlag) throws CoreException {
        if(resources == null || resources.length == 0)
            return;

        IProgressMonitor subProgressMonitor = getSubProgressMonitor(resources.length);
        IWorkspace workspace = resources[0].getWorkspace();

        workspace.delete(resources, forceFlag ? IResource.FORCE | IResource.KEEP_HISTORY : IResource.KEEP_HISTORY,
                subProgressMonitor);
        setAttribute(HAS_MODIFIED_RESOURCE_ATTR, TRUE);
    }

    @Override
    public void done() {
        if(m_progressMonitor != null) {
            m_progressMonitor.done();
        }
    }

    protected boolean equalsOneOf(IPath path, IPath[] otherPaths) {
        for(int i = 0, length = otherPaths.length; i < length; i++) {
            if(path.equals(otherPaths[i])) {
                return true;
            }
        }
        return false;
    }

    public void executeNestedOperation(Z8Operation operation, int subWorkAmount) throws CoreException {
        IStatus status = operation.verify();

        if(!status.isOK()) {
            throw new CoreException(status);
        }
        IProgressMonitor subProgressMonitor = getSubProgressMonitor(subWorkAmount);

        operation.setNested(true);
        operation.run(subProgressMonitor);
    }

    protected abstract void executeOperation() throws CoreException;

    protected Object getAttribute(Object key) {
        List<Z8Operation> stack = getCurrentOperationStack();

        if(stack.size() == 0)
            return null;

        Z8Operation topLevelOp = stack.get(0);

        if(topLevelOp.m_attributes == null) {
            return null;
        }
        else {
            return topLevelOp.m_attributes.get(key);
        }
    }

    protected CompilationUnit getCompilationUnitFor(ILanguageElement element) {
        return element.getCompilationUnit();
    }

    protected static List<Z8Operation> getCurrentOperationStack() {
        List<Z8Operation> stack = m_operationStacks.get();

        if(stack == null) {
            stack = new ArrayList<Z8Operation>();
            m_operationStacks.set(stack);
        }
        return stack;
    }

    @SuppressWarnings("deprecation")
    protected ITextFileBuffer getBuffer(CompilationUnit compilationUnit, IProgressMonitor monitor) throws CoreException {
        ITextFileBufferManager manager = (TextFileBufferManager)FileBuffersPlugin.getDefault().getFileBufferManager();

        IPath location = compilationUnit.getResource().getFullPath();
        manager.connect(location, monitor);
        return manager.getTextFileBuffer(location);
    }

    protected IDocument getDocument(CompilationUnit compilationUnit) throws CoreException {
        ITextFileBuffer buffer = getBuffer(compilationUnit, m_progressMonitor);
        return buffer.getDocument();
    }

    protected ILanguageElement[] getElementsToProcess() {
        return m_elementsToProcess;
    }

    protected ILanguageElement getElementToProcess() {
        if(m_elementsToProcess == null || m_elementsToProcess.length == 0) {
            return null;
        }
        return m_elementsToProcess[0];
    }

    protected ILanguageElement getParentElement() {
        if(m_parentElements == null || m_parentElements.length == 0) {
            return null;
        }
        return m_parentElements[0];
    }

    protected ILanguageElement[] getParentElements() {
        return m_parentElements;
    }

    public ILanguageElement[] getResultElements() {
        return m_resultElements;
    }

    protected ISchedulingRule getSchedulingRule() {
        return ResourcesPlugin.getWorkspace().getRoot();
    }

    protected IProgressMonitor getSubProgressMonitor(int workAmount) {
        IProgressMonitor sub = null;
        if(m_progressMonitor != null) {
            sub = new SubProgressMonitor(m_progressMonitor, workAmount, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK);
        }
        return sub;
    }

    public boolean hasModifiedResource() {
        return !isReadOnly() && getAttribute(HAS_MODIFIED_RESOURCE_ATTR) == TRUE;
    }

    @Override
    public void internalWorked(double work) {
        if(m_progressMonitor != null) {
            m_progressMonitor.internalWorked(work);
        }
    }

    @Override
    public boolean isCanceled() {
        if(m_progressMonitor != null) {
            return m_progressMonitor.isCanceled();
        }
        return false;
    }

    public boolean isReadOnly() {
        return false;
    }

    protected boolean isTopLevelOperation() {
        List<Z8Operation> stack = getCurrentOperationStack();
        return stack.iterator().hasNext() && stack.get(0) == this;
    }

    protected int firstActionWithID(String id, int start) {
        for(int i = start; i <= m_actionsEnd; i++) {
            if(m_actions[i].getID().equals(id)) {
                return i;
            }
        }
        return -1;
    }

    protected void moveResources(IResource[] resources, IPath destinationPath) throws CoreException {
        IProgressMonitor subProgressMonitor = null;

        if(m_progressMonitor != null) {
            subProgressMonitor = new SubProgressMonitor(m_progressMonitor, resources.length,
                    SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK);
        }
        IWorkspace workspace = resources[0].getWorkspace();

        workspace.move(resources, destinationPath, false, subProgressMonitor);
        setAttribute(HAS_MODIFIED_RESOURCE_ATTR, TRUE);
    }

    protected Z8Operation popOperation() {
        ArrayList<Z8Operation> stack = (ArrayList<Z8Operation>)getCurrentOperationStack();

        int size = stack.size();

        if(size > 0) {
            if(size == 1) {
                m_operationStacks.set(null);
            }
            return (Z8Operation)stack.remove(size - 1);
        }
        else {
            return null;
        }
    }

    protected void postAction(IPostAction action, int insertionMode) {
        if(POST_ACTION_VERBOSE) {
            System.out
                    .print("(" + Thread.currentThread() + ") [JavaModelOperation.postAction(IPostAction, int)] Posting action " + action.getID()); //$NON-NLS-1$ //$NON-NLS-2$
            switch(insertionMode) {
            case REMOVEALL_APPEND:
                System.out.println(" (REMOVEALL_APPEND)");
                break;
            case KEEP_EXISTING:
                System.out.println(" (KEEP_EXISTING)");
                break;
            case APPEND:
                System.out.println(" (APPEND)");
                break;
            }
        }

        Z8Operation topLevelOp = getCurrentOperationStack().get(0);

        IPostAction[] postActions = topLevelOp.m_actions;

        if(postActions == null) {
            topLevelOp.m_actions = postActions = new IPostAction[1];
            postActions[0] = action;
            topLevelOp.m_actionsEnd = 0;
        }
        else {
            String id = action.getID();
            switch(insertionMode) {
            case REMOVEALL_APPEND:
                int index = m_actionsStart - 1;
                while((index = topLevelOp.firstActionWithID(id, index + 1)) >= 0) {
                    // remove action[index]
                    System.arraycopy(postActions, index + 1, postActions, index, topLevelOp.m_actionsEnd - index);
                    postActions[topLevelOp.m_actionsEnd--] = null;
                }
                topLevelOp.addAction(action);
                break;
            case KEEP_EXISTING:
                if(topLevelOp.firstActionWithID(id, 0) < 0) {
                    topLevelOp.addAction(action);
                }
                break;
            case APPEND:
                topLevelOp.addAction(action);
                break;
            }
        }
    }

    protected boolean prefixesOneOf(IPath path, IPath[] otherPaths) {
        for(int i = 0, length = otherPaths.length; i < length; i++) {
            if(path.isPrefixOf(otherPaths[i])) {
                return true;
            }
        }
        return false;
    }

    protected void pushOperation(Z8Operation operation) {
        getCurrentOperationStack().add(operation);
    }

    protected void removeAllPostAction(String actionID) {
        if(POST_ACTION_VERBOSE) {
            System.out
                    .println("(" + Thread.currentThread() + ") [JavaModelOperation.removeAllPostAction(String)] Removing actions " + actionID); //$NON-NLS-1$ //$NON-NLS-2$
        }

        Z8Operation topLevelOp = getCurrentOperationStack().get(0);

        IPostAction[] postActions = topLevelOp.m_actions;

        if(postActions == null)
            return;

        int index = m_actionsStart - 1;

        while((index = topLevelOp.firstActionWithID(actionID, index + 1)) >= 0) {
            System.arraycopy(postActions, index + 1, postActions, index, topLevelOp.m_actionsEnd - index);
            postActions[topLevelOp.m_actionsEnd--] = null;
        }
    }

    @Override
    public void run(IProgressMonitor monitor) throws CoreException {
        try {
            m_progressMonitor = monitor;
            pushOperation(this);
            try {
                executeOperation();
            }
            finally {
                if(isTopLevelOperation()) {
                    runPostActions();
                }
            }
        }
        finally {
            popOperation();
        }
    }

    public void runOperation(IProgressMonitor monitor) throws CoreException {
        IStatus status = verify();

        if(!status.isOK()) {
            throw new CoreException(status);
        }

        if(isReadOnly()) {
            run(monitor);
        }
        else {
            ResourcesPlugin.getWorkspace().run(this, getSchedulingRule(), IWorkspace.AVOID_UPDATE, monitor);
        }
    }

    protected void runPostActions() throws CoreException {
        while(m_actionsStart <= m_actionsEnd) {
            IPostAction postAction = m_actions[m_actionsStart++];
            if(POST_ACTION_VERBOSE) {
                System.out
                        .println("(" + Thread.currentThread() + ") [JavaModelOperation.runPostActions()] Running action " + postAction.getID()); //$NON-NLS-1$ //$NON-NLS-2$
            }
            postAction.run();
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected void setAttribute(Object key, Object attribute) {
        Z8Operation topLevelOp = getCurrentOperationStack().get(0);

        if(topLevelOp.m_attributes == null) {
            topLevelOp.m_attributes = new HashMap();
        }

        topLevelOp.m_attributes.put(key, attribute);
    }

    @Override
    public void setCanceled(boolean b) {
        if(m_progressMonitor != null) {
            m_progressMonitor.setCanceled(b);
        }
    }

    protected void setNested(boolean nested) {
        m_isNested = nested;
    }

    @Override
    public void setTaskName(String name) {
        if(m_progressMonitor != null) {
            m_progressMonitor.setTaskName(name);
        }
    }

    @Override
    public void subTask(String name) {
        if(m_progressMonitor != null) {
            m_progressMonitor.subTask(name);
        }
    }

    protected IStatus verify() {
        return commonVerify();
    }

    @Override
    public void worked(int work) {
        if(m_progressMonitor != null) {
            m_progressMonitor.worked(work);
            checkCanceled();
        }
    }
}
