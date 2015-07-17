package org.zenframework.z8.pde.refactoring.reorg;

import org.eclipse.core.resources.IResource;

import org.zenframework.z8.compiler.core.ILanguageElement;

public interface IReorgDestinationValidator {
    public boolean canChildrenBeDestinations(IResource resource);

    public boolean canChildrenBeDestinations(ILanguageElement element);

    public boolean canElementBeDestination(IResource resource);

    public boolean canElementBeDestination(ILanguageElement element);
}
