package org.zenframework.z8.pde.refactoring.reorg;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.mapping.IResourceChangeDescriptionFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.ChangeDescriptor;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.NullChange;
import org.eclipse.ltk.core.refactoring.RefactoringChangeDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.CopyArguments;
import org.eclipse.ltk.core.refactoring.participants.MoveArguments;
import org.eclipse.ltk.core.refactoring.participants.RefactoringArguments;
import org.eclipse.ltk.core.refactoring.participants.RefactoringParticipant;
import org.eclipse.ltk.core.refactoring.participants.RefactoringProcessor;
import org.eclipse.ltk.core.refactoring.participants.ReorgExecutionLog;
import org.eclipse.ltk.core.refactoring.participants.ResourceChangeChecker;
import org.eclipse.ltk.core.refactoring.participants.SharableParticipants;
import org.eclipse.ltk.core.refactoring.participants.ValidateEditChecker;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.TextEdit;

import org.zenframework.z8.compiler.core.ILanguageElement;
import org.zenframework.z8.compiler.core.IType;
import org.zenframework.z8.compiler.workspace.CompilationUnit;
import org.zenframework.z8.compiler.workspace.Folder;
import org.zenframework.z8.compiler.workspace.Project;
import org.zenframework.z8.compiler.workspace.Resource;
import org.zenframework.z8.compiler.workspace.ResourceVisitor;
import org.zenframework.z8.compiler.workspace.Workspace;
import org.zenframework.z8.pde.Plugin;
import org.zenframework.z8.pde.refactoring.Checks;
import org.zenframework.z8.pde.refactoring.Z8RefactoringArguments;
import org.zenframework.z8.pde.refactoring.Z8RefactoringDescriptor;
import org.zenframework.z8.pde.refactoring.Z8RefactoringDescriptorComment;
import org.zenframework.z8.pde.refactoring.ScriptableRefactoring;
import org.zenframework.z8.pde.refactoring.changes.CopyFolderChange;
import org.zenframework.z8.pde.refactoring.changes.CopyResourceChange;
import org.zenframework.z8.pde.refactoring.changes.DynamicValidationStateChange;
import org.zenframework.z8.pde.refactoring.changes.MoveCompilationUnitChange;
import org.zenframework.z8.pde.refactoring.changes.MoveFolderChange;
import org.zenframework.z8.pde.refactoring.changes.MoveResourceChange;
import org.zenframework.z8.pde.refactoring.changes.TextChangeCompatibility;
import org.zenframework.z8.pde.refactoring.changes.TextChangeManager;
import org.zenframework.z8.pde.refactoring.messages.LanguageElementLabels;
import org.zenframework.z8.pde.refactoring.messages.Messages;
import org.zenframework.z8.pde.refactoring.messages.RefactoringMessages;
import org.zenframework.z8.pde.refactoring.modifications.CopyModifications;
import org.zenframework.z8.pde.refactoring.modifications.MoveModifications;
import org.zenframework.z8.pde.refactoring.modifications.RefactoringModifications;
import org.zenframework.z8.pde.refactoring.processors.move.ParentChecker;
import org.zenframework.z8.pde.refactoring.reorg.IReorgPolicy.ICopyPolicy;
import org.zenframework.z8.pde.refactoring.reorg.IReorgPolicy.IMovePolicy;
import org.zenframework.z8.pde.refactoring.search.SearchEngine;

public final class ReorgPolicyFactory {
    public static final String COPY = "org.zenframework.z8.reorg.copy";
    public static final String DELETE = "org.zenframework.z8.reorg.delete";
    public static final String MOVE = "org.zenframework.z8.reorg.move";

    private static final String NO_ID = "no_id";
    private static final String NO_POLICY = "no_policy";
    private static final String UNUSED_STRING = "unused";
    private static final String ATTRIBUTE_POLICY = "policy";
    private static final String ATTRIBUTE_LOG = "log";

    private static final String ATTRIBUTE_FILES = "files";
    private static final String ATTRIBUTE_FOLDERS = "folders";
    private static final String ATTRIBUTE_UNITS = "units";

    private static final String ATTRIBUTE_PATTERNS = "patterns";
    private static final String ATTRIBUTE_REFERENCES = "references";

    private static final String DELIMITER_RECORD = "\n";

    //	private static final String DELIMITER_ELEMENT= "\t";

    public static CompilationUnit[] getSearchScope() {
        ArrayList<CompilationUnit> result = new ArrayList<CompilationUnit>();

        for(Project project : Workspace.getInstance().getProjects()) {
            CompilationUnit[] units = project.getCompilationUnits();

            result.addAll(Arrays.asList(units));
        }

        return result.toArray(new CompilationUnit[result.size()]);
    }

    public static boolean isParentInWorkspaceOrOnDisk(CompilationUnit cu, Folder dest) {
        if(cu == null)
            return false;

        Folder cuParent = cu.getFolder();

        if(cuParent == null)
            return false;

        if(cuParent.equals(dest))
            return true;

        IResource cuResource = cu.getResource();
        IResource folderResource = dest.getResource();

        return isParentInWorkspaceOrOnDisk(cuResource, folderResource);
    }

    public static boolean isParentInWorkspaceOrOnDisk(Folder folder, Folder root) {
        if(folder == null)
            return false;

        Folder parent = folder.getFolder();

        if(parent == null)
            return false;

        if(parent.equals(root))
            return true;

        IResource resource = folder.getResource();
        IResource rootResource = root.getResource();
        return isParentInWorkspaceOrOnDisk(resource, rootResource);
    }

    public static boolean isParentInWorkspaceOrOnDisk(IResource res, IResource maybeParent) {
        if(res == null)
            return false;
        return areEqualInWorkspaceOrOnDisk(res.getParent(), maybeParent);
    }

    public static boolean areEqualInWorkspaceOrOnDisk(IResource r1, IResource r2) {
        if(r1 == null || r2 == null)
            return false;

        if(r1.equals(r2))
            return true;

        URI r1Location = r1.getLocationURI();
        URI r2Location = r2.getLocationURI();

        if(r1Location == null || r2Location == null)
            return false;

        return r1Location.equals(r2Location);
    }

    private static boolean isOneOf(Folder root, Folder[] folders) {
        for(int i = 0; i < folders.length; i++) {
            Folder fragment = folders[i];
            if(fragment == root)
                return true;
        }
        return false;
    }

    private static boolean isParentOfAny(Folder root, Folder[] folders) {
        for(int i = 0; i < folders.length; i++) {
            Folder folder = folders[i];
            if(isParentInWorkspaceOrOnDisk(folder, root) || isParentInWorkspaceOrOnDisk(root, folder))
                return true;
        }
        return false;
    }

    public static boolean containsLinkedResources(IResource[] resources) {
        for(int i = 0; i < resources.length; i++) {
            if(resources[i] != null && resources[i].isLinked())
                return true;
        }
        return false;
    }

    public static boolean containsLinkedResources(Resource[] resources) {
        for(int i = 0; i < resources.length; i++) {
            if(resources[i] != null && resources[i].getResource().isLinked())
                return true;
        }
        return false;
    }

    private static boolean containsNull(Object[] objects) {
        for(int i = 0; i < objects.length; i++) {
            if(objects[i] == null)
                return true;
        }
        return false;
    }

    private static final class ActualSelectionComputer {
        private final ILanguageElement[] m_elements;
        private final IResource[] m_resources;

        public ActualSelectionComputer(ILanguageElement[] elements, IResource[] resources) {
            m_elements = elements;
            m_resources = resources;
        }

        public ILanguageElement[] getActualElementsToReorg() throws CoreException {
            List<ILanguageElement> result = new ArrayList<ILanguageElement>();

            for(int i = 0; i < m_elements.length; i++) {
                ILanguageElement element = m_elements[i];

                if(element == null)
                    continue;

                if(element instanceof IType) {
                    IType type = (IType)element;
                    CompilationUnit cu = type.getCompilationUnit();

                    if(cu != null && type.getContainerType() == null && cu.getResource().exists() && !result.contains(cu))
                        result.add(cu);
                    else if(!result.contains(type))
                        result.add(type);
                }
                else if(!result.contains(element)) {
                    result.add(element);
                }
            }
            return result.toArray(new ILanguageElement[result.size()]);
        }

        public IResource getResource(ILanguageElement element) {
            if(element != null) {
                if(element instanceof Resource) {
                    return ((Resource)element).getResource();
                }
                return element.getCompilationUnit().getResource();
            }
            return null;
        }

        public IResource[] getActualResourcesToReorg() {
            List<IResource> result = new ArrayList<IResource>();

            for(int i = 0; i < m_resources.length; i++) {
                if(m_resources[i] == null)
                    continue;

                ILanguageElement element = Workspace.getInstance().getResource(m_resources[i]);

                if(element != null && getResource(element).isAccessible())
                    result.add(m_resources[i]);
            }
            return result.toArray(new IResource[result.size()]);
        }
    }

    private static final class NoCopyPolicy extends ReorgPolicy implements ICopyPolicy {
        @Override
        public boolean canEnable() throws CoreException {
            return false;
        }

        @Override
        public Change createChange(IProgressMonitor pm, INewNameQueries copyQueries) {
            return new NullChange();
        }

        @Override
        protected String getDescriptionPlural() {
            return UNUSED_STRING;
        }

        @Override
        protected String getDescriptionSingular() {
            return UNUSED_STRING;
        }

        @Override
        public ChangeDescriptor getDescriptor() {
            return null;
        }

        @Override
        protected String getHeaderPattern() {
            return UNUSED_STRING;
        }

        @Override
        public ILanguageElement[] getElements() {
            return new ILanguageElement[0];
        }

        @Override
        public String getPolicyId() {
            return NO_POLICY;
        }

        @Override
        protected String getProcessorId() {
            return NO_ID;
        }

        @Override
        protected String getRefactoringId() {
            return NO_ID;
        }

        @Override
        public ReorgExecutionLog getReorgExecutionLog() {
            return null;
        }

        @Override
        public IResource[] getResources() {
            return new IResource[0];
        }

        @Override
        public RefactoringStatus initialize(RefactoringArguments arguments) {
            return new RefactoringStatus();
        }

        @Override
        protected RefactoringStatus verifyDestination(ILanguageElement languageElement) throws CoreException {
            return RefactoringStatus.createFatalErrorStatus(RefactoringMessages.ReorgPolicyFactory_noCopying);
        }

        @Override
        protected RefactoringStatus verifyDestination(IResource resource) throws CoreException {
            return RefactoringStatus.createFatalErrorStatus(RefactoringMessages.ReorgPolicyFactory_noCopying);
        }
    }

    private static final class NoMovePolicy extends ReorgPolicy implements IMovePolicy {
        @Override
        public boolean canEnable() throws CoreException {
            return false;
        }

        @Override
        public Change createChange(IProgressMonitor pm) {
            return new NullChange();
        }

        @Override
        public CreateTargetExecutionLog getCreateTargetExecutionLog() {
            return new CreateTargetExecutionLog();
        }

        @Override
        public ICreateTargetQuery getCreateTargetQuery(ICreateTargetQueries createQueries) {
            return null;
        }

        @Override
        protected String getDescriptionPlural() {
            return UNUSED_STRING;
        }

        @Override
        protected String getDescriptionSingular() {
            return UNUSED_STRING;
        }

        @Override
        public ChangeDescriptor getDescriptor() {
            return null;
        }

        @Override
        protected String getHeaderPattern() {
            return UNUSED_STRING;
        }

        @Override
        public ILanguageElement[] getElements() {
            return new ILanguageElement[0];
        }

        @Override
        public String getPolicyId() {
            return NO_POLICY;
        }

        @Override
        protected String getProcessorId() {
            return NO_ID;
        }

        @Override
        protected String getRefactoringId() {
            return NO_ID;
        }

        @Override
        public IResource[] getResources() {
            return new IResource[0];
        }

        @Override
        public RefactoringStatus initialize(RefactoringArguments arguments) {
            return new RefactoringStatus();
        }

        @Override
        public boolean isTextualMove() {
            return true;
        }

        @Override
        public Change postCreateChange(Change[] participantChanges, IProgressMonitor pm) throws CoreException {
            return null;
        }

        @Override
        public void setDestinationCheck(boolean check) {
            m_checkDestination = check;
        }

        @Override
        protected RefactoringStatus verifyDestination(ILanguageElement element) throws CoreException {
            return RefactoringStatus.createFatalErrorStatus(RefactoringMessages.ReorgPolicyFactory_noMoving);
        }

        @Override
        protected RefactoringStatus verifyDestination(IResource resource) throws CoreException {
            return RefactoringStatus.createFatalErrorStatus(RefactoringMessages.ReorgPolicyFactory_noMoving);
        }
    }

    private static abstract class ReorgPolicy implements IReorgPolicy {
        private static final String ATTRIBUTE_DESTINATION = "destination";
        private static final String ATTRIBUTE_TARGET = "target";

        protected boolean m_checkDestination = true;

        private ILanguageElement m_elementDestination;

        private IResource m_resourceDestination;

        @Override
        public boolean canChildrenBeDestinations(ILanguageElement element) {
            return true;
        }

        @Override
        public boolean canChildrenBeDestinations(IResource resource) {
            return true;
        }

        @Override
        public boolean canElementBeDestination(ILanguageElement element) {
            return true;
        }

        @Override
        public boolean canElementBeDestination(IResource resource) {
            return true;
        }

        @Override
        public boolean canEnable() throws CoreException {
            IResource[] resources = getResources();
            for(int i = 0; i < resources.length; i++) {
                IResource resource = resources[i];
                if(!resource.exists() || resource.isPhantom() || !resource.isAccessible())
                    return false;
            }

            ILanguageElement[] elements = getElements();

            for(int i = 0; i < elements.length; i++) {
                ILanguageElement element = elements[i];

                if(element instanceof Resource) {
                    IResource resource = ((Resource)element).getResource();
                    if(!resource.isAccessible())
                        return false;
                }
                else if(!element.getCompilationUnit().getResource().isAccessible()) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public boolean canEnableUpdateReferences() {
            return false;
        }

        @Override
        public boolean canUpdateQualifiedNames() {
            return false;
        }

        @Override
        public boolean canUpdateReferences() {
            return false;
        }

        @Override
        public RefactoringStatus checkFinalConditions(IProgressMonitor pm, CheckConditionsContext context,
                IReorgQueries reorgQueries) throws CoreException {
            ResourceChangeChecker checker = (ResourceChangeChecker)context.getChecker(ResourceChangeChecker.class);

            IFile[] allModifiedFiles = getAllModifiedFiles();

            RefactoringModifications modifications = getModifications();

            IResourceChangeDescriptionFactory deltaFactory = checker.getDeltaFactory();

            for(int i = 0; i < allModifiedFiles.length; i++) {
                deltaFactory.change(allModifiedFiles[i]);
            }
            if(modifications != null) {
                modifications.buildValidateEdits((ValidateEditChecker)context.getChecker(ValidateEditChecker.class));
            }
            return new RefactoringStatus();
        }

        public IFile[] getAllModifiedFiles() {
            return new IFile[0];
        }

        protected abstract String getDescriptionPlural();

        protected abstract String getDescriptionSingular();

        protected String getDestinationLabel() {
            Object destination = getElementDestination();
            if(destination == null)
                destination = getResourceDestination();
            return LanguageElementLabels.getTextLabel(destination, LanguageElementLabels.ALL_FULLY_QUALIFIED);
        }

        protected abstract String getHeaderPattern();

        @Override
        public final ILanguageElement getElementDestination() {
            return m_elementDestination;
        }

        protected RefactoringModifications getModifications() throws CoreException {
            return null;
        }

        protected abstract String getProcessorId();

        protected Map<String, Object> getRefactoringArguments(String project) {
            Map<String, Object> arguments = new HashMap<String, Object>();

            ILanguageElement element = getElementDestination();

            if(element != null) {
                arguments.put(ATTRIBUTE_DESTINATION, element);
            }
            else {
                IResource resource = getResourceDestination();

                if(resource != null)
                    arguments.put(ATTRIBUTE_TARGET, resource);
            }
            return arguments;
        }

        protected abstract String getRefactoringId();

        @Override
        public final IResource getResourceDestination() {
            return m_resourceDestination;
        }

        @Override
        public boolean getUpdateReferences() {
            return false;
        }

        @Override
        public boolean hasAllInputSet() {
            return m_elementDestination != null || m_resourceDestination != null;
        }

        @Override
        public RefactoringStatus initialize(RefactoringArguments arguments) {
            if(!(arguments instanceof Z8RefactoringArguments)) {
                return RefactoringStatus
                        .createFatalErrorStatus(RefactoringMessages.InitializableRefactoring_inacceptable_arguments);
            }

            Z8RefactoringArguments extended = (Z8RefactoringArguments)arguments;
            Object object = extended.getAttribute(ATTRIBUTE_DESTINATION);

            if(object != null) {
                if(object instanceof ILanguageElement) {
                    ILanguageElement element = (ILanguageElement)object;

                    if(m_checkDestination && !element.getCompilationUnit().getResource().exists()) {
                        return ScriptableRefactoring.createInputFatalStatus(element, getProcessorId(), getRefactoringId());
                    }

                    try {
                        return setDestination(element);
                    }
                    catch(CoreException exception) {
                        Plugin.log(exception);
                        return RefactoringStatus.createFatalErrorStatus(Messages.format(
                                RefactoringMessages.InitializableRefactoring_illegal_argument,
                                new String[] { Z8RefactoringDescriptor.ATTRIBUTE_INPUT }));
                    }
                }

                IResource resource = (IResource)object;

                if(resource == null || (m_checkDestination && !resource.exists())) {
                    return ScriptableRefactoring.createInputFatalStatus(resource, getProcessorId(), getRefactoringId());
                }

                try {
                    return setDestination(resource);
                }
                catch(CoreException exception) {
                    Plugin.log(exception);
                    return RefactoringStatus.createFatalErrorStatus(Messages.format(
                            RefactoringMessages.InitializableRefactoring_illegal_argument,
                            new String[] { Z8RefactoringDescriptor.ATTRIBUTE_INPUT }));
                }
            }

            object = extended.getAttribute(ATTRIBUTE_TARGET);

            if(object != null) {
                IResource resource = (IResource)object;

                if(resource == null || (m_checkDestination && !resource.exists())) {
                    return ScriptableRefactoring.createInputFatalStatus(resource, getProcessorId(), getRefactoringId());
                }

                try {
                    return setDestination(resource);
                }
                catch(CoreException exception) {
                    Plugin.log(exception);
                    return RefactoringStatus.createFatalErrorStatus(Messages.format(
                            RefactoringMessages.InitializableRefactoring_illegal_argument,
                            new String[] { Z8RefactoringDescriptor.ATTRIBUTE_INPUT }));
                }
            }

            return RefactoringStatus.createFatalErrorStatus(Messages
                    .format(RefactoringMessages.InitializableRefactoring_argument_not_exist,
                            Z8RefactoringDescriptor.ATTRIBUTE_INPUT));
        }

        @Override
        public final RefactoringParticipant[] loadParticipants(RefactoringStatus status, RefactoringProcessor processor,
                String[] natures, SharableParticipants shared) throws CoreException {
            RefactoringModifications modifications = getModifications();

            if(modifications != null) {
                return modifications.loadParticipants(status, processor, natures, shared);
            }
            else {
                return new RefactoringParticipant[0];
            }
        }

        @Override
        public final RefactoringStatus setDestination(ILanguageElement destination) throws CoreException {
            m_elementDestination = null;
            m_resourceDestination = null;
            m_elementDestination = destination;
            return verifyDestination(destination);
        }

        @Override
        public final RefactoringStatus setDestination(IResource destination) throws CoreException {
            m_elementDestination = null;
            m_resourceDestination = null;
            m_resourceDestination = destination;
            return verifyDestination(destination);
        }

        @Override
        public void setUpdateReferences(boolean update) {}

        protected abstract RefactoringStatus verifyDestination(ILanguageElement destination) throws CoreException;

        protected abstract RefactoringStatus verifyDestination(IResource destination) throws CoreException;
    }

    private static abstract class FilesFoldersAndCusReorgPolicy extends ReorgPolicy {
        protected static final int ONLY_CUS = 2;
        protected static final int ONLY_FILES = 1;
        protected static final int ONLY_FOLDERS = 0;

        private static IContainer getAsContainer(IResource resDest) {
            if(resDest instanceof IContainer)
                return (IContainer)resDest;
            if(resDest instanceof IFile)
                return ((IFile)resDest).getParent();
            return null;
        }

        private CompilationUnit[] m_compilationUnits;
        private IFile[] m_files;
        private Folder[] m_folders;

        public FilesFoldersAndCusReorgPolicy(IFile[] files, Folder[] folders, CompilationUnit[] compilationUnits) {
            m_files = files;
            m_folders = folders;
            m_compilationUnits = compilationUnits;
        }

        @Override
        public boolean canChildrenBeDestinations(ILanguageElement element) {
            return element instanceof Folder;
        }

        @Override
        public boolean canChildrenBeDestinations(IResource resource) {
            return resource instanceof IContainer;
        }

        @Override
        public boolean canElementBeDestination(ILanguageElement element) {
            return element instanceof Folder;
        }

        @Override
        public boolean canElementBeDestination(IResource resource) {
            return resource instanceof IProject || resource instanceof IFolder;
        }

        @Override
        public RefactoringStatus checkFinalConditions(IProgressMonitor pm, CheckConditionsContext context,
                IReorgQueries reorgQueries) throws CoreException {
            RefactoringStatus status = super.checkFinalConditions(pm, context, reorgQueries);
            confirmOverwriting(reorgQueries);
            return status;
        }

        private void confirmOverwriting(IReorgQueries reorgQueries) {
            OverwriteHelper helper = new OverwriteHelper();
            helper.setFiles(m_files);
            helper.setFolders(m_folders);
            helper.setCus(m_compilationUnits);

            Folder destination = getDestinationAsFolder();

            if(destination != null) {
                helper.confirmOverwriting(reorgQueries, destination);
            }
            else {
                IContainer destinationAsContainer = getDestinationAsContainer();
                if(destinationAsContainer != null)
                    helper.confirmOverwriting(reorgQueries, destinationAsContainer);
            }
            m_files = helper.getFilesWithoutUnconfirmedOnes();
            m_folders = helper.getFoldersWithoutUnconfirmedOnes();
            m_compilationUnits = helper.getCusWithoutUnconfirmedOnes();
        }

        protected Z8RefactoringDescriptor createRefactoringDescriptor(final Z8RefactoringDescriptorComment comment,
                final Map<String, Object> arguments, final String description, final String project, int flags) {
            return new Z8RefactoringDescriptor(getProcessorId(), project, description, comment.asString(), arguments, flags);
        }

        protected final int getContentKind() {
            final int length = m_compilationUnits.length + m_files.length + m_folders.length;
            if(length == m_compilationUnits.length)
                return ONLY_CUS;
            else if(length == m_files.length)
                return ONLY_FILES;
            else if(length == m_folders.length)
                return ONLY_FOLDERS;
            return -1;
        }

        protected final CompilationUnit[] getCompilationUnits() {
            return m_compilationUnits;
        }

        @Override
        public final ChangeDescriptor getDescriptor() {
            final Map<String, Object> arguments = new HashMap<String, Object>();

            final int length = m_files.length + m_folders.length + m_compilationUnits.length;

            final String description = length == 1 ? getDescriptionSingular() : getDescriptionPlural();

            final IProject resource = getSingleProject();

            final String project = resource != null ? resource.getName() : null;
            final String header = Messages.format(getHeaderPattern(), new String[] { String.valueOf(length),
                    getDestinationLabel() });

            int flags = RefactoringDescriptor.STRUCTURAL_CHANGE | RefactoringDescriptor.MULTI_CHANGE;

            final Z8RefactoringDescriptorComment comment = new Z8RefactoringDescriptorComment(this, header);
            final Z8RefactoringDescriptor descriptor = createRefactoringDescriptor(comment, arguments, description, project,
                    flags);

            arguments.put(ATTRIBUTE_POLICY, getPolicyId());
            arguments.put(ATTRIBUTE_FILES, m_files);
            arguments.put(ATTRIBUTE_FOLDERS, m_folders);
            arguments.put(ATTRIBUTE_UNITS, m_compilationUnits);
            arguments.putAll(getRefactoringArguments(project));
            return new RefactoringChangeDescriptor(descriptor);
        }

        protected final IContainer getDestinationAsContainer() {
            IResource resDest = getResourceDestination();

            if(resDest != null)
                return getAsContainer(resDest);

            ILanguageElement destination = getElementDestination();

            return getAsContainer(((Resource)destination).getResource());
        }

        protected final Folder getDestinationAsFolder() {
            Folder folder = getDestinationAsFolder(getElementDestination());

            if(folder != null)
                return folder;

            return getResourceDestinationAsFolder(getResourceDestination());
        }

        protected final ILanguageElement getDestinationContainerAsLanguageElement() {
            if(getElementDestination() != null)
                return getElementDestination();

            IContainer destinationAsContainer = getDestinationAsContainer();

            if(destinationAsContainer == null)
                return null;

            Resource resource = Workspace.getInstance().getResource(destinationAsContainer);

            if(resource != null && resource.getResource().exists())
                return resource;

            return null;
        }

        protected final IFile[] getFiles() {
            return m_files;
        }

        protected final Folder[] getFolders() {
            return m_folders;
        }

        private Folder getDestinationAsFolder(ILanguageElement dest) {
            if(dest == null)
                return null;

            if(dest instanceof Folder)
                return (Folder)dest;

            if(dest instanceof Project)
                return (Project)dest;

            return dest.getCompilationUnit().getFolder();
        }

        @Override
        public final ILanguageElement[] getElements() {
            return union(m_compilationUnits, m_folders);
        }

        private Folder getResourceDestinationAsFolder(IResource resource) {
            if(resource instanceof IFile)
                return getDestinationAsFolder(Workspace.getInstance().getResource(resource.getParent()));
            return null;
        }

        private static void addAll(ILanguageElement[] array, List<ILanguageElement> list) {
            for(int i = 0; i < array.length; i++) {
                if(!list.contains(array[i]))
                    list.add(array[i]);
            }
        }

        public static ILanguageElement[] union(ILanguageElement[] set1, ILanguageElement[] set2) {
            List<ILanguageElement> union = new ArrayList<ILanguageElement>(set1.length + set2.length);
            addAll(set1, union);
            addAll(set2, union);
            return union.toArray(new ILanguageElement[union.size()]);
        }

        @Override
        public final IResource[] getResources() {
            return m_files;
        }

        private IProject getSingleProject() {
            IProject result = null;

            for(IFile file : m_files) {
                if(result == null) {
                    result = file.getProject();
                }
                else if(!result.equals(file.getProject())) {
                    return null;
                }
            }

            for(Folder folder : m_folders) {
                if(result == null) {
                    result = folder.getResource().getProject();
                }
                else if(!result.equals(folder.getResource().getProject())) {
                    return null;
                }
            }

            for(CompilationUnit compilationUnit : m_compilationUnits) {
                if(result == null) {
                    result = compilationUnit.getResource().getProject();
                }
                else if(!result.equals(compilationUnit.getResource().getProject())) {
                    return null;
                }
            }
            return result;
        }

        @Override
        public RefactoringStatus initialize(RefactoringArguments arguments) {
            final RefactoringStatus status = new RefactoringStatus();

            if(arguments instanceof Z8RefactoringArguments) {
                final Z8RefactoringArguments extended = (Z8RefactoringArguments)arguments;

                Object value = extended.getAttribute(ATTRIBUTE_FILES);

                if(value != null) {
                    m_files = (IFile[])value;
                }
                else {
                    return RefactoringStatus.createFatalErrorStatus(Messages.format(
                            RefactoringMessages.InitializableRefactoring_argument_not_exist, ATTRIBUTE_FILES));
                }

                value = extended.getAttribute(ATTRIBUTE_FOLDERS);

                if(value != null && !"".equals(value)) {
                    m_folders = (Folder[])value;
                }
                else {
                    return RefactoringStatus.createFatalErrorStatus(Messages.format(
                            RefactoringMessages.InitializableRefactoring_argument_not_exist, ATTRIBUTE_FOLDERS));
                }

                value = extended.getAttribute(ATTRIBUTE_UNITS);

                if(value != null) {
                    m_compilationUnits = (CompilationUnit[])value;
                }
                else {
                    return RefactoringStatus.createFatalErrorStatus(Messages.format(
                            RefactoringMessages.InitializableRefactoring_argument_not_exist, ATTRIBUTE_UNITS));
                }
            }
            else {
                return RefactoringStatus
                        .createFatalErrorStatus(RefactoringMessages.InitializableRefactoring_inacceptable_arguments);
            }

            status.merge(super.initialize(arguments));

            return status;
        }

        private boolean isChildOfOrEqualToAnyFolder(IResource resource) {
            for(Folder folder : m_folders) {
                if(folder.getResource().equals(resource) || ParentChecker.isDescendantOf(resource, folder))
                    return true;
            }
            return false;
        }

        @Override
        protected RefactoringStatus verifyDestination(ILanguageElement element) throws CoreException {
            if(!m_checkDestination)
                return new RefactoringStatus();

            if(!(element instanceof Resource)) {
                return RefactoringStatus.createFatalErrorStatus(RefactoringMessages.ReorgPolicyFactory_cannot);
            }

            Resource resource = (Resource)element;

            if(!resource.getResource().exists())
                return RefactoringStatus.createFatalErrorStatus(RefactoringMessages.ReorgPolicyFactory_doesnotexist0);

            if(!resource.getResource().isAccessible())
                return RefactoringStatus.createFatalErrorStatus(RefactoringMessages.ReorgPolicyFactory_readonly);

            if(element instanceof Workspace)
                return RefactoringStatus.createFatalErrorStatus(RefactoringMessages.ReorgPolicyFactory_workspace);

            if(element instanceof CompilationUnit && element.getCompilationUnit().containsParseError())
                return RefactoringStatus.createFatalErrorStatus(RefactoringMessages.ReorgPolicyFactory_structure);

            IContainer destinationAsContainer = getDestinationAsContainer();

            if(destinationAsContainer == null || isChildOfOrEqualToAnyFolder(destinationAsContainer))
                return RefactoringStatus.createFatalErrorStatus(RefactoringMessages.ReorgPolicyFactory_not_this_resource);

            //			if(containsLinkedResources() && !true/*ReorgUtils.canBeDestinationForLinkedResources(javaElement)*/)
            //				return RefactoringStatus.createFatalErrorStatus(RefactoringMessages.ReorgPolicyFactory_linked);

            return new RefactoringStatus();
        }

        @Override
        protected RefactoringStatus verifyDestination(IResource resource) throws CoreException {
            if(!resource.exists() || resource.isPhantom())
                return RefactoringStatus.createFatalErrorStatus(RefactoringMessages.ReorgPolicyFactory_phantom);

            if(!resource.isAccessible())
                return RefactoringStatus.createFatalErrorStatus(RefactoringMessages.ReorgPolicyFactory_inaccessible);

            if(resource.getType() == IResource.ROOT)
                return RefactoringStatus.createFatalErrorStatus(RefactoringMessages.ReorgPolicyFactory_not_this_resource);

            if(isChildOfOrEqualToAnyFolder(resource))
                return RefactoringStatus.createFatalErrorStatus(RefactoringMessages.ReorgPolicyFactory_not_this_resource);

            //			if(containsLinkedResources() && !true/*ReorgUtils.canBeDestinationForLinkedResources(resource)*/)
            //				return RefactoringStatus.createFatalErrorStatus(RefactoringMessages.ReorgPolicyFactory_linked);

            return new RefactoringStatus();
        }
    }

    private static abstract class FoldersReorgPolicy extends ReorgPolicy {
        private Folder[] m_folders;

        public FoldersReorgPolicy(Folder[] folders) {
            m_folders = folders;
        }

        @Override
        public boolean canChildrenBeDestinations(ILanguageElement element) {
            return element instanceof Folder;
        }

        @Override
        public boolean canChildrenBeDestinations(IResource resource) {
            return false;
        }

        @Override
        public boolean canElementBeDestination(ILanguageElement element) {
            return element instanceof Project || element instanceof Folder;
        }

        @Override
        public boolean canElementBeDestination(IResource resource) {
            return false;
        }

        @Override
        public boolean canEnable() throws CoreException {
            for(int i = 0; i < m_folders.length; i++) {
                if(!m_folders[i].getResource().isAccessible())
                    return false;
            }

            if(containsLinkedResources(m_folders))
                return false;

            return true;
        }

        @Override
        public RefactoringStatus checkFinalConditions(IProgressMonitor pm, CheckConditionsContext context,
                IReorgQueries reorgQueries) throws CoreException {
            RefactoringStatus refactoringStatus = super.checkFinalConditions(pm, context, reorgQueries);
            confirmOverwriting(reorgQueries);
            return refactoringStatus;
        }

        private void confirmOverwriting(IReorgQueries reorgQueries) throws CoreException {
            OverwriteHelper helper = new OverwriteHelper();
            helper.setFolders(m_folders);
            Folder destRoot = getDestinationAsFolder();
            helper.confirmOverwriting(reorgQueries, destRoot);
            m_folders = helper.getFoldersWithoutUnconfirmedOnes();
        }

        protected Z8RefactoringDescriptor createRefactoringDescriptor(final Z8RefactoringDescriptorComment comment,
                final Map<String, Object> arguments, final String description, final String project, int flags) {
            return new Z8RefactoringDescriptor(getProcessorId(), project, description, comment.asString(), arguments, flags);
        }

        @Override
        public final ChangeDescriptor getDescriptor() {
            Map<String, Object> arguments = new HashMap<String, Object>();
            String description = m_folders.length == 1 ? getDescriptionSingular() : getDescriptionPlural();
            IProject resource = getSingleProject();
            String project = resource != null ? resource.getName() : null;
            String header = Messages.format(getHeaderPattern(), new String[] { String.valueOf(m_folders.length),
                    getDestinationLabel() });
            int flags = RefactoringDescriptor.STRUCTURAL_CHANGE | RefactoringDescriptor.MULTI_CHANGE;
            Z8RefactoringDescriptorComment comment = new Z8RefactoringDescriptorComment(this, header);
            Z8RefactoringDescriptor descriptor = createRefactoringDescriptor(comment, arguments, description, project, flags);
            arguments.put(ATTRIBUTE_POLICY, getPolicyId());
            arguments.put(ATTRIBUTE_FOLDERS, m_folders);
            arguments.putAll(getRefactoringArguments(project));
            return new RefactoringChangeDescriptor(descriptor);
        }

        protected Folder getDestinationAsFolder() throws CoreException {
            return getDestinationAsFolder(getElementDestination());
        }

        private Folder getDestinationAsFolder(ILanguageElement element) throws CoreException {
            if(element instanceof Folder)
                return (Folder)element;

            return null;
        }

        @Override
        public ILanguageElement[] getElements() {
            return m_folders;
        }

        protected Folder[] getFolders() {
            return m_folders;
        }

        @Override
        public IResource[] getResources() {
            return new IResource[0];
        }

        private IProject getSingleProject() {
            IProject result = null;

            for(int index = 0; index < m_folders.length; index++) {
                if(result == null)
                    result = (IProject)m_folders[index].getProject().getResource();
                else if(!result.equals(m_folders[index].getProject().getResource()))
                    return null;
            }
            return result;
        }

        @Override
        public RefactoringStatus initialize(RefactoringArguments arguments) {
            final RefactoringStatus status = new RefactoringStatus();

            if(arguments instanceof Z8RefactoringArguments) {
                final Z8RefactoringArguments extended = (Z8RefactoringArguments)arguments;

                Folder[] value = (Folder[])extended.getAttribute(ATTRIBUTE_FOLDERS);

                if(value == null) {
                    return RefactoringStatus.createFatalErrorStatus(Messages.format(
                            RefactoringMessages.InitializableRefactoring_argument_not_exist, ATTRIBUTE_FOLDERS));
                }

                m_folders = value;
            }
            else
                return RefactoringStatus
                        .createFatalErrorStatus(RefactoringMessages.InitializableRefactoring_inacceptable_arguments);

            status.merge(super.initialize(arguments));

            return status;
        }

        @Override
        protected RefactoringStatus verifyDestination(ILanguageElement element) throws CoreException {
            if(!m_checkDestination)
                return new RefactoringStatus();

            if(element instanceof Resource && !((Resource)element).getResource().exists())
                return RefactoringStatus.createFatalErrorStatus(RefactoringMessages.ReorgPolicyFactory_cannot1);

            return new RefactoringStatus();
        }

        @Override
        protected RefactoringStatus verifyDestination(IResource resource) {
            return RefactoringStatus.createFatalErrorStatus(RefactoringMessages.ReorgPolicyFactory_folders);
        }
    }

    private static final class MoveFoldersPolicy extends FoldersReorgPolicy implements IMovePolicy {
        private static final String POLICY_MOVE_FOLDERS = "org.zenframework.z8.moveFolders";

        private CreateTargetExecutionLog fCreateTargetExecutionLog = new CreateTargetExecutionLog();
        private MoveModifications m_modifications;

        MoveFoldersPolicy(Folder[] folders) {
            super(folders);
        }

        @Override
        public RefactoringStatus checkFinalConditions(IProgressMonitor pm, CheckConditionsContext context,
                IReorgQueries reorgQueries) throws CoreException {
            RefactoringStatus status = super.checkFinalConditions(pm, context, reorgQueries);
            confirmMovingReadOnly(reorgQueries);
            return status;
        }

        private void confirmMovingReadOnly(IReorgQueries reorgQueries) throws CoreException {
            //			if(!ReadOnlyResourceFinder.confirmMoveOfReadOnlyElements(getElements(), getResources(), reorgQueries))
            //				throw new OperationCanceledException();
        }

        private Change createChange(Folder folder, Folder destination) {
            return new MoveFolderChange(folder, destination);
        }

        @Override
        public Change createChange(IProgressMonitor pm) throws CoreException {
            Folder[] folders = getFolders();

            pm.beginTask("", folders.length);

            CompositeChange result = new DynamicValidationStateChange(RefactoringMessages.ReorgPolicy_move_folder);
            result.markAsSynthetic();

            Folder destination = getDestinationAsFolder();

            for(Folder folder : folders) {
                result.add(createChange(folder, destination));
                pm.worked(1);

                if(pm.isCanceled())
                    throw new OperationCanceledException();
            }

            pm.done();
            return result;
        }

        @Override
        public CreateTargetExecutionLog getCreateTargetExecutionLog() {
            return fCreateTargetExecutionLog;
        }

        @Override
        public ICreateTargetQuery getCreateTargetQuery(ICreateTargetQueries createQueries) {
            return null;
        }

        @Override
        protected String getDescriptionPlural() {
            return RefactoringMessages.ReorgPolicyFactory_move_folders_plural;
        }

        @Override
        protected String getDescriptionSingular() {
            return RefactoringMessages.ReorgPolicyFactory_move_folders_singular;
        }

        @Override
        protected String getHeaderPattern() {
            return RefactoringMessages.ReorgPolicyFactory_move_folders_header;
        }

        @Override
        protected RefactoringModifications getModifications() throws CoreException {
            if(m_modifications != null)
                return m_modifications;

            m_modifications = new MoveModifications();

            boolean updateReferences = canUpdateReferences() && getUpdateReferences();

            Folder[] folders = getFolders();
            Folder destination = getDestinationAsFolder();

            for(Folder folder : folders) {
                m_modifications.move(folder, new MoveArguments(destination, updateReferences));
            }
            return m_modifications;
        }

        @Override
        public String getPolicyId() {
            return POLICY_MOVE_FOLDERS;
        }

        @Override
        protected String getProcessorId() {
            return MOVE;
        }

        @Override
        protected String getRefactoringId() {
            return MOVE;
        }

        @Override
        public boolean isTextualMove() {
            return false;
        }

        @Override
        public Change postCreateChange(Change[] participantChanges, IProgressMonitor pm) throws CoreException {
            return null;
        }

        @Override
        public void setDestinationCheck(boolean check) {
            m_checkDestination = check;
        }

        @Override
        protected RefactoringStatus verifyDestination(ILanguageElement element) throws CoreException {
            RefactoringStatus superStatus = super.verifyDestination(element);
            if(superStatus.hasFatalError())
                return superStatus;

            Folder root = getDestinationAsFolder();
            Folder[] folders = getFolders();

            if(isOneOf(root, folders))
                return RefactoringStatus.createFatalErrorStatus(RefactoringMessages.ReorgPolicyFactory_folder2itself);

            if(isParentOfAny(root, getFolders()))
                return RefactoringStatus.createFatalErrorStatus(RefactoringMessages.ReorgPolicyFactory_folder2parent);

            return superStatus;
        }
    }

    private static final class MoveFilesFoldersAndCusPolicy extends FilesFoldersAndCusReorgPolicy implements IMovePolicy {
        private static final String POLICY_MOVE_RESOURCES = "org.zenframework.z8.refactoring.moveResources";

        private static Change moveCuToFolder(CompilationUnit cu, Folder dest) {
            IResource resource = cu.getResource();

            if(resource != null && resource.isLinked()) {
                return moveResourceToContainer(resource, (IContainer)dest.getResource());
            }
            return new MoveCompilationUnitChange(cu, dest);
        }

        private static Change moveFolderToFolder(Folder folder, Folder destination) {
            IResource resource = folder.getResource();

            if(resource != null && resource.isLinked()) {
                return moveResourceToContainer(resource, (IContainer)destination.getResource());
            }
            return new MoveFolderChange(folder, destination);
        }

        private static Change moveResourceToContainer(IResource resource, IContainer dest) {
            return new MoveResourceChange(resource, dest);
        }

        private TextChangeManager fChangeManager;
        private CreateTargetExecutionLog fCreateTargetExecutionLog = new CreateTargetExecutionLog();
        private String fFilePatterns;
        private MoveModifications fModifications;
        private boolean fUpdateReferences;
        private CompilationUnit[] m_references;

        MoveFilesFoldersAndCusPolicy(IFile[] files, Folder[] folders, CompilationUnit[] cus) {
            super(files, folders, cus);
            fUpdateReferences = true;
        }

        @Override
        public boolean canEnableUpdateReferences() {
            return true;
        }

        @Override
        public boolean canUpdateReferences() {
            return true;
        }

        @Override
        public RefactoringStatus checkFinalConditions(IProgressMonitor pm, CheckConditionsContext context,
                IReorgQueries reorgQueries) throws CoreException {
            try {
                pm.beginTask("", 3);

                RefactoringStatus result = new RefactoringStatus();

                //				confirmMovingReadOnly(reorgQueries);

                fChangeManager = new TextChangeManager();

                if(fUpdateReferences)
                    initializeReferences(new SubProgressMonitor(pm, 4));

                result.merge(super.checkFinalConditions(new SubProgressMonitor(pm, 1), context, reorgQueries));

                return result;
            }
            finally {
                pm.done();
            }
        }

        CompilationUnit[] getMovedCompilationUnits() {
            final Set<CompilationUnit> result = new HashSet<CompilationUnit>();

            result.addAll(Arrays.asList(getCompilationUnits()));

            for(Folder folder : getFolders()) {
                folder.iterate(new ResourceVisitor() {
                    @Override
                    public boolean visit(CompilationUnit compilationUnit) {
                        result.add(compilationUnit);
                        return true;
                    }
                });
            }

            return result.toArray(new CompilationUnit[0]);
        }

        private String[] getMovedResourcesNames() {
            Set<String> result = new HashSet<String>();

            for(CompilationUnit compilationUnit : getMovedCompilationUnits()) {
                result.add(compilationUnit.getPath().removeFileExtension().lastSegment());
            }

            return result.toArray(new String[0]);
        }

        private void initializeReferences(IProgressMonitor pm) throws CoreException {
            Folder destination = getDestinationAsFolder();

            if(destination == null)
                return;

            try {
                pm.beginTask("", 16);

                pm.subTask(RefactoringMessages.MoveRefactoring_scanning_qualified_names);

                m_references = SearchEngine.search(getMovedResourcesNames(), getSearchScope(), pm);
            }
            finally {
                pm.done();
            }
        }

        //		private void confirmMovingReadOnly(IReorgQueries reorgQueries) throws CoreException
        //		{
        //			if(!ReadOnlyResourceFinder.confirmMoveOfReadOnlyElements(getElements(), getResources(), reorgQueries))
        //				throw new OperationCanceledException();
        //		}

        private Change createChange(CompilationUnit cu) {
            Folder destination = getDestinationAsFolder();

            if(destination != null)
                return moveCuToFolder(cu, destination);

            IContainer container = getDestinationAsContainer();

            if(container == null)
                return new NullChange();

            return moveResourceToContainer(cu.getResource(), container);
        }

        private Change createChange(Folder folder) {
            Folder destination = getDestinationAsFolder();

            if(destination != null)
                return moveFolderToFolder(folder, destination);

            IContainer container = getDestinationAsContainer();

            if(container == null)
                return new NullChange();

            return moveResourceToContainer(folder.getResource(), container);
        }

        @Override
        public Change createChange(IProgressMonitor pm) throws CoreException {
            if(!fUpdateReferences) {
                return createSimpleMoveChange(pm);
            }
            else {
                return createReferenceUpdatingMoveChange(pm);
            }
        }

        private Change createChange(IResource res) {
            IContainer destinationAsContainer = getDestinationAsContainer();

            if(destinationAsContainer == null)
                return new NullChange();

            if(res instanceof IFolder) {
                return new MoveFolderChange(Workspace.getInstance().getFolder(res), getDestinationAsFolder());
            }

            return new MoveResourceChange(res, destinationAsContainer);
        }

        private Change createReferenceUpdatingMoveChange(IProgressMonitor pm) throws CoreException {
            pm.beginTask("", 2);

            try {
                CompositeChange composite = new DynamicValidationStateChange(RefactoringMessages.ReorgPolicy_move);

                composite.markAsSynthetic();

                if(fChangeManager == null) {
                    fChangeManager = new TextChangeManager();
                    RefactoringStatus status = Checks.validateModifiesFiles(getAllModifiedFiles(), null);
                    if(status.hasFatalError())
                        fChangeManager = new TextChangeManager();
                }

                if(fUpdateReferences) {
                    addReferenceUpdates(fChangeManager, pm);
                }

                composite.merge(new CompositeChange(RefactoringMessages.MoveRefactoring_reorganize_elements, fChangeManager
                        .getAllChanges()));

                Change fileMove = createSimpleMoveChange(new SubProgressMonitor(pm, 1));

                if(fileMove instanceof CompositeChange) {
                    composite.merge(((CompositeChange)fileMove));
                }
                else {
                    composite.add(fileMove);
                }
                return composite;
            }
            finally {
                pm.done();
            }
        }

        private void addReferenceUpdates(TextChangeManager manager, IProgressMonitor pm) throws CoreException {
            try {
                pm.beginTask("", m_references.length);

                CompilationUnit[] movedCompilationUnits = getMovedCompilationUnits();
                for(CompilationUnit compilationUnit : m_references) {
                    addImportUpdate(manager, compilationUnit, movedCompilationUnits);
                    pm.worked(1);
                }
            }
            finally {
                pm.done();
            }
        }

        private Folder getMovedFolderFor(CompilationUnit compilationUnit) {
            for(Folder folder : getFolders()) {
                if(compilationUnit.isDescendantOf(folder))
                    return folder;
            }

            return null;
        }

        private void addImportUpdate(TextChangeManager manager, CompilationUnit compilationUnit,
                CompilationUnit[] movedCompilationUnits) throws CoreException {
            String name = RefactoringMessages.MoveRefactoring_update_imports;

            MultiTextEdit textEdit = new MultiTextEdit();

            IPath destinationPath = getDestinationAsFolder().getPath();

            for(CompilationUnit movedResource : movedCompilationUnits) {
                int segmentsToRemove = movedResource.getPath().segmentCount() - 1;

                Folder movedFolder = getMovedFolderFor(movedResource);

                if(movedFolder != null) {
                    segmentsToRemove = movedFolder.getPath().removeLastSegments(1).segmentCount();
                }

                IPath oldPath = movedResource.getPath().removeFileExtension();
                IPath newPath = destinationPath.append(movedResource.getPath().removeFileExtension()
                        .removeFirstSegments(segmentsToRemove));
                compilationUnit.replaceImport(textEdit, oldPath, newPath);
            }

            TextEdit[] children = textEdit.removeChildren();

            for(TextEdit edit : children) {
                TextChangeCompatibility.addTextEdit(manager.get(compilationUnit), name, edit);
            }
        }

        private Change createSimpleMoveChange(IProgressMonitor pm) {
            CompositeChange result = new DynamicValidationStateChange(RefactoringMessages.ReorgPolicy_move);
            result.markAsSynthetic();

            IFile[] files = getFiles();
            Folder[] folders = getFolders();
            CompilationUnit[] cus = getCompilationUnits();

            pm.beginTask("", files.length + folders.length + cus.length);

            for(int i = 0; i < files.length; i++) {
                result.add(createChange(files[i]));
                pm.worked(1);
            }

            if(pm.isCanceled())
                throw new OperationCanceledException();

            for(int i = 0; i < folders.length; i++) {
                result.add(createChange(folders[i]));
                pm.worked(1);
            }

            if(pm.isCanceled())
                throw new OperationCanceledException();

            for(int i = 0; i < cus.length; i++) {
                result.add(createChange(cus[i]));
                pm.worked(1);
            }

            pm.done();

            return result;
        }

        public static IFile[] getFiles(CompilationUnit[] cus) {
            List<IFile> files = new ArrayList<IFile>(cus.length);

            for(int i = 0; i < cus.length; i++) {
                IResource resource = cus[i].getResource();

                if(resource != null && resource.getType() == IResource.FILE)
                    files.add((IFile)resource);
            }
            return files.toArray(new IFile[files.size()]);
        }

        @Override
        public IFile[] getAllModifiedFiles() {
            Set<IFile> result = new HashSet<IFile>();
            result.addAll(Arrays.asList(getFiles(fChangeManager.getAllCompilationUnits())));
            if(getDestinationAsFolder() != null && getUpdateReferences())
                result.addAll(Arrays.asList(getFiles(getCompilationUnits())));
            return result.toArray(new IFile[result.size()]);
        }

        private Object getCommonParent() {
            return new ParentChecker(getResources(), getElements()).getCommonParent();
        }

        @Override
        public CreateTargetExecutionLog getCreateTargetExecutionLog() {
            return fCreateTargetExecutionLog;
        }

        @Override
        public ICreateTargetQuery getCreateTargetQuery(ICreateTargetQueries createQueries) {
            return createQueries.createNewFolderQuery();
        }

        @Override
        protected String getDescriptionPlural() {
            final int kind = getContentKind();
            switch(kind) {
            case ONLY_FOLDERS:
                return RefactoringMessages.ReorgPolicyFactory_move_folders;
            case ONLY_FILES:
                return RefactoringMessages.ReorgPolicyFactory_move_files;
            case ONLY_CUS:
                return RefactoringMessages.ReorgPolicyFactory_move_compilation_units;
            }
            return RefactoringMessages.ReorgPolicyFactory_move_description_plural;
        }

        @Override
        protected String getDescriptionSingular() {
            final int kind = getContentKind();
            switch(kind) {
            case ONLY_FOLDERS:
                return RefactoringMessages.ReorgPolicyFactory_move_folder;
            case ONLY_FILES:
                return RefactoringMessages.ReorgPolicyFactory_move_file;
            case ONLY_CUS:
                return RefactoringMessages.ReorgPolicyFactory_move_compilation_unit;
            }
            return RefactoringMessages.ReorgPolicyFactory_move_description_singular;
        }

        @Override
        protected String getHeaderPattern() {
            return RefactoringMessages.ReorgPolicyFactory_move_header;
        }

        @Override
        protected RefactoringModifications getModifications() throws CoreException {
            if(fModifications != null)
                return fModifications;

            fModifications = new MoveModifications();

            Folder destination = getDestinationAsFolder();

            boolean updateReferenes = canUpdateReferences() && getUpdateReferences();

            for(CompilationUnit compilationUnit : getCompilationUnits()) {
                fModifications.move(compilationUnit, new MoveArguments(destination, updateReferenes));
            }

            for(IFile file : getFiles()) {
                fModifications.move(file, new MoveArguments(destination, updateReferenes));
            }

            for(Folder folder : getFolders()) {
                fModifications.move(folder, new MoveArguments(destination, updateReferenes));
            }

            return fModifications;
        }

        @Override
        public String getPolicyId() {
            return POLICY_MOVE_RESOURCES;
        }

        @Override
        protected String getProcessorId() {
            return MOVE;
        }

        @Override
        protected Map<String, Object> getRefactoringArguments(String project) {
            final Map<String, Object> arguments = new HashMap<String, Object>();

            arguments.putAll(super.getRefactoringArguments(project));

            if(fFilePatterns != null && !"".equals(fFilePatterns))
                arguments.put(ATTRIBUTE_PATTERNS, fFilePatterns);

            arguments.put(ATTRIBUTE_REFERENCES, Boolean.valueOf(fUpdateReferences).toString());

            return arguments;
        }

        @Override
        protected String getRefactoringId() {
            return MOVE;
        }

        @Override
        public boolean getUpdateReferences() {
            return fUpdateReferences;
        }

        @Override
        public boolean hasAllInputSet() {
            return super.hasAllInputSet() && !canUpdateReferences() && !canUpdateQualifiedNames();
        }

        @Override
        public RefactoringStatus initialize(RefactoringArguments arguments) {
            if(arguments instanceof Z8RefactoringArguments) {
                final Z8RefactoringArguments extended = (Z8RefactoringArguments)arguments;

                final String patterns = (String)extended.getAttribute(ATTRIBUTE_PATTERNS);

                if(patterns != null && !"".equals(patterns))
                    fFilePatterns = patterns;
                else
                    fFilePatterns = "";

                final String references = (String)extended.getAttribute(ATTRIBUTE_REFERENCES);

                if(references != null) {
                    fUpdateReferences = Boolean.valueOf(references).booleanValue();
                }
                else
                    return RefactoringStatus.createFatalErrorStatus(Messages.format(
                            RefactoringMessages.InitializableRefactoring_argument_not_exist, ATTRIBUTE_REFERENCES));
            }
            else
                return RefactoringStatus
                        .createFatalErrorStatus(RefactoringMessages.InitializableRefactoring_inacceptable_arguments);
            return super.initialize(arguments);
        }

        @Override
        public boolean isTextualMove() {
            return false;
        }

        @Override
        public Change postCreateChange(Change[] participantChanges, IProgressMonitor pm) throws CoreException {
            return null;
        }

        @Override
        public void setDestinationCheck(boolean check) {
            m_checkDestination = check;
        }

        @Override
        public void setUpdateReferences(boolean update) {
            fUpdateReferences = update;
        }

        @Override
        protected RefactoringStatus verifyDestination(ILanguageElement destination) throws CoreException {
            RefactoringStatus superStatus = super.verifyDestination(destination);

            if(superStatus.hasFatalError())
                return superStatus;

            Object commonParent = new ParentChecker(getResources(), getElements()).getCommonParent();

            if(destination.equals(commonParent))
                return RefactoringStatus.createFatalErrorStatus(RefactoringMessages.ReorgPolicyFactory_parent);

            IContainer destinationAsContainer = getDestinationAsContainer();

            if(destinationAsContainer != null && destinationAsContainer.equals(commonParent))
                return RefactoringStatus.createFatalErrorStatus(RefactoringMessages.ReorgPolicyFactory_parent);

            Folder destinationAsFolder = getDestinationAsFolder();

            if(destinationAsFolder != null && destinationAsFolder.equals(commonParent))
                return RefactoringStatus.createFatalErrorStatus(RefactoringMessages.ReorgPolicyFactory_parent);

            return superStatus;
        }

        @Override
        protected RefactoringStatus verifyDestination(IResource destination) throws CoreException {
            RefactoringStatus superStatus = super.verifyDestination(destination);

            if(superStatus.hasFatalError())
                return superStatus;

            Object commonParent = getCommonParent();

            if(destination.equals(commonParent))
                return RefactoringStatus.createFatalErrorStatus(RefactoringMessages.ReorgPolicyFactory_parent);

            IContainer destinationAsContainer = getDestinationAsContainer();

            if(destinationAsContainer != null && destinationAsContainer.equals(commonParent))
                return RefactoringStatus.createFatalErrorStatus(RefactoringMessages.ReorgPolicyFactory_parent);

            ILanguageElement destinationContainerAsFolder = getDestinationContainerAsLanguageElement();

            if(destinationContainerAsFolder != null && destinationContainerAsFolder.equals(commonParent))
                return RefactoringStatus.createFatalErrorStatus(RefactoringMessages.ReorgPolicyFactory_parent);

            return superStatus;
        }
    }

    private static final class NewNameProposer {
        private static boolean isNewNameOk(IContainer container, String newName) {
            return container.findMember(newName) == null;
        }

        private final Set<String> fAutoGeneratedNewNames = new HashSet<String>(2);

        public String createNewName(IResource res, IContainer destination) {
            if(isNewNameOk(destination, res.getName()))
                return null;

            if(!isParentInWorkspaceOrOnDisk(res, destination))
                return null;

            int i = 1;

            while(true) {
                String newName;

                if(i == 1)
                    newName = Messages.format(RefactoringMessages.CopyRefactoring_resource_copyOf1, res.getName());
                else
                    newName = Messages.format(RefactoringMessages.CopyRefactoring_resource_copyOfMore,
                            new String[] { String.valueOf(i), res.getName() });

                if(isNewNameOk(destination, newName) && !fAutoGeneratedNewNames.contains(newName)) {
                    fAutoGeneratedNewNames.add(newName);
                    return newName;
                }
                i++;
            }
        }
    }

    private static final class CopyFilesFoldersAndCusPolicy extends FilesFoldersAndCusReorgPolicy implements ICopyPolicy {
        private static final String POLICY_COPY_RESOURCE = "org.zenframework.z8.copyResources";

        /*		private static Change copyCuToPackage(CompilationUnit cu, Folder dest, NewNameProposer nameProposer, INewNameQueries copyQueries)
        		{
        			IResource res = cu.getResource();
        			
        			if(res != null && res.isLinked())
        			{
        				if(dest.getResource() instanceof IContainer)
        					return copyFileToContainer(cu, (IContainer)dest.getResource(), nameProposer, copyQueries);
        			}
        			
        			String newName = nameProposer.createNewName(cu, dest);
        			
        			Change simpleCopy = new CopyCompilationUnitChange(cu, dest, copyQueries.createStaticQuery(newName));
        			
        			if(newName == null || newName.equals(cu.getName()))
        				return simpleCopy;
        			
        			try
        			{
        				IPath newPath = cu.getResource().getParent().getFullPath().append(getRenamedCUName(cu, newName));
        				INewNameQuery nameQuery = copyQueries.createNewCompilationUnitNameQuery(cu, newName);
        				return new CreateCopyOfCompilationUnitChange(newPath, cu.getSource(), cu, nameQuery);
        			}
        			catch(CoreException e)
        			{
        				return simpleCopy;
        			}
        		}
        */
        private static Change copyFileToContainer(CompilationUnit cu, IContainer dest, NewNameProposer nameProposer,
                INewNameQueries copyQueries) {
            IResource resource = cu.getResource();
            return createCopyResourceChange(resource, nameProposer, copyQueries, dest);
        }

        private static Change createCopyResourceChange(IResource resource, NewNameProposer nameProposer,
                INewNameQueries copyQueries, IContainer destination) {
            if(resource == null || destination == null)
                return new NullChange();

            INewNameQuery nameQuery;

            String name = nameProposer.createNewName(resource, destination);

            if(name == null)
                nameQuery = copyQueries.createNullQuery();
            else
                nameQuery = copyQueries.createNewResourceNameQuery(resource, name);

            return new CopyResourceChange(resource, destination, nameQuery);
        }

        private static Change createCopyFolderChange(Folder folder, NewNameProposer nameProposer,
                INewNameQueries copyQueries, Folder destination) {
            if(folder == null || destination == null)
                return new NullChange();

            INewNameQuery nameQuery;

            String name = nameProposer.createNewName(folder.getResource(), (IContainer)destination.getResource());

            if(name == null)
                nameQuery = copyQueries.createNullQuery();
            else
                nameQuery = copyQueries.createNewResourceNameQuery(folder.getResource(), name);

            return new CopyFolderChange(folder, destination, nameQuery);
        }

        private CopyModifications fModifications;
        private ReorgExecutionLog fReorgExecutionLog;

        CopyFilesFoldersAndCusPolicy(IFile[] files, Folder[] folders, CompilationUnit[] cus) {
            super(files, folders, cus);
        }

        private Change createChange(CompilationUnit unit, NewNameProposer nameProposer, INewNameQueries copyQueries) {
            //			if(pack != null)
            //				return copyCuToPackage(unit, getDestinationAsFolder(), nameProposer, copyQueries);

            return copyFileToContainer(unit, getDestinationAsContainer(), nameProposer, copyQueries);
        }

        @Override
        public Change createChange(IProgressMonitor pm, INewNameQueries copyQueries) {
            IFile[] file = getFiles();
            Folder[] folders = getFolders();
            CompilationUnit[] cus = getCompilationUnits();

            pm.beginTask("", cus.length + file.length + folders.length);

            NewNameProposer nameProposer = new NewNameProposer();

            CompositeChange composite = new DynamicValidationStateChange(RefactoringMessages.ReorgPolicy_copy);

            composite.markAsSynthetic();

            for(int i = 0; i < cus.length; i++) {
                composite.add(createChange(cus[i], nameProposer, copyQueries));
                pm.worked(1);
            }

            if(pm.isCanceled())
                throw new OperationCanceledException();

            for(int i = 0; i < file.length; i++) {
                composite.add(createChange(file[i], nameProposer, copyQueries));
                pm.worked(1);
            }

            if(pm.isCanceled())
                throw new OperationCanceledException();

            for(int i = 0; i < folders.length; i++) {
                composite.add(createChange(folders[i], nameProposer, copyQueries));
                pm.worked(1);
            }
            pm.done();
            return composite;
        }

        private Change createChange(IResource resource, NewNameProposer nameProposer, INewNameQueries copyQueries) {
            IContainer dest = getDestinationAsContainer();
            return createCopyResourceChange(resource, nameProposer, copyQueries, dest);
        }

        private Change createChange(Folder folder, NewNameProposer nameProposer, INewNameQueries copyQueries) {
            return createCopyFolderChange(folder, nameProposer, copyQueries, getDestinationAsFolder());
        }

        @Override
        protected String getDescriptionPlural() {
            final int kind = getContentKind();
            switch(kind) {
            case ONLY_FOLDERS:
                return RefactoringMessages.ReorgPolicyFactory_copy_folders;
            case ONLY_FILES:
                return RefactoringMessages.ReorgPolicyFactory_copy_files;
            case ONLY_CUS:
                return RefactoringMessages.ReorgPolicyFactory_copy_compilation_units;
            }
            return RefactoringMessages.ReorgPolicyFactory_copy_description_plural;
        }

        @Override
        protected String getDescriptionSingular() {
            final int kind = getContentKind();
            switch(kind) {
            case ONLY_FOLDERS:
                return RefactoringMessages.ReorgPolicyFactory_copy_folder;
            case ONLY_FILES:
                return RefactoringMessages.ReorgPolicyFactory_copy_file;
            case ONLY_CUS:
                return RefactoringMessages.ReorgPolicyFactory_copy_compilation_unit;
            }
            return RefactoringMessages.ReorgPolicyFactory_copy_description_singular;
        }

        private Object getDestination() {
            Object result = getDestinationAsFolder();
            if(result != null)
                return result;
            return getDestinationAsContainer();
        }

        @Override
        protected String getHeaderPattern() {
            return RefactoringMessages.ReorgPolicyFactory_copy_header;
        }

        @Override
        protected RefactoringModifications getModifications() throws CoreException {
            if(fModifications != null)
                return fModifications;

            fModifications = new CopyModifications();
            fReorgExecutionLog = new ReorgExecutionLog();

            CopyArguments jArgs = new CopyArguments(getDestination(), fReorgExecutionLog);
            CopyArguments rArgs = new CopyArguments(getDestinationAsContainer(), fReorgExecutionLog);
            CompilationUnit[] cus = getCompilationUnits();

            for(int i = 0; i < cus.length; i++) {
                fModifications.copy(cus[i], jArgs, rArgs);
            }

            IResource[] resources = getFiles();

            for(int i = 0; i < resources.length; i++) {
                fModifications.copy(resources[i], rArgs);
            }
            return fModifications;
        }

        @Override
        public String getPolicyId() {
            return POLICY_COPY_RESOURCE;
        }

        @Override
        protected String getProcessorId() {
            return COPY;
        }

        @Override
        protected String getRefactoringId() {
            return COPY;
        }

        @Override
        public ReorgExecutionLog getReorgExecutionLog() {
            return fReorgExecutionLog;
        }
    }

    public static IMovePolicy createMovePolicy(IResource[] resources, ILanguageElement[] elements) throws CoreException {
        return (IMovePolicy)createReorgPolicy(false, resources, elements);
    }

    public static IMovePolicy createMovePolicy(RefactoringStatus status, RefactoringArguments arguments) {
        if(arguments instanceof Z8RefactoringArguments) {
            final Z8RefactoringArguments extended = (Z8RefactoringArguments)arguments;

            final String policy = (String)extended.getAttribute(ATTRIBUTE_POLICY);

            if(policy != null && !"".equals(policy)) {
                if(MoveFilesFoldersAndCusPolicy.POLICY_MOVE_RESOURCES.equals(policy)) {
                    return new MoveFilesFoldersAndCusPolicy(null, null, null);
                }
                else if(MoveFoldersPolicy.POLICY_MOVE_FOLDERS.equals(policy)) {
                    return new MoveFoldersPolicy(null);
                }
                else
                    status.merge(RefactoringStatus.createFatalErrorStatus(Messages
                            .format(RefactoringMessages.InitializableRefactoring_illegal_argument,
                                    new String[] { ATTRIBUTE_POLICY })));
            }
            else
                status.merge(RefactoringStatus.createFatalErrorStatus(Messages.format(
                        RefactoringMessages.InitializableRefactoring_argument_not_exist, ATTRIBUTE_POLICY)));
        }
        else
            status.merge(RefactoringStatus
                    .createFatalErrorStatus(RefactoringMessages.InitializableRefactoring_inacceptable_arguments));
        return null;
    }

    private static boolean hasElementsOfType(Object[] elements, Class<? extends Object> type) {
        for(Object element : elements) {
            if(element != null && type.isInstance(element))
                return true;
        }
        return false;
    }

    private static IFile[] getFiles(IResource[] elements) {
        List<IFile> result = new ArrayList<IFile>();

        for(IResource element : elements) {
            if(element != null && element instanceof IFile)
                result.add((IFile)element);
        }
        return result.toArray(new IFile[0]);
    }

    private static Folder[] getFolders(ILanguageElement[] elements) {
        List<Folder> result = new ArrayList<Folder>();

        for(ILanguageElement element : elements) {
            if(element != null && element instanceof Folder)
                result.add((Folder)element);
        }
        return result.toArray(new Folder[0]);
    }

    private static CompilationUnit[] getCompilationUnits(ILanguageElement[] elements) {
        List<CompilationUnit> result = new ArrayList<CompilationUnit>();

        for(ILanguageElement element : elements) {
            if(element != null && element instanceof CompilationUnit)
                result.add((CompilationUnit)element);
        }
        return result.toArray(new CompilationUnit[0]);
    }

    private static IReorgPolicy createReorgPolicy(boolean copy, IResource[] selectedResources,
            ILanguageElement[] selectedElements) throws CoreException {
        IReorgPolicy NO = copy ? new NoCopyPolicy() : new NoMovePolicy();

        ActualSelectionComputer selectionComputer = new ActualSelectionComputer(selectedElements, selectedResources);

        IResource[] resources = selectionComputer.getActualResourcesToReorg();

        ILanguageElement[] elements = selectionComputer.getActualElementsToReorg();

        if((resources.length + elements.length == 0) || containsNull(resources) || containsNull(elements)
                || hasElementsOfType(elements, Project.class) || hasElementsOfType(resources, IProject.class)
                || hasElementsOfType(resources, IWorkspaceRoot.class)
                || !new ParentChecker(resources, elements).haveCommonParent()) {
            return NO;
        }

        /*		if(hasElementsOfType(elements, Folder.class))
        		{
        			if(resources.length != 0 || hasElementsNotOfType(elements, Folder.class))
        			{
        				return NO;
        			}
        			
        			if(copy)
        			{
        				return new CopyFoldersPolicy(getFolders(elements));
        			}

        			return new MoveFoldersPolicy(getFolders(elements));
        		}
        */
        if(hasElementsOfType(resources, IFolder.class) || hasElementsOfType(resources, IFile.class)
                || hasElementsOfType(elements, CompilationUnit.class)) {
            if(copy) {
                return new CopyFilesFoldersAndCusPolicy(getFiles(resources), getFolders(elements),
                        getCompilationUnits(elements));
            }

            return new MoveFilesFoldersAndCusPolicy(getFiles(resources), getFolders(elements), getCompilationUnits(elements));
        }

        return NO;
    }

    public static CreateTargetExecutionLog loadCreateTargetExecutionLog(RefactoringStatus status,
            Z8RefactoringArguments arguments) {
        CreateTargetExecutionLog log = new CreateTargetExecutionLog();

        String value = (String)arguments.getAttribute(ATTRIBUTE_LOG);

        if(value != null) {
            final StringTokenizer tokenizer = new StringTokenizer(value, DELIMITER_RECORD, false);
            while(tokenizer.hasMoreTokens()) {
                final String token = tokenizer.nextToken();
                processCreateTargetExecutionRecord(log, arguments, token);
            }
        }
        return log;
    }

    private static void processCreateTargetExecutionRecord(CreateTargetExecutionLog log, Z8RefactoringArguments arguments,
            String token) {
        /*
        		StringTokenizer tokenizer = new StringTokenizer(token, DELIMITER_ELEMENT, false);
        		
         		String value = null;
        		
        		if(tokenizer.hasMoreTokens())
        		{
        			value = tokenizer.nextToken();
        			
        			Object selection = JDTRefactoringDescriptor.handleToElement(arguments.getProject(), value, false);
        			
        			if(selection == null)
        				selection = JDTRefactoringDescriptor.handleToResource(arguments.getProject(), value);
        			
        			if(selection != null && tokenizer.hasMoreTokens())
        			{
        				value = tokenizer.nextToken();
        				
        				Object created = JDTRefactoringDescriptor.handleToElement(arguments.getProject(), value, false);
        				
        				if(created == null)
        					created = JDTRefactoringDescriptor.handleToResource(arguments.getProject(), value);
        				
        				if(created != null)
        					log.markAsCreated(selection, created);
        			}
        		}
        */
    }

}
