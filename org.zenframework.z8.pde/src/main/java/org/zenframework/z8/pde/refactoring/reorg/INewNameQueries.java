package org.zenframework.z8.pde.refactoring.reorg;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.OperationCanceledException;

import org.zenframework.z8.compiler.workspace.CompilationUnit;
import org.zenframework.z8.compiler.workspace.Folder;

public interface INewNameQueries {
    public INewNameQuery createNewCompilationUnitNameQuery(CompilationUnit cu, String initialSuggestedName)
            throws OperationCanceledException;

    public INewNameQuery createNewResourceNameQuery(IResource res, String initialSuggestedName)
            throws OperationCanceledException;

    public INewNameQuery createNewFolderNameQuery(Folder folder, String initialSuggestedName)
            throws OperationCanceledException;

    public INewNameQuery createNullQuery();

    public INewNameQuery createStaticQuery(String newName) throws OperationCanceledException;
}
