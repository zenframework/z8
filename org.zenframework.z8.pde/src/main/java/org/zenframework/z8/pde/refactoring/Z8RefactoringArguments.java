package org.zenframework.z8.pde.refactoring;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.ltk.core.refactoring.participants.RefactoringArguments;

public final class Z8RefactoringArguments extends RefactoringArguments {
    private final Map<String, Object> m_attributes = new HashMap<String, Object>(2);

    private String m_project;

    public Z8RefactoringArguments(String project) {
        m_project = project;
    }

    public Object getAttribute(String name) {
        return m_attributes.get(name);
    }

    public String getProject() {
        return m_project;
    }

    public void setAttribute(String name, Object value) {
        m_attributes.put(name, value);
    }

    public void setProject(String project) {
        m_project = project;
    }

    @Override
    public String toString() {
        return getClass().getName() + m_attributes.toString();
    }
}
