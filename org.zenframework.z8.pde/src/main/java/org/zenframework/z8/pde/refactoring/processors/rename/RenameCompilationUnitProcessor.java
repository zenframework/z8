package org.zenframework.z8.pde.refactoring.processors.rename;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.IResourceMapper;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.RefactoringArguments;
import org.eclipse.ltk.core.refactoring.participants.RenameArguments;
import org.eclipse.ui.ide.ResourceUtil;

import org.zenframework.z8.compiler.core.ILanguageElement;
import org.zenframework.z8.compiler.workspace.CompilationUnit;
import org.zenframework.z8.compiler.workspace.Folder;
import org.zenframework.z8.compiler.workspace.Workspace;
import org.zenframework.z8.pde.Plugin;
import org.zenframework.z8.pde.refactoring.Checks;
import org.zenframework.z8.pde.refactoring.ILanguageElementMapper;
import org.zenframework.z8.pde.refactoring.IReferenceUpdating;
import org.zenframework.z8.pde.refactoring.ITextUpdating;
import org.zenframework.z8.pde.refactoring.LanguageConventions;
import org.zenframework.z8.pde.refactoring.Z8RefactoringArguments;
import org.zenframework.z8.pde.refactoring.Z8RefactoringDescriptor;
import org.zenframework.z8.pde.refactoring.Z8RefactoringDescriptorComment;
import org.zenframework.z8.pde.refactoring.RefactoringAvailabilityTester;
import org.zenframework.z8.pde.refactoring.ScriptableRefactoring;
import org.zenframework.z8.pde.refactoring.changes.DynamicValidationStateChange;
import org.zenframework.z8.pde.refactoring.changes.RenameCompilationUnitChange;
import org.zenframework.z8.pde.refactoring.changes.RenameResourceChange;
import org.zenframework.z8.pde.refactoring.messages.Messages;
import org.zenframework.z8.pde.refactoring.messages.RefactoringMessages;
import org.zenframework.z8.pde.refactoring.modifications.RenameModifications;
import org.zenframework.z8.pde.refactoring.processors.Z8Processors;

public class RenameCompilationUnitProcessor extends Z8RenameProcessor implements IReferenceUpdating, ITextUpdating,
        IResourceMapper, ILanguageElementMapper {
    private static final String ID_RENAME_COMPILATION_UNIT = "org.zenframework.z8.refactoring.compilationunit";
    public static final String IDENTIFIER = "org.zenframework.z8.refactoring.renameCompilationUnitProcessor";

    private static final String ATTRIBUTE_PATH = "path";
    private static final String ATTRIBUTE_NAME = "name";

    private RenameTypeProcessor m_renameTypeProcessor;
    private boolean m_willRenameType;
    private CompilationUnit m_compilationUnit;

    public RenameCompilationUnitProcessor(CompilationUnit compilationUnit) throws CoreException {
        m_compilationUnit = compilationUnit;

        if(m_compilationUnit != null) {
            computeRenameTypeRefactoring();
            setNewElementName(m_compilationUnit.getName());
        }
    }

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public boolean isApplicable() {
        return RefactoringAvailabilityTester.isRenameAvailable(m_compilationUnit.getResource());
    }

    @Override
    public String getProcessorName() {
        return RefactoringMessages.RenameCompilationUnitRefactoring_name;
    }

    @Override
    protected String[] getAffectedProjectNatures() throws CoreException {
        return Z8Processors.computeAffectedNatures(m_compilationUnit);
    }

    @Override
    public Object[] getElements() {
        return new Object[] { m_compilationUnit };
    }

    @Override
    protected RenameModifications computeRenameModifications() {
        RenameModifications result = new RenameModifications();
        result.rename(m_compilationUnit, new RenameArguments(getNewElementName(), getUpdateReferences()));
        if(m_renameTypeProcessor != null) {
            String newTypeName = removeFileNameExtension(getNewElementName());
            RenameArguments arguments = new RenameArguments(newTypeName, getUpdateReferences());
            result.rename(m_renameTypeProcessor.getType(), arguments);
        }
        return result;
    }

    @Override
    protected IFile[] getChangedFiles() throws CoreException {
        if(!m_willRenameType) {
            IFile file = ResourceUtil.getFile(m_compilationUnit);
            if(file != null)
                return new IFile[] { file };
        }
        return new IFile[0];
    }

    // ---- IRenameProcessor -------------------------------------
    @Override
    public String getCurrentElementName() {
        return getSimpleCUName();
    }

    @Override
    public String getCurrentElementQualifier() {
        return m_compilationUnit.getFolder().getName();
    }

    @Override
    public RefactoringStatus checkNewElementName(String newName) throws CoreException {
        String typeName = removeFileNameExtension(newName);

        RefactoringStatus result = Checks.checkCompilationUnitName(newName);

        if(m_willRenameType) {
            result.merge(m_renameTypeProcessor.checkNewElementName(typeName));
        }

        if(Checks.isAlreadyNamed(m_compilationUnit, newName)) {
            result.addFatalError(RefactoringMessages.RenameCompilationUnitRefactoring_same_name);
        }

        return result;
    }

    @Override
    public void setNewElementName(String newName) {
        super.setNewElementName(newName);
        if(m_willRenameType)
            m_renameTypeProcessor.setNewElementName(removeFileNameExtension(newName));
    }

    @Override
    public Object getNewElement() {
        Folder folder = m_compilationUnit.getFolder();

        if(LanguageConventions.validateCompilationUnitName(getNewElementName()).getSeverity() == IStatus.ERROR)
            return m_compilationUnit;

        return folder.getCompilationUnit(getNewElementName());
    }

    // ---- ITextUpdating ---------------------------------------------
    @Override
    public boolean canEnableTextUpdating() {
        if(m_renameTypeProcessor == null)
            return false;
        return m_renameTypeProcessor.canEnableUpdateReferences();
    }

    @Override
    public boolean getUpdateTextualMatches() {
        if(m_renameTypeProcessor == null)
            return false;
        return m_renameTypeProcessor.getUpdateTextualMatches();
    }

    @Override
    public void setUpdateTextualMatches(boolean update) {
        if(m_renameTypeProcessor != null)
            m_renameTypeProcessor.setUpdateTextualMatches(update);
    }

    // ---- IReferenceUpdating -----------------------------------
    @Override
    public boolean canEnableUpdateReferences() {
        if(m_renameTypeProcessor == null)
            return false;
        return m_renameTypeProcessor.canEnableUpdateReferences();
    }

    @Override
    public void setUpdateReferences(boolean update) {
        if(m_renameTypeProcessor != null)
            m_renameTypeProcessor.setUpdateReferences(update);
    }

    @Override
    public boolean getUpdateReferences() {
        if(m_renameTypeProcessor == null)
            return false;
        return m_renameTypeProcessor.getUpdateReferences();
    }

    @Override
    public IResource getRefactoredResource(IResource element) {
        if(m_renameTypeProcessor == null)
            return element;
        return m_renameTypeProcessor.getRefactoredResource(element);
    }

    @Override
    public ILanguageElement getRefactoredLanguageElement(ILanguageElement element) {
        if(m_renameTypeProcessor == null)
            return element;
        return m_renameTypeProcessor.getRefactoredLanguageElement(element);
    }

    // --- preconditions ----------------------------------
    @Override
    public RefactoringStatus checkInitialConditions(IProgressMonitor pm) throws CoreException {
        if(m_renameTypeProcessor != null && m_compilationUnit.containsParseError()) {
            m_renameTypeProcessor = null;
            m_willRenameType = false;
            return new RefactoringStatus();
        }

        return new RefactoringStatus();
    }

    @Override
    protected RefactoringStatus doCheckFinalConditions(IProgressMonitor pm, CheckConditionsContext context)
            throws CoreException {
        try {
            if(m_willRenameType && m_compilationUnit.containsParseError()) {
                RefactoringStatus result1 = new RefactoringStatus();
                RefactoringStatus result2 = new RefactoringStatus();
                result2.merge(Checks.checkCompilationUnitNewName(m_compilationUnit, getNewElementName()));

                if(result2.hasFatalError()) {
                    result1.addError(Messages.format(RefactoringMessages.RenameCompilationUnitRefactoring_not_parsed_1,
                            m_compilationUnit.getName()));
                }
                else {
                    result1.addError(Messages.format(RefactoringMessages.RenameCompilationUnitRefactoring_not_parsed,
                            m_compilationUnit.getName()));
                }

                result1.merge(result2);
            }

            if(m_willRenameType) {
                return m_renameTypeProcessor.checkFinalConditions(pm, context);
            }
            else {
                return Checks.checkCompilationUnitNewName(m_compilationUnit, getNewElementName());
            }
        }
        finally {
            pm.done();
        }
    }

    private void computeRenameTypeRefactoring() throws CoreException {
        if(getSimpleCUName().indexOf(".") != -1) {
            m_renameTypeProcessor = null;
            m_willRenameType = false;
            return;
        }

        if(m_compilationUnit.getReconciledType() != null) {
            m_renameTypeProcessor = new RenameTypeProcessor(m_compilationUnit);
        }
        else {
            m_renameTypeProcessor = null;
        }

        m_willRenameType = m_renameTypeProcessor != null && !m_compilationUnit.containsParseError();
    }

    private String getSimpleCUName() {
        return removeFileNameExtension(m_compilationUnit.getName());
    }

    private static String removeFileNameExtension(String fileName) {
        if(fileName.lastIndexOf(".") == -1)
            return fileName;
        return fileName.substring(0, fileName.lastIndexOf("."));
    }

    @Override
    public Change createChange(IProgressMonitor pm) throws CoreException {
        if(m_willRenameType)
            return m_renameTypeProcessor.createChange(pm);

        m_renameTypeProcessor = null;

        String newName = getNewElementName();
        IResource resource = ResourceUtil.getResource(m_compilationUnit);

        if(resource != null && resource.isLinked()) {
            Map<String, Object> arguments = new HashMap<String, Object>();

            IProject project = resource.getProject();
            String name = project.getName();
            String description = Messages.format(
                    RefactoringMessages.RenameCompilationUnitChange_descriptor_description_short, resource.getName());
            String header = Messages.format(RefactoringMessages.RenameCompilationUnitChange_descriptor_description,
                    new String[] { resource.getFullPath().toString(), newName });
            String comment = new Z8RefactoringDescriptorComment(this, header).asString();
            Z8RefactoringDescriptor descriptor = new Z8RefactoringDescriptor(RenameResourceProcessor.ID_RENAME_RESOURCE,
                    name, description, comment, arguments, (RefactoringDescriptor.STRUCTURAL_CHANGE
                            | RefactoringDescriptor.MULTI_CHANGE | RefactoringDescriptor.BREAKING_CHANGE));

            arguments.put(Z8RefactoringDescriptor.ATTRIBUTE_INPUT, m_compilationUnit.getResource());
            arguments.put(Z8RefactoringDescriptor.ATTRIBUTE_NAME, newName);

            return new DynamicValidationStateChange(new RenameResourceChange(descriptor, resource, newName, comment));
        }

        String label = null;

        if(m_compilationUnit != null) {
            Folder fragment = m_compilationUnit.getFolder();
            label = fragment.getName() + "." + m_compilationUnit.getName();
        }
        else {
            assert (false);
            label = m_compilationUnit.getName();
        }

        Map<String, Object> arguments = new HashMap<String, Object>();
        String name = m_compilationUnit.getProject().getName();
        String description = Messages.format(RefactoringMessages.RenameCompilationUnitChange_descriptor_description_short,
                m_compilationUnit.getName());
        String header = Messages.format(RefactoringMessages.RenameCompilationUnitChange_descriptor_description,
                new String[] { label, newName });
        String comment = new Z8RefactoringDescriptorComment(this, header).asString();
        Z8RefactoringDescriptor descriptor = new Z8RefactoringDescriptor(
                RenameCompilationUnitProcessor.ID_RENAME_COMPILATION_UNIT, name, description, comment, arguments,
                RefactoringDescriptor.STRUCTURAL_CHANGE | RefactoringDescriptor.MULTI_CHANGE);
        arguments.put(Z8RefactoringDescriptor.ATTRIBUTE_INPUT, m_compilationUnit);
        arguments.put(Z8RefactoringDescriptor.ATTRIBUTE_NAME, newName);
        return new DynamicValidationStateChange(new RenameCompilationUnitChange(descriptor, m_compilationUnit, newName,
                comment));
    }

    @Override
    public Change postCreateChange(Change[] participantChanges, IProgressMonitor pm) throws CoreException {
        if(m_willRenameType) {
            return m_renameTypeProcessor.postCreateChange(participantChanges, pm);
        }
        return super.postCreateChange(participantChanges, pm);
    }

    @Override
    public RefactoringStatus initialize(RefactoringArguments arguments) {
        if(arguments instanceof Z8RefactoringArguments) {
            Z8RefactoringArguments generic = (Z8RefactoringArguments)arguments;

            String path = (String)generic.getAttribute(ATTRIBUTE_PATH);

            if(path != null) {
                IResource resource = ResourcesPlugin.getWorkspace().getRoot().findMember(new Path(path));

                if(resource == null || !resource.exists()) {
                    return ScriptableRefactoring.createInputFatalStatus(resource, getRefactoring().getName(),
                            ID_RENAME_COMPILATION_UNIT);
                }
                else {
                    m_compilationUnit = Workspace.getInstance().getCompilationUnit(resource);

                    try {
                        computeRenameTypeRefactoring();
                    }
                    catch(CoreException exception) {
                        Plugin.log(exception);
                    }
                }
            }
            else {
                return RefactoringStatus.createFatalErrorStatus(Messages.format(
                        RefactoringMessages.InitializableRefactoring_argument_not_exist, ATTRIBUTE_PATH));
            }

            String name = (String)generic.getAttribute(ATTRIBUTE_NAME);

            if(name != null && !"".equals(name)) {
                setNewElementName(name);
            }
            else {
                return RefactoringStatus.createFatalErrorStatus(Messages.format(
                        RefactoringMessages.InitializableRefactoring_argument_not_exist, ATTRIBUTE_NAME));
            }
        }
        else
            return RefactoringStatus
                    .createFatalErrorStatus(RefactoringMessages.InitializableRefactoring_inacceptable_arguments);
        return new RefactoringStatus();
    }

    public RenameTypeProcessor getRenameTypeProcessor() {
        return m_renameTypeProcessor;
    }

    public boolean isWillRenameType() {
        return m_willRenameType;
    }
}
