package org.zenframework.z8.pde.refactoring.processors.rename;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
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

import org.zenframework.z8.compiler.core.ILanguageElement;
import org.zenframework.z8.compiler.core.IType;
import org.zenframework.z8.compiler.parser.type.ImportElement;
import org.zenframework.z8.compiler.workspace.CompilationUnit;
import org.zenframework.z8.compiler.workspace.Project;
import org.zenframework.z8.compiler.workspace.Workspace;
import org.zenframework.z8.pde.refactoring.Checks;
import org.zenframework.z8.pde.refactoring.ILanguageElementMapper;
import org.zenframework.z8.pde.refactoring.IReferenceUpdating;
import org.zenframework.z8.pde.refactoring.ITextUpdating;
import org.zenframework.z8.pde.refactoring.Z8RefactoringArguments;
import org.zenframework.z8.pde.refactoring.Z8RefactoringDescriptor;
import org.zenframework.z8.pde.refactoring.Z8RefactoringDescriptorComment;
import org.zenframework.z8.pde.refactoring.RefactoringAvailabilityTester;
import org.zenframework.z8.pde.refactoring.ScriptableRefactoring;
import org.zenframework.z8.pde.refactoring.changes.DynamicValidationRefactoringChange;
import org.zenframework.z8.pde.refactoring.changes.RenameCompilationUnitChange;
import org.zenframework.z8.pde.refactoring.changes.RenameResourceChange;
import org.zenframework.z8.pde.refactoring.changes.TextChangeCompatibility;
import org.zenframework.z8.pde.refactoring.changes.TextChangeManager;
import org.zenframework.z8.pde.refactoring.messages.LanguageElementLabels;
import org.zenframework.z8.pde.refactoring.messages.Messages;
import org.zenframework.z8.pde.refactoring.messages.RefactoringMessages;
import org.zenframework.z8.pde.refactoring.modifications.RenameModifications;
import org.zenframework.z8.pde.refactoring.processors.QualifiedNameSearchResult;
import org.zenframework.z8.pde.refactoring.processors.Z8Processors;
import org.zenframework.z8.pde.refactoring.search.SearchEngine;

@SuppressWarnings("deprecation")
public class RenameTypeProcessor extends Z8RenameProcessor implements ITextUpdating, IReferenceUpdating, IResourceMapper,
        ILanguageElementMapper {
    public static final String ID_RENAME_TYPE = "org.zenframework.z8.refactoring.rename.type";
    public static final String IDENTIFIER = "org.zenframework.z8.refactoring.rename.renameTypeProcessor";

    private static final String ATTRIBUTE_REFERENCES = "references";
    private static final String ATTRIBUTE_TEXTUAL_MATCHES = "textual";

    private CompilationUnit m_compilationUnit;
    private CompilationUnit[] m_references;
    private TextChangeManager m_changeManager;
    private QualifiedNameSearchResult m_qualifiedNameSearchResult;

    private boolean m_updateReferences;
    private boolean m_updateTextualMatches;

    private RefactoringStatus m_cachedRefactoringStatus = null;

    public RenameTypeProcessor(CompilationUnit compilationUnit) {
        m_compilationUnit = compilationUnit;

        IType type = getType();

        if(type != null) {
            setNewElementName(type.getUserName());
        }

        m_updateReferences = true;
        m_updateTextualMatches = false;
    }

    public IType getType() {
        return m_compilationUnit.getReconciledType();
    }

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public boolean isApplicable() throws CoreException {
        return RefactoringAvailabilityTester.isRenameAvailable(getType());
    }

    @Override
    public String getProcessorName() {
        return RefactoringMessages.RenameTypeRefactoring_name;
    }

    @Override
    protected String[] getAffectedProjectNatures() throws CoreException {
        return Z8Processors.computeAffectedNatures(getType());
    }

    @Override
    public Object[] getElements() {
        return new Object[] { getType() };
    }

    @Override
    protected RenameModifications computeRenameModifications() {
        RenameModifications result = new RenameModifications();
        result.rename(getType(), new RenameArguments(getNewElementName(), getUpdateReferences()));

        String newCUName = getNewCompilationUnit().getName();
        result.rename(m_compilationUnit, new RenameArguments(newCUName, getUpdateReferences()));
        return result;
    }

    // ---- IRenameProcessor ----------------------------------------------
    @Override
    public String getCurrentElementName() {
        return getType().getUserName();
    }

    @Override
    public String getCurrentElementQualifier() {
        return getType().getCompilationUnit().getPackage();
    }

    @Override
    public RefactoringStatus checkNewElementName(String newName) {
        Assert.isNotNull(newName, "new name");
        RefactoringStatus result = Checks.checkTypeName(newName);
        if(Checks.isAlreadyNamed(getType(), newName))
            result.addFatalError(RefactoringMessages.RenameTypeRefactoring_choose_another_name);
        return result;
    }

    @Override
    public Object getNewElement() {
        return getType();
    }

    private CompilationUnit getNewCompilationUnit() {
        return m_compilationUnit;
    }

    protected RenameArguments createRenameArguments() {
        return new RenameArguments(getNewElementName(), getUpdateReferences());
    }

    @Override
    protected IFile[] getChangedFiles() throws CoreException {
        List<IFile> result = new ArrayList<IFile>();

        result.addAll(Arrays.asList(m_changeManager.getFiles()));

        if(m_qualifiedNameSearchResult != null) {
            result.addAll(Arrays.asList(m_qualifiedNameSearchResult.getAllFiles()));
        }

        if(willRenameCU()) {
            result.add((IFile)m_compilationUnit.getResource());
        }

        return result.toArray(new IFile[result.size()]);
    }

    // ---- ITextUpdating -------------------------------------------------
    @Override
    public boolean canEnableTextUpdating() {
        return true;
    }

    @Override
    public boolean getUpdateTextualMatches() {
        return m_updateTextualMatches;
    }

    @Override
    public void setUpdateTextualMatches(boolean update) {
        m_updateTextualMatches = update;
    }

    // ---- IReferenceUpdating --------------------------------------
    @Override
    public void setUpdateReferences(boolean update) {
        m_updateReferences = update;
    }

    @Override
    public boolean canEnableUpdateReferences() {
        return true;
    }

    @Override
    public boolean getUpdateReferences() {
        return m_updateReferences;
    }

    @Override
    public IResource getRefactoredResource(IResource element) {
        if(element instanceof IFile) {
            if(element.equals(m_compilationUnit.getResource())) {
                return getNewCompilationUnit().getResource();
            }
        }
        return element;
    }

    @Override
    public ILanguageElement getRefactoredLanguageElement(ILanguageElement element) {
        if(element instanceof CompilationUnit) {
            if(element.equals(m_compilationUnit))
                return getNewCompilationUnit();
        }
        return element;
    }

    @Override
    public RefactoringStatus checkInitialConditions(IProgressMonitor pm) throws CoreException {
        if(getType() == null || !m_compilationUnit.getResource().exists()) {
            String message = Messages.format(RefactoringMessages.RenameTypeRefactoring_does_not_exist, new String[] {
                    getType().getQualifiedUserName(), m_compilationUnit.getName() });
            return RefactoringStatus.createFatalErrorStatus(message);
        }

        return Checks.checkIfCuBroken(m_compilationUnit);
    }

    @Override
    protected RefactoringStatus doCheckFinalConditions(IProgressMonitor pm, CheckConditionsContext context)
            throws CoreException {
        RefactoringStatus result = new RefactoringStatus();

        int referenceSearchTicks = m_updateReferences ? 15 : 0;
        int affectedCusTicks = m_updateReferences ? 10 : 1;
        int createChangeTicks = 5;

        try {
            pm.beginTask("", 12 + referenceSearchTicks + affectedCusTicks + createChangeTicks);

            pm.setTaskName(RefactoringMessages.RenameTypeRefactoring_checking);

            m_changeManager = new TextChangeManager(true);

            result.merge(checkNewElementName(getNewElementName()));

            if(result.hasFatalError())
                return result;

            result.merge(Checks.checkIfCuBroken(m_compilationUnit));

            if(result.hasFatalError())
                return result;

            pm.worked(1);
            result.merge(checkImportedTypes());

            result.merge(checkTypesFolder());
            pm.worked(1);

            result.merge(checkTypesImportedInCu());
            pm.worked(1);

            result.merge(checkIfNativeType(getType()));
            pm.worked(1);

            if(result.hasFatalError())
                return result;

            m_references = new CompilationUnit[0];

            if(m_updateReferences) {
                pm.setTaskName(RefactoringMessages.RenameTypeRefactoring_searching);
                result.merge(initializeReferences(new SubProgressMonitor(pm, referenceSearchTicks)));
            }

            pm.setTaskName(RefactoringMessages.RenameTypeRefactoring_checking);

            if(pm.isCanceled())
                throw new OperationCanceledException();

            if(m_updateReferences) {
                result.merge(analyzeAffectedCompilationUnits(new SubProgressMonitor(pm, affectedCusTicks)));
            }
            else {
                Checks.checkCompileErrorsInAffectedFile(result, m_compilationUnit);
                pm.worked(affectedCusTicks);
            }

            if(result.hasFatalError())
                return result;

            createChanges(new SubProgressMonitor(pm, createChangeTicks));

            return result;
        }
        finally {
            pm.done();
        }
    }

    private CompilationUnit[] getCompilationUnits() {
        ArrayList<CompilationUnit> result = new ArrayList<CompilationUnit>();

        for(Project project : Workspace.getInstance().getProjects()) {
            CompilationUnit[] units = project.getCompilationUnits();

            result.addAll(Arrays.asList(units));
        }

        return result.toArray(new CompilationUnit[result.size()]);
    }

    public RefactoringStatus initializeReferences(IProgressMonitor monitor) throws CoreException, OperationCanceledException {
        CompilationUnit[] compilationUnits = getCompilationUnits();

        m_references = SearchEngine.search(getCurrentElementName(), compilationUnits, monitor);

        return m_cachedRefactoringStatus;
    }

    private RefactoringStatus checkIfNativeType(IType type) throws CoreException {
        if(type.isNative()) {
            String msg = Messages.format(RefactoringMessages.Checks_type_native, new String[] {
                    getType().getQualifiedUserName(), "UnsatisfiedLinkError" });
            return RefactoringStatus.createErrorStatus(msg);
        }

        return new RefactoringStatus();
    }

    private RefactoringStatus checkNewPathValidity() {
        IContainer c = m_compilationUnit.getResource().getParent();

        String notRename = RefactoringMessages.RenameTypeRefactoring_will_not_rename;

        IStatus status = c.getWorkspace().validateName(getNewElementName(), IResource.FILE);

        if(status.getSeverity() == IStatus.ERROR)
            return RefactoringStatus.createWarningStatus(status.getMessage() + ". " + notRename);

        status = c.getWorkspace().validatePath(createNewPath(getNewElementName()), IResource.FILE);

        if(status.getSeverity() == IStatus.ERROR)
            return RefactoringStatus.createWarningStatus(status.getMessage() + ". " + notRename);

        return new RefactoringStatus();
    }

    private String createNewPath(String newName) {
        return m_compilationUnit.getResource().getFullPath().removeLastSegments(1).append(newName).toString();
    }

    private RefactoringStatus checkTypesImportedInCu() throws CoreException {
        ImportElement imp = getImportedType(m_compilationUnit, getNewElementName());

        if(imp == null)
            return null;

        String msg = Messages.format(RefactoringMessages.RenameTypeRefactoring_imported, new Object[] { getNewElementName(),
                m_compilationUnit.getResource().getFullPath().toString() });
        return RefactoringStatus.createErrorStatus(msg);
    }

    private RefactoringStatus checkTypesFolder() throws CoreException {
        IType type = Checks.findTypeFolder(m_compilationUnit.getFolder(), getNewElementName());

        if(type == null || !type.getCompilationUnit().getResource().exists())
            return null;

        String msg = Messages.format(RefactoringMessages.RenameTypeRefactoring_exists, new String[] { getNewElementName(),
                m_compilationUnit.getPackage() });
        return RefactoringStatus.createErrorStatus(msg);
    }

    private static ImportElement getImportedType(CompilationUnit cu, String typeName) throws CoreException {
        String dotTypeName = "." + typeName;

        for(ImportElement element : cu.getReconciledType().getImports()) {
            if(element.getQualifiedName().endsWith(dotTypeName))
                return element;
        }

        return null;
    }

    private RefactoringStatus checkImportedTypes() throws CoreException {
        RefactoringStatus result = new RefactoringStatus();

        for(ImportElement element : m_compilationUnit.getReconciledType().getImports()) {
            analyzeImportDeclaration(element, result);
        }
        return result;
    }

    private void analyzeImportedTypes(IType type, RefactoringStatus result, ImportElement imp) throws CoreException {
        if(type.getUserName().equals(getNewElementName())) {
            String msg = Messages.format(RefactoringMessages.RenameTypeRefactoring_name_conflict1,
                    new Object[] { type.getQualifiedUserName(),
                            imp.extractCompilationUnit().getResource().getFullPath().toString() });
            result.addError(msg);
        }
    }

    private void analyzeImportDeclaration(ImportElement imp, RefactoringStatus result) throws CoreException {
        CompilationUnit imported = imp.extractCompilationUnit();

        if(imported == null)
            return;

        analyzeImportedTypes(imported.getReconciledType(), result, imp);
    }

    private RefactoringStatus analyzeAffectedCompilationUnits(IProgressMonitor pm) throws CoreException {
        RefactoringStatus result = new RefactoringStatus();
        //		result.merge(Checks.checkCompileErrorsInAffectedFiles(m_references, m_compilationUnit.getResource()));
        pm.beginTask("", m_references.length);
        result.merge(checkConflictingTypes(pm));
        return result;
    }

    private RefactoringStatus checkConflictingTypes(IProgressMonitor pm) throws CoreException {
        RefactoringStatus result = new RefactoringStatus();

        /*		IJavaSearchScope scope = RefactoringScopeFactory.create(m_type);
        		
        		SearchPattern pattern = SearchPattern.createPattern(getNewElementName(), IJavaSearchConstants.TYPE, IJavaSearchConstants.ALL_OCCURRENCES, SearchUtils.GENERICS_AGNOSTIC_MATCH_RULE);
        	
        		CompilationUnit[] cusWithReferencesToConflictingTypes = RefactoringSearchEngine.findAffectedCompilationUnits(pattern, scope, pm, result);
        		
        		if(cusWithReferencesToConflictingTypes.length == 0)
        			return result;
        		
        		CompilationUnit[] cusWithReferencesToRenamedType = getCus(m_references);
        		CompilationUnit[] intersection = isIntersectionEmpty(cusWithReferencesToRenamedType, cusWithReferencesToConflictingTypes);
        		
        		if(intersection.length == 0)
        			return result;
        		for(int i = 0; i < intersection.length; i++)
        		
        		{
        			String message = Messages.format(RefactoringMessages.RenameTypeRefactoring_another_type, new String[] { getNewElementName(), intersection[i].getElementName() });
        			result.addError(message);
        		}
        */
        return result;
    }

    // ------------- Changes ---------------
    @Override
    public Change createChange(IProgressMonitor monitor) throws CoreException {
        try {
            monitor.beginTask(RefactoringMessages.RenameTypeRefactoring_creating_change, 4);
            Map<String, Object> arguments = new HashMap<String, Object>();

            String projectName = null;

            Project project = m_compilationUnit.getProject();

            if(project != null)
                projectName = project.getName();

            int flags = RefactoringDescriptor.STRUCTURAL_CHANGE | RefactoringDescriptor.MULTI_CHANGE;

            String description = Messages.format(RefactoringMessages.RenameTypeProcessor_descriptor_description_short,
                    getType().getUserName());
            String header = Messages.format(RefactoringMessages.RenameTypeProcessor_descriptor_description, new String[] {
                    LanguageElementLabels.getElementLabel(getType(), LanguageElementLabels.ALL_FULLY_QUALIFIED),
                    getNewElementName() });
            String comment = new Z8RefactoringDescriptorComment(this, header).asString();

            Z8RefactoringDescriptor descriptor = new Z8RefactoringDescriptor(ID_RENAME_TYPE, projectName, description,
                    comment, arguments, flags);
            arguments.put(Z8RefactoringDescriptor.ATTRIBUTE_INPUT, getType());
            arguments.put(Z8RefactoringDescriptor.ATTRIBUTE_NAME, getNewElementName());

            arguments.put(ATTRIBUTE_REFERENCES, Boolean.valueOf(m_updateReferences).toString());
            arguments.put(ATTRIBUTE_TEXTUAL_MATCHES, Boolean.valueOf(m_updateTextualMatches).toString());

            DynamicValidationRefactoringChange result = new DynamicValidationRefactoringChange(descriptor,
                    RefactoringMessages.RenameTypeProcessor_change_name);
            result.addAll(m_changeManager.getAllChanges());

            if(willRenameCU()) {
                IResource resource = m_compilationUnit.getResource();
                if(resource != null && resource.isLinked()) {
                    String ext = resource.getFileExtension();
                    String renamedResourceName;
                    if(ext == null)
                        renamedResourceName = getNewElementName();
                    else
                        renamedResourceName = getNewElementName() + '.' + ext;
                    result.add(new RenameResourceChange(null, m_compilationUnit.getResource(), renamedResourceName, comment));
                }
                else {
                    String renamedCUName = getNewElementName() + ".bl";
                    result.add(new RenameCompilationUnitChange(null, m_compilationUnit, renamedCUName, comment));
                }
            }
            monitor.worked(1);
            return result;
        }
        finally {
            m_changeManager = null;
        }
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
        if(m_qualifiedNameSearchResult != null) {
            try {
                return m_qualifiedNameSearchResult.getSingleChange(getModifiedFiles(participantChanges));
            }
            finally {
                m_qualifiedNameSearchResult = null;
            }
        }
        else {
            return null;
        }
    }

    private boolean willRenameCU() throws CoreException {
        String name = m_compilationUnit.getName();

        int index = name.lastIndexOf('.');

        if(index != -1) {
            name = name.substring(0, index);
        }

        if(!(Checks.isTopLevel(getType()) && name.equals(getType().getUserName())))
            return false;

        if(!checkNewPathValidity().isOK())
            return false;

        if(!Checks.checkCompilationUnitNewName(m_compilationUnit, getNewElementName()).isOK())
            return false;

        return true;
    }

    private void createChanges(IProgressMonitor pm) throws CoreException {
        try {
            pm.beginTask("", 12);

            pm.setTaskName(RefactoringMessages.RenameTypeProcessor_creating_changes);

            if(m_updateReferences) {
                addReferenceUpdates(m_changeManager, new SubProgressMonitor(pm, 3));
            }

            pm.worked(1);

            addTypeDeclarationUpdate(m_changeManager, m_compilationUnit);
        }
        finally {
            pm.done();
        }
    }

    private void addTypeDeclarationUpdate(TextChangeManager manager, CompilationUnit compilationUnit) throws CoreException {
        String name = RefactoringMessages.RenameTypeRefactoring_update;

        MultiTextEdit textEdit = new MultiTextEdit();

        compilationUnit.replaceTypeName(textEdit, getType(), getNewElementName());

        TextEdit[] children = textEdit.removeChildren();

        for(TextEdit edit : children) {
            TextChangeCompatibility.addTextEdit(manager.get(compilationUnit), name, edit);
        }
    }

    private void addReferenceUpdates(TextChangeManager manager, IProgressMonitor pm) throws CoreException {
        try {
            pm.beginTask("", m_references.length);

            for(CompilationUnit compilationUnit : m_references) {
                if(compilationUnit != m_compilationUnit) {
                    addTypeDeclarationUpdate(manager, compilationUnit);
                }
                pm.worked(1);
            }
        }
        finally {
            pm.done();
        }
    }

    @Override
    public RefactoringStatus initialize(RefactoringArguments arguments) {
        if(arguments instanceof Z8RefactoringArguments) {
            Z8RefactoringArguments extended = (Z8RefactoringArguments)arguments;

            Object element = extended.getAttribute(Z8RefactoringDescriptor.ATTRIBUTE_INPUT);

            if(element != null && element instanceof IType) {
                m_compilationUnit = ((IType)element).getCompilationUnit();
            }
            else {
                return ScriptableRefactoring.createInputFatalStatus(element, getRefactoring().getName(), ID_RENAME_TYPE);
            }

            String name = (String)extended.getAttribute(Z8RefactoringDescriptor.ATTRIBUTE_NAME);

            if(name != null && !"".equals(name))
                setNewElementName(name);
            else
                return RefactoringStatus.createFatalErrorStatus(Messages.format(
                        RefactoringMessages.InitializableRefactoring_argument_not_exist,
                        Z8RefactoringDescriptor.ATTRIBUTE_NAME));

            String references = (String)extended.getAttribute(ATTRIBUTE_REFERENCES);

            if(references != null) {
                m_updateReferences = Boolean.valueOf(references).booleanValue();
            }
            else {
                return RefactoringStatus.createFatalErrorStatus(Messages.format(
                        RefactoringMessages.InitializableRefactoring_argument_not_exist, ATTRIBUTE_REFERENCES));
            }

            String matches = (String)extended.getAttribute(ATTRIBUTE_TEXTUAL_MATCHES);

            if(matches != null) {
                m_updateTextualMatches = Boolean.valueOf(matches).booleanValue();
            }
            else {
                return RefactoringStatus.createFatalErrorStatus(Messages.format(
                        RefactoringMessages.InitializableRefactoring_argument_not_exist, ATTRIBUTE_TEXTUAL_MATCHES));
            }
        }
        else {
            return RefactoringStatus
                    .createFatalErrorStatus(RefactoringMessages.InitializableRefactoring_inacceptable_arguments);
        }

        return new RefactoringStatus();
    }
}
