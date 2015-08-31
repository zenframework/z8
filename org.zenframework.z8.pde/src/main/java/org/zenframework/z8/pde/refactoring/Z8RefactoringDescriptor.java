package org.zenframework.z8.pde.refactoring;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringContribution;
import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.RefactoringArguments;

import org.zenframework.z8.pde.Plugin;
import org.zenframework.z8.pde.refactoring.messages.Messages;
import org.zenframework.z8.pde.refactoring.messages.RefactoringMessages;

public class Z8RefactoringDescriptor extends RefactoringDescriptor {
    public static final String ATTRIBUTE_ELEMENT = "element";
    public static final String ATTRIBUTE_INPUT = "input";
    public static final String ATTRIBUTE_NAME = "name";
    public static final String ATTRIBUTE_SELECTION = "selection";
    private static final String ATTRIBUTE_VERSION = "version";
    private static final String VALUE_VERSION_1_0 = "1.0";

    private Map<String, Object> m_arguments;
    private Z8RefactoringContribution m_contribution;

    public Z8RefactoringDescriptor(Z8RefactoringContribution contribution, String id, String project, String description,
            String comment, Map<String, Object> arguments, int flags) {
        super(id, project, description, comment, flags);
        Assert.isNotNull(arguments);
        m_contribution = contribution;
        m_arguments = arguments;
    }

    public Z8RefactoringDescriptor(String id, String project, String description, String comment,
            Map<String, Object> arguments, int flags) {
        this(null, id, project, description, comment, arguments, flags);
    }

    public RefactoringArguments createArguments() {
        Z8RefactoringArguments arguments = new Z8RefactoringArguments(getProject());

        for(Map.Entry<String, Object> entry : m_arguments.entrySet()) {
            String name = entry.getKey();
            Object value = entry.getValue();

            if(name != null && !"".equals(name) && value != null) {
                arguments.setAttribute(name, value);
            }
        }

        return arguments;
    }

    @Override
    public Refactoring createRefactoring(RefactoringStatus status) throws CoreException {
        Refactoring refactoring = null;
        if(m_contribution != null) {
            refactoring = m_contribution.createRefactoring(this);
        }
        else {
            RefactoringContribution contribution = RefactoringCore.getRefactoringContribution(getID());

            if(contribution instanceof Z8RefactoringContribution) {
                m_contribution = (Z8RefactoringContribution)contribution;
                refactoring = m_contribution.createRefactoring(this);
            }
        }

        if(refactoring != null) {
            if(refactoring instanceof IScriptableRefactoring) {
                RefactoringStatus result = ((IScriptableRefactoring)refactoring).initialize(createArguments());

                if(result.hasFatalError()) {
                    throw new CoreException(new Status(IStatus.ERROR, Plugin.PLUGIN_ID, 0,
                            result.getMessageMatchingSeverity(RefactoringStatus.FATAL), null));
                }

                status.merge(result);
            }
            else
                throw new CoreException(new Status(IStatus.ERROR, Plugin.PLUGIN_ID, 0, Messages.format(
                        RefactoringMessages.RefactoringDescriptor_initialization_error, getDescription()), null));
        }
        return refactoring;
    }

    public Map<String, Object> getArguments() {
        Map<String, Object> map = new HashMap<String, Object>(m_arguments);
        map.put(ATTRIBUTE_VERSION, VALUE_VERSION_1_0);
        return map;
    }

    public Z8RefactoringContribution getContribution() {
        return m_contribution;
    }
}