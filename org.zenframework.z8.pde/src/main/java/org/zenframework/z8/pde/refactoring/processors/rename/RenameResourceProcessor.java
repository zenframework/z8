package org.zenframework.z8.pde.refactoring.processors.rename;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.mapping.IResourceChangeDescriptionFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.RefactoringArguments;
import org.eclipse.ltk.core.refactoring.participants.RefactoringParticipant;
import org.eclipse.ltk.core.refactoring.participants.RenameArguments;
import org.eclipse.ltk.core.refactoring.participants.RenameProcessor;
import org.eclipse.ltk.core.refactoring.participants.ResourceChangeChecker;
import org.eclipse.ltk.core.refactoring.participants.SharableParticipants;

import org.zenframework.z8.pde.refactoring.IScriptableRefactoring;
import org.zenframework.z8.pde.refactoring.Z8RefactoringArguments;
import org.zenframework.z8.pde.refactoring.Z8RefactoringDescriptor;
import org.zenframework.z8.pde.refactoring.Z8RefactoringDescriptorComment;
import org.zenframework.z8.pde.refactoring.RefactoringAvailabilityTester;
import org.zenframework.z8.pde.refactoring.Resources;
import org.zenframework.z8.pde.refactoring.ScriptableRefactoring;
import org.zenframework.z8.pde.refactoring.changes.DynamicValidationStateChange;
import org.zenframework.z8.pde.refactoring.changes.RenameResourceChange;
import org.zenframework.z8.pde.refactoring.messages.Messages;
import org.zenframework.z8.pde.refactoring.messages.RefactoringMessages;
import org.zenframework.z8.pde.refactoring.modifications.RenameModifications;
import org.zenframework.z8.pde.refactoring.processors.ResourceProcessors;
import org.zenframework.z8.pde.refactoring.rename.ICommentProvider;
import org.zenframework.z8.pde.refactoring.rename.INameUpdating;

public class RenameResourceProcessor extends RenameProcessor implements IScriptableRefactoring, ICommentProvider,
        INameUpdating {
    public static final String ID_RENAME_RESOURCE = "org.zenframework.z8.pde.refactoring.rename.resource";
    public static final String IDENTIFIER = "org.zenframework.z8.pde.refactoring.renameResourceProcessor";

    private IResource m_resource;
    private String m_newElementName;
    private String m_comment;

    private RenameModifications m_renameModifications;

    public RenameResourceProcessor(IResource resource) {
        m_resource = resource;

        if(resource != null) {
            setNewElementName(resource.getName());
        }
    }

    @Override
    public void setNewElementName(String newName) {
        m_newElementName = newName;
    }

    @Override
    public String getNewElementName() {
        return m_newElementName;
    }

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public boolean isApplicable() {
        return RefactoringAvailabilityTester.isRenameAvailable(m_resource);
    }

    @Override
    public String getProcessorName() {
        return RefactoringMessages.RenameResourceProcessor_name;
    }

    @Override
    public Object[] getElements() {
        return new Object[] { m_resource };
    }

    @Override
    public String getCurrentElementName() {
        return m_resource.getName();
    }

    public String[] getAffectedProjectNatures() throws CoreException {
        return ResourceProcessors.computeAffectedNatures(m_resource);
    }

    @Override
    public Object getNewElement() {
        return ResourcesPlugin.getWorkspace().getRoot().findMember(createNewPath(getNewElementName()));
    }

    public boolean getUpdateReferences() {
        return true;
    }

    @Override
    public RefactoringParticipant[] loadParticipants(RefactoringStatus status, SharableParticipants shared)
            throws CoreException {
        return m_renameModifications.loadParticipants(status, this, getAffectedProjectNatures(), shared);
    }

    @Override
    public RefactoringStatus checkInitialConditions(IProgressMonitor pm) throws CoreException {
        return RefactoringStatus.create(Resources.checkInSync(m_resource));
    }

    @Override
    public RefactoringStatus checkNewElementName(String newName) {
        IContainer c = m_resource.getParent();

        if(c == null)
            return RefactoringStatus.createFatalErrorStatus(RefactoringMessages.RenameResourceRefactoring_Internal_Error);

        if(c.findMember(newName) != null)
            return RefactoringStatus.createFatalErrorStatus(RefactoringMessages.RenameResourceRefactoring_already_exists);

        if(!c.getFullPath().isValidSegment(newName))
            return RefactoringStatus.createFatalErrorStatus(RefactoringMessages.RenameResourceRefactoring_invalidName);

        RefactoringStatus result = RefactoringStatus.create(c.getWorkspace().validateName(newName, m_resource.getType()));

        if(!result.hasFatalError())
            result.merge(RefactoringStatus.create(c.getWorkspace()
                    .validatePath(createNewPath(newName), m_resource.getType())));

        return result;
    }

    @Override
    public RefactoringStatus checkFinalConditions(IProgressMonitor pm, CheckConditionsContext context) {
        pm.beginTask("", 1);

        try {
            m_renameModifications = new RenameModifications();
            m_renameModifications.rename(m_resource, new RenameArguments(getNewElementName(), getUpdateReferences()));
            ResourceChangeChecker checker = (ResourceChangeChecker)context.getChecker(ResourceChangeChecker.class);
            IResourceChangeDescriptionFactory deltaFactory = checker.getDeltaFactory();
            m_renameModifications.buildDelta(deltaFactory);
            return new RefactoringStatus();
        }
        finally {
            pm.done();
        }
    }

    private String createNewPath(String newName) {
        return m_resource.getFullPath().removeLastSegments(1).append(newName).toString();
    }

    @Override
    public Change createChange(IProgressMonitor pm) {
        pm.beginTask("", 1);

        try {
            Map<String, Object> arguments = new HashMap<String, Object>();

            String project = null;

            if(m_resource.getType() != IResource.PROJECT)
                project = m_resource.getProject().getName();

            String header = Messages.format(RefactoringMessages.RenameResourceChange_descriptor_description, new String[] {
                    m_resource.getFullPath().toString(), getNewElementName() });
            String description = Messages.format(RefactoringMessages.RenameResourceChange_descriptor_description_short,
                    m_resource.getName());

            String comment = new Z8RefactoringDescriptorComment(this, header).asString();

            Z8RefactoringDescriptor descriptor = new Z8RefactoringDescriptor(RenameResourceProcessor.ID_RENAME_RESOURCE,
                    project, description, comment, arguments, RefactoringDescriptor.STRUCTURAL_CHANGE
                            | RefactoringDescriptor.MULTI_CHANGE | RefactoringDescriptor.BREAKING_CHANGE);

            arguments.put(Z8RefactoringDescriptor.ATTRIBUTE_INPUT, m_resource);
            arguments.put(Z8RefactoringDescriptor.ATTRIBUTE_NAME, getNewElementName());

            return new DynamicValidationStateChange(new RenameResourceChange(descriptor, m_resource, getNewElementName(),
                    comment));
        }
        finally {
            pm.done();
        }
    }

    @Override
    public RefactoringStatus initialize(RefactoringArguments arguments) {
        if(arguments instanceof Z8RefactoringArguments) {
            Z8RefactoringArguments extended = (Z8RefactoringArguments)arguments;

            Object object = extended.getAttribute(Z8RefactoringDescriptor.ATTRIBUTE_INPUT);

            if(object != null && object instanceof IResource) {
                m_resource = (IResource)object;

                if(!m_resource.exists()) {
                    return ScriptableRefactoring.createInputFatalStatus(m_resource, getRefactoring().getName(),
                            ID_RENAME_RESOURCE);
                }
            }
            else {
                return RefactoringStatus.createFatalErrorStatus(Messages.format(
                        RefactoringMessages.InitializableRefactoring_argument_not_exist,
                        Z8RefactoringDescriptor.ATTRIBUTE_INPUT));
            }

            String name = (String)extended.getAttribute(Z8RefactoringDescriptor.ATTRIBUTE_NAME);

            if(name != null && !"".equals(name)) {
                setNewElementName(name);
            }
            else {
                return RefactoringStatus.createFatalErrorStatus(Messages.format(
                        RefactoringMessages.InitializableRefactoring_argument_not_exist,
                        Z8RefactoringDescriptor.ATTRIBUTE_NAME));
            }
        }
        else {
            return RefactoringStatus
                    .createFatalErrorStatus(RefactoringMessages.InitializableRefactoring_inacceptable_arguments);
        }

        return new RefactoringStatus();
    }

    @Override
    public boolean canEnableComment() {
        return true;
    }

    @Override
    public String getComment() {
        return m_comment;
    }

    @Override
    public void setComment(String comment) {
        m_comment = comment;
    }
}
