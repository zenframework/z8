package org.zenframework.z8.pde.refactoring.processors;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

import org.zenframework.z8.compiler.core.ILanguageElement;
import org.zenframework.z8.compiler.core.IMember;
import org.zenframework.z8.compiler.workspace.Project;
import org.zenframework.z8.compiler.workspace.Resource;

public class Z8Processors {
    public static Project getProject(ILanguageElement element) {
        if(element == null) {
            return null;
        }

        if(element instanceof Resource) {
            return ((Resource)element).getProject();
        }

        return element.getCompilationUnit().getProject();
    }

    public static String[] computeAffectedNatures(ILanguageElement element) throws CoreException {
        Project project = getProject(element);
        IProject iProject = (IProject)project.getResource();

        if(element instanceof IMember) {
            IMember member = (IMember)element;

            if(member.isPrivate()) {
                return iProject.getDescription().getNatureIds();
            }
        }
        return ResourceProcessors.computeAffectedNatures(iProject);
    }

    public static String[] computeAffectedNaturs(ILanguageElement[] elements) throws CoreException {
        Set<String> result = new HashSet<String>();

        for(int i = 0; i < elements.length; i++) {
            String[] natures = computeAffectedNatures(elements[i]);

            for(int j = 0; j < natures.length; j++) {
                result.add(natures[j]);
            }
        }
        return result.toArray(new String[result.size()]);
    }
}
