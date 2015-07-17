package org.zenframework.z8.pde.refactoring.processors.move;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IResource;

import org.zenframework.z8.compiler.core.ILanguageElement;
import org.zenframework.z8.compiler.workspace.Resource;
import org.zenframework.z8.compiler.workspace.Workspace;

public class ParentChecker {
    private IResource[] m_resources;
    private ILanguageElement[] m_elements;

    public ParentChecker(IResource[] resources, ILanguageElement[] elements) {
        m_resources = resources;
        m_elements = elements;
    }

    public boolean haveCommonParent() {
        return getCommonParent() != null;
    }

    public Object getCommonParent() {
        if(m_elements.length == 0 && m_resources.length == 0)
            return null;

        if(!resourcesHaveCommonParent() || !elementsHaveCommonParent())
            return null;

        if(m_elements.length == 0) {
            IResource commonResourceParent = getCommonResourceParent();

            Resource converted = Workspace.getInstance().getResource(commonResourceParent);

            if(converted != null && commonResourceParent.exists())
                return converted;
            else
                return commonResourceParent;
        }

        if(m_resources.length == 0)
            return getCommonElementParent();

        IResource commonResourceParent = getCommonResourceParent();
        ILanguageElement commonElementParent = getCommonElementParent();

        Resource converted = Workspace.getInstance().getResource(commonResourceParent);

        if(converted == null || !converted.getResource().isAccessible() || commonElementParent != converted)
            return null;

        return commonElementParent;
    }

    private ILanguageElement getCommonElementParent() {
        return m_elements[0].getParent();
    }

    private IResource getCommonResourceParent() {
        return m_resources[0].getParent();
    }

    private boolean elementsHaveCommonParent() {
        if(m_elements.length == 0)
            return true;

        ILanguageElement firstParent = m_elements[0].getParent();

        for(int i = 1; i < m_elements.length; i++) {
            if(!firstParent.equals(m_elements[i].getParent()))
                return false;
        }
        return true;
    }

    private boolean resourcesHaveCommonParent() {
        if(m_resources.length == 0)
            return true;

        IResource firstParent = m_resources[0].getParent();

        for(int i = 1; i < m_resources.length; i++) {
            if(!firstParent.equals(m_resources[i].getParent()))
                return false;
        }
        return true;
    }

    public IResource[] getResources() {
        return m_resources;
    }

    public ILanguageElement[] getLanguageElements() {
        return m_elements;
    }

    public void removeElementsWithAncestorsOnList(boolean removeOnlyElements) {
        if(!removeOnlyElements) {
            removeResourcesDescendantsOfResources();
            removeResourcesDescendantsOfElements();
        }
        removeElementsDescendantsOfElements();
    }

    private void removeResourcesDescendantsOfElements() {
        List<IResource> subResources = new ArrayList<IResource>(3);

        for(int i = 0; i < m_resources.length; i++) {
            IResource subResource = m_resources[i];

            for(int j = 0; j < m_elements.length; j++) {
                ILanguageElement superElements = m_elements[j];
                if(isDescendantOf(subResource, superElements))
                    subResources.add(subResource);
            }
        }
        removeFromSetToDelete(subResources.toArray(new IResource[subResources.size()]));
    }

    private void removeElementsDescendantsOfElements() {
        List<ILanguageElement> subElements = new ArrayList<ILanguageElement>(3);

        for(int i = 0; i < m_elements.length; i++) {
            ILanguageElement subElement = m_elements[i];

            for(int j = 0; j < m_elements.length; j++) {
                ILanguageElement superElement = m_elements[j];
                if(isDescendantOf(subElement, superElement))
                    subElements.add(subElement);
            }
        }
        removeFromSetToDelete(subElements.toArray(new ILanguageElement[subElements.size()]));
    }

    private void removeResourcesDescendantsOfResources() {
        List<IResource> subResources = new ArrayList<IResource>(3);
        for(int i = 0; i < m_resources.length; i++) {
            IResource subResource = m_resources[i];
            for(int j = 0; j < m_resources.length; j++) {
                IResource superResource = m_resources[j];
                if(isDescendantOf(subResource, superResource))
                    subResources.add(subResource);
            }
        }
        removeFromSetToDelete(subResources.toArray(new IResource[subResources.size()]));
    }

    public static boolean isDescendantOf(IResource subResource, ILanguageElement superElement) {
        IResource parent = subResource.getParent();

        while(parent != null) {
            Resource el = Workspace.getInstance().getResource(parent);

            if(el != null && el.getResource().exists() && el.equals(superElement))
                return true;

            parent = parent.getParent();
        }
        return false;
    }

    public static boolean isDescendantOf(ILanguageElement subElement, ILanguageElement superElement) {
        if(subElement.equals(superElement))
            return false;

        ILanguageElement parent = subElement.getParent();

        while(parent != null) {
            if(parent.equals(superElement))
                return true;
            parent = parent.getParent();
        }
        return false;
    }

    public static boolean isDescendantOf(IResource subResource, IResource superResource) {
        return !subResource.equals(superResource) && superResource.getFullPath().isPrefixOf(subResource.getFullPath());
    }

    public static Object[] setMinus(Object[] setToRemoveFrom, Object[] elementsToRemove) {
        Set<Object> setMinus = new HashSet<Object>(setToRemoveFrom.length - setToRemoveFrom.length);

        setMinus.addAll(Arrays.asList(setToRemoveFrom));
        setMinus.removeAll(Arrays.asList(elementsToRemove));

        return setMinus.toArray(new Object[setMinus.size()]);
    }

    private void removeFromSetToDelete(IResource[] resourcesToNotDelete) {
        m_resources = (IResource[])setMinus(m_resources, resourcesToNotDelete);
    }

    private void removeFromSetToDelete(ILanguageElement[] elementsToNotDelete) {
        m_elements = (ILanguageElement[])setMinus(m_elements, elementsToNotDelete);
    }
}
