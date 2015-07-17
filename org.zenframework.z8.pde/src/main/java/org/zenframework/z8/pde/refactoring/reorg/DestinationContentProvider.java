package org.zenframework.z8.pde.refactoring.reorg;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

import org.zenframework.z8.compiler.core.ILanguageElement;
import org.zenframework.z8.compiler.workspace.Project;
import org.zenframework.z8.compiler.workspace.Workspace;

public final class DestinationContentProvider extends StandardLanguageElementContentProvider {
    private IReorgDestinationValidator fValidator;

    public DestinationContentProvider(IReorgDestinationValidator validator) {
        super(true);
        fValidator = validator;
    }

    @Override
    public boolean hasChildren(Object object) {
        if(object instanceof ILanguageElement) {
            ILanguageElement element = (ILanguageElement)object;

            if(!fValidator.canChildrenBeDestinations(element))
                return false;
        }
        else if(object instanceof IResource) {
            IResource resource = (IResource)object;
            if(!fValidator.canChildrenBeDestinations(resource))
                return false;
        }
        return super.hasChildren(object);
    }

    @Override
    public Object[] getChildren(Object object) {
        Object[] children = doGetChildren(object);

        ArrayList<Object> result = new ArrayList<Object>(children.length);

        for(int i = 0; i < children.length; i++) {
            if(children[i] instanceof ILanguageElement) {
                ILanguageElement element = (ILanguageElement)children[i];
                if(fValidator.canElementBeDestination(element) || fValidator.canChildrenBeDestinations(element))
                    result.add(element);
            }
            else if(children[i] instanceof IResource) {
                IResource resource = (IResource)children[i];
                if(fValidator.canElementBeDestination(resource) || fValidator.canChildrenBeDestinations(resource))
                    result.add(resource);
            }
        }
        return result.toArray();
    }

    private Object[] doGetChildren(Object parentElement) {
        if(parentElement instanceof IContainer) {
            final IContainer container = (IContainer)parentElement;
            return getResources(container);
        }
        return super.getChildren(parentElement);
    }

    private Object[] getResources(IContainer container) {
        try {
            IResource[] members = container.members();

            Project javaProject = Workspace.getInstance().getProject(container.getProject());

            if(javaProject == null || !javaProject.getResource().exists())
                return members;

            List<IResource> nonJavaResources = new ArrayList<IResource>();

            for(int i = 0; i < members.length; i++) {
                IResource member = members[i];

                if(Workspace.getInstance().getResource(member) != null) {
                    nonJavaResources.add(member);
                }
            }
            return nonJavaResources.toArray();
        }
        catch(CoreException e) {
            return NO_CHILDREN;
        }
    }
}
