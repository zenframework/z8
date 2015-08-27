package org.zenframework.z8.pde.refactoring.processors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.IResourceMapper;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.RefactoringArguments;
import org.eclipse.ltk.core.refactoring.participants.RenameArguments;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.TextEdit;

import org.zenframework.z8.compiler.core.IType;
import org.zenframework.z8.compiler.workspace.CompilationUnit;
import org.zenframework.z8.compiler.workspace.Folder;
import org.zenframework.z8.compiler.workspace.Project;
import org.zenframework.z8.compiler.workspace.ResourceVisitor;
import org.zenframework.z8.compiler.workspace.Workspace;
import org.zenframework.z8.pde.refactoring.Checks;
import org.zenframework.z8.pde.refactoring.IReferenceUpdating;
import org.zenframework.z8.pde.refactoring.Z8RefactoringArguments;
import org.zenframework.z8.pde.refactoring.Z8RefactoringDescriptor;
import org.zenframework.z8.pde.refactoring.Z8RefactoringDescriptorComment;
import org.zenframework.z8.pde.refactoring.RefactoringAvailabilityTester;
import org.zenframework.z8.pde.refactoring.Resources;
import org.zenframework.z8.pde.refactoring.changes.DynamicValidationRefactoringChange;
import org.zenframework.z8.pde.refactoring.changes.RenameFolderChange;
import org.zenframework.z8.pde.refactoring.changes.TextChangeCompatibility;
import org.zenframework.z8.pde.refactoring.changes.TextChangeManager;
import org.zenframework.z8.pde.refactoring.messages.Messages;
import org.zenframework.z8.pde.refactoring.messages.RefactoringMessages;
import org.zenframework.z8.pde.refactoring.modifications.RenameModifications;
import org.zenframework.z8.pde.refactoring.processors.rename.Z8RenameProcessor;
import org.zenframework.z8.pde.refactoring.processors.rename.RenameResourceProcessor;
import org.zenframework.z8.pde.refactoring.search.SearchEngine;

public class RenameFolderProcessor extends Z8RenameProcessor implements IReferenceUpdating, IResourceMapper {
    private static final String ATTRIBUTE_REFERENCES = "references";

    private Folder m_folder;
    private TextChangeManager fChangeManager;
    private boolean fUpdateReferences;

    public static final String IDENTIFIER = "org.zenframework.z8.refactoring.renameFolderProcessor";

    private RenameFolderChange m_renameFolderChange;

    public RenameFolderProcessor(Folder folder) {
        m_folder = folder;

        if(m_folder != null)
            setNewElementName(m_folder.getName());

        fUpdateReferences = true;
    }

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public boolean isApplicable() throws CoreException {
        return RefactoringAvailabilityTester.isRenameAvailable(m_folder);
    }

    @Override
    public String getProcessorName() {
        return RefactoringMessages.RenameFolderRefactoring_name;
    }

    @Override
    protected String[] getAffectedProjectNatures() throws CoreException {
        return Z8Processors.computeAffectedNatures(m_folder);
    }

    @Override
    public Object[] getElements() {
        return new Object[] { m_folder };
    }

    @Override
    protected RenameModifications computeRenameModifications() throws CoreException {
        RenameModifications result = new RenameModifications();
        result.rename(m_folder, new RenameArguments(getNewElementName(), getUpdateReferences()));
        return result;
    }

    public static IFile[] getFiles(CompilationUnit[] cus) {
        List<IFile> files = new ArrayList<IFile>(cus.length);

        for(CompilationUnit compilationUnit : cus) {
            files.add((IFile)compilationUnit.getResource());
        }
        return files.toArray(new IFile[files.size()]);
    }

    @Override
    protected IFile[] getChangedFiles() throws CoreException {
        Set<IFile> combined = new HashSet<IFile>();

        combined.addAll(Arrays.asList(getFiles(fChangeManager.getAllCompilationUnits())));
        combined.addAll(Arrays.asList(getFiles(m_folder.getCompilationUnits())));

        return (IFile[])combined.toArray(new IFile[combined.size()]);
    }

    public int getSaveMode() {
        return 2;
    }

    // ---- IReferenceUpdating --------------------------------------

    @Override
    public boolean canEnableUpdateReferences() {
        return true;
    }

    @Override
    public void setUpdateReferences(boolean update) {
        fUpdateReferences = update;
    }

    @Override
    public boolean getUpdateReferences() {
        return fUpdateReferences;
    }

    // ---- IResourceMapper ----------------------------------

    @Override
    public IResource getRefactoredResource(IResource element) {
        IFolder folder = (IFolder)m_folder.getResource();

        if(folder == null)
            return element;

        IContainer newFolder = getNewFolder();

        if(folder.equals(element))
            return newFolder;

        IPath folderPath = folder.getProjectRelativePath();
        IPath elementPath = element.getProjectRelativePath();

        if(folderPath.isPrefixOf(elementPath)) {
            if(element instanceof IFile && folder.equals(element.getParent())) {
                IPath pathInFolder = elementPath.removeFirstSegments(folderPath.segmentCount());

                if(element instanceof IFile)
                    return newFolder.getFile(pathInFolder);
                else
                    return newFolder.getFolder(pathInFolder);
            }
        }
        return element;
    }

    // ---- IRenameProcessor ----------------------------------------------
    @Override
    public final String getCurrentElementName() {
        return m_folder.getName();
    }

    public String getCurrentElementQualifier() {
        return "";
    }

    @Override
    public RefactoringStatus checkNewElementName(String newName) throws CoreException {
        RefactoringStatus result = Checks.checkFolderName(newName);

        if(result.hasFatalError())
            return result;

        if(Checks.isAlreadyNamed(m_folder, newName)) {
            result.addFatalError(RefactoringMessages.RenameFolderRefactoring_another_name);
            return result;
        }

        result.merge(checkFolderInCurrentRoot(newName));

        return result;
    }

    @Override
    public Object getNewElement() {
        return getNewFolder();
    }

    private IContainer getNewFolder() {
        return ((IContainer)getRoot().getResource()).getFolder(new Path(getNewElementName()));
    }

    @Override
    public RefactoringStatus checkInitialConditions(IProgressMonitor pm) throws CoreException {
        return new RefactoringStatus();
    }

    @Override
    protected RefactoringStatus doCheckFinalConditions(IProgressMonitor pm, CheckConditionsContext context)
            throws CoreException {
        try {
            pm.beginTask("", 23);
            pm.setTaskName(RefactoringMessages.RenameFolderRefactoring_checking);
            RefactoringStatus result = new RefactoringStatus();
            result.merge(checkNewElementName(getNewElementName()));
            pm.worked(1);

            if(Resources.isReadOnly(m_folder.getResource())) {
                String message = Messages.format(RefactoringMessages.RenameFolderRefactoring_resource_read_only,
                        m_folder.getName());
                result.addError(message);
            }

            result.merge(checkFolderName(getNewElementName()));

            if(result.hasFatalError())
                return result;

            fChangeManager = new TextChangeManager();
            SubProgressMonitor subPm = new SubProgressMonitor(pm, 16);

            new FolderRenamer(m_folder, this, fChangeManager).doRename(subPm, result);

            return result;
        }
        finally {
            pm.done();
        }
    }

    public Folder getFolder() {
        return m_folder;
    }

    public static boolean isFolderNameOkInRoot(String newName, Folder root) throws CoreException {
        Folder folder = root.getFolder(newName);
        return folder == null || !folder.getResource().exists();
    }

    private RefactoringStatus checkFolderInCurrentRoot(String newName) throws CoreException {
        if(!isFolderNameOkInRoot(newName, getRoot())) {
            return RefactoringStatus.createFatalErrorStatus(RefactoringMessages.RenameFolderRefactoring_folder_exists);
        }
        else {
            return null;
        }
    }

    private Folder getRoot() {
        return m_folder.getFolder();
    }

    private RefactoringStatus checkFolderName(String newName) throws CoreException {
        RefactoringStatus status = new RefactoringStatus();
        Folder root = getRoot();

        Set<String> topLevelTypeNames = getTopLevelTypeNames();

        if(!isFolderNameOkInRoot(newName, root)) {
            String message = Messages.format(RefactoringMessages.RenameFolderRefactoring_aleady_exists, new Object[] {
                    getNewElementName(), root.getName() });
            status.merge(RefactoringStatus.createWarningStatus(message));
            status.merge(checkTypeNameConflicts(root, newName, topLevelTypeNames));
        }
        return status;
    }

    private Set<String> getTopLevelTypeNames() throws CoreException {
        Set<String> result = new HashSet<String>();

        for(CompilationUnit compilationUnit : m_folder.getCompilationUnits()) {
            IType type = compilationUnit.getReconciledType();

            if(type != null)
                result.add(type.getUserName());
        }

        return result;
    }

    private RefactoringStatus checkTypeNameConflicts(Folder root, String newName, Set<String> topLevelTypeNames)
            throws CoreException {
        Folder otherFolder = root.getFolder(newName);

        if(m_folder.equals(otherFolder))
            return null;

        CompilationUnit[] cus = otherFolder.getCompilationUnits();

        RefactoringStatus result = new RefactoringStatus();

        for(CompilationUnit compilationUnit : cus) {
            result.merge(checkTypeNameConflicts(compilationUnit, topLevelTypeNames));
        }

        return result;
    }

    private RefactoringStatus checkTypeNameConflicts(CompilationUnit compilationUnit, Set<String> topLevelTypeNames)
            throws CoreException {
        RefactoringStatus result = new RefactoringStatus();

        IType type = compilationUnit.getReconciledType();

        String folderName = compilationUnit.getFolder().getName();

        String name = type.getUserName();

        if(topLevelTypeNames.contains(name)) {
            String[] keys = { folderName, name };
            String msg = Messages.format(RefactoringMessages.RenameFolderRefactoring_contains_type, keys);
            result.addError(msg, null);
        }

        return result;
    }

    @Override
    public Change createChange(IProgressMonitor monitor) throws CoreException {
        try {
            monitor.beginTask(RefactoringMessages.RenameFolderRefactoring_creating_change, 1);
            final RefactoringDescriptor descriptor = createRefactoringDescriptor();
            final DynamicValidationRefactoringChange result = new DynamicValidationRefactoringChange(descriptor,
                    RefactoringMessages.RenameFolderRefactoring_change_name);
            result.addAll(fChangeManager.getAllChanges());
            m_renameFolderChange = new RenameFolderChange(m_folder, getNewElementName());
            result.add(m_renameFolderChange);
            monitor.worked(1);
            return result;
        }
        finally {
            fChangeManager = null;
            monitor.done();
        }
    }

    private Z8RefactoringDescriptor createRefactoringDescriptor() {
        String name = null;

        Project project = m_folder.getProject();

        if(project != null)
            name = project.getName();

        final String description = Messages.format(RefactoringMessages.RenameFolderProcessor_descriptor_description_short,
                m_folder.getName());
        final String header = Messages.format(RefactoringMessages.RenameFolderProcessor_descriptor_description,
                new String[] { m_folder.getName(), getNewElementName() });
        final Z8RefactoringDescriptorComment comment = new Z8RefactoringDescriptorComment(this, header);

        Map<String, Object> arguments = new HashMap<String, Object>();
        arguments.put(Z8RefactoringDescriptor.ATTRIBUTE_INPUT, m_folder);
        arguments.put(Z8RefactoringDescriptor.ATTRIBUTE_NAME, getNewElementName());

        Z8RefactoringDescriptor descriptor = new Z8RefactoringDescriptor(RenameResourceProcessor.ID_RENAME_RESOURCE, name,
                description, comment.asString(), arguments, RefactoringDescriptor.STRUCTURAL_CHANGE
                        | RefactoringDescriptor.MULTI_CHANGE | RefactoringDescriptor.BREAKING_CHANGE);
        return descriptor;
    }

    public static IFile[] getModifiedFiles(Change[] changes) {
        List<IFile> result = new ArrayList<IFile>();
        getModifiedFiles(result, changes);
        return result.toArray(new IFile[result.size()]);
    }

    private static void getModifiedFiles(List<IFile> result, Change[] changes) {
        for(int i = 0; i < changes.length; i++) {
            Change change = changes[i];
            Object modifiedElement = change.getModifiedElement();
            if(modifiedElement instanceof IAdaptable) {
                IFile file = (IFile)((IAdaptable)modifiedElement).getAdapter(IFile.class);
                if(file != null)
                    result.add(file);
            }
            if(change instanceof CompositeChange) {
                getModifiedFiles(result, ((CompositeChange)change).getChildren());
            }
        }
    }

    @Override
    public Change postCreateChange(Change[] participantChanges, IProgressMonitor pm) throws CoreException {
        return null;
    }

    public String getNewFolderName(String oldSubFolderName) {
        String oldFolderName = getFolder().getName();
        return getNewElementName() + oldSubFolderName.substring(oldFolderName.length());
    }

    private static class FolderRenamer {
        private Folder m_folder;
        private RenameFolderProcessor m_processor;
        private TextChangeManager m_textChangeManager;

        public FolderRenamer(Folder folder, RenameFolderProcessor processor, TextChangeManager textChangeManager) {
            m_folder = folder;
            m_processor = processor;
            m_textChangeManager = textChangeManager;
        }

        public static CompilationUnit[] getSearchScope() {
            ArrayList<CompilationUnit> result = new ArrayList<CompilationUnit>();

            for(Project project : Workspace.getInstance().getProjects()) {
                CompilationUnit[] units = project.getCompilationUnits();

                result.addAll(Arrays.asList(units));
            }

            return result.toArray(new CompilationUnit[result.size()]);
        }

        void doRename(IProgressMonitor pm, RefactoringStatus result) throws CoreException {
            pm.beginTask("", 16);

            if(m_processor.getUpdateReferences()) {
                pm.setTaskName(RefactoringMessages.RenameFolderRefactoring_searching);

                CompilationUnit[] references = SearchEngine.search(new String[] { m_processor.getCurrentElementName() },
                        getSearchScope(), pm);

                pm.setTaskName(RefactoringMessages.RenameFolderRefactoring_checking);

                try {
                    pm.beginTask("", references.length);

                    CompilationUnit[] movedCompilationUnits = getMovedCompilationUnits();
                    for(CompilationUnit compilationUnit : references) {
                        addImportUpdate(m_textChangeManager, compilationUnit, movedCompilationUnits);
                        pm.worked(1);
                    }
                }
                finally {
                    pm.done();
                }

                pm.worked(1);
            }

            if(result.hasFatalError())
                return;

            pm.done();
        }

        CompilationUnit[] getMovedCompilationUnits() {
            final List<CompilationUnit> result = new ArrayList<CompilationUnit>();

            m_folder.iterate(new ResourceVisitor() {
                @Override
                public boolean visit(CompilationUnit compilationUnit) {
                    result.add(compilationUnit);
                    return true;
                }
            });

            return result.toArray(new CompilationUnit[0]);
        }

        private void addImportUpdate(TextChangeManager manager, CompilationUnit compilationUnit,
                CompilationUnit[] movedCompilationUnits) throws CoreException {
            String name = RefactoringMessages.MoveRefactoring_update_imports;

            MultiTextEdit textEdit = new MultiTextEdit();

            for(CompilationUnit movedCompilationUnit : movedCompilationUnits) {
                IPath head = m_folder.getPath().removeLastSegments(1);
                IPath tail = movedCompilationUnit.getPath().removeFileExtension()
                        .removeFirstSegments(m_folder.getPath().segmentCount());
                compilationUnit.replaceImport(textEdit, movedCompilationUnit.getPath().removeFileExtension(),
                        head.append(m_processor.getNewElementName()).append(tail));

                TextEdit[] children = textEdit.removeChildren();

                for(TextEdit edit : children) {
                    TextChangeCompatibility.addTextEdit(manager.get(compilationUnit), name, edit);
                }
            }
        }
    }

    @Override
    public RefactoringStatus initialize(RefactoringArguments arguments) {
        if(arguments instanceof Z8RefactoringArguments) {
            Z8RefactoringArguments extended = (Z8RefactoringArguments)arguments;

            m_folder = (Folder)extended.getAttribute(Z8RefactoringDescriptor.ATTRIBUTE_INPUT);

            if(m_folder == null) {
                return RefactoringStatus.createFatalErrorStatus(Messages.format(
                        RefactoringMessages.InitializableRefactoring_argument_not_exist,
                        Z8RefactoringDescriptor.ATTRIBUTE_INPUT));
            }

            String name = (String)extended.getAttribute(Z8RefactoringDescriptor.ATTRIBUTE_NAME);

            if(name != null && !"".equals(name))
                setNewElementName(name);
            else
                return RefactoringStatus.createFatalErrorStatus(Messages.format(
                        RefactoringMessages.InitializableRefactoring_argument_not_exist,
                        Z8RefactoringDescriptor.ATTRIBUTE_NAME));

            final String references = (String)extended.getAttribute(ATTRIBUTE_REFERENCES);

            if(references != null) {
                fUpdateReferences = Boolean.valueOf(references).booleanValue();
            }
            else
                return RefactoringStatus.createFatalErrorStatus(Messages.format(
                        RefactoringMessages.InitializableRefactoring_argument_not_exist, ATTRIBUTE_REFERENCES));
        }
        else {
            return RefactoringStatus
                    .createFatalErrorStatus(RefactoringMessages.InitializableRefactoring_inacceptable_arguments);
        }

        return new RefactoringStatus();
    }
}
