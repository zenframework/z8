package org.zenframework.z8.pde.refactoring.processors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IResource;

import org.zenframework.z8.compiler.workspace.CompilationUnit;
import org.zenframework.z8.compiler.workspace.Workspace;

public class SearchResultGroup {
    private final IResource m_resource;
    private final List<SearchMatch> m_searchMatches;

    class SearchMatch {}

    public SearchResultGroup(IResource res, SearchMatch[] matches) {
        m_resource = res;
        m_searchMatches = new ArrayList<SearchMatch>(Arrays.asList(matches));
    }

    public void add(SearchMatch match) {
        m_searchMatches.add(match);
    }

    public IResource getResource() {
        return m_resource;
    }

    public SearchMatch[] getSearchResults() {
        return m_searchMatches.toArray(new SearchMatch[m_searchMatches.size()]);
    }

    public static IResource[] getResources(SearchResultGroup[] searchResultGroups) {
        Set<IResource> resourceSet = new HashSet<IResource>(searchResultGroups.length);

        for(int i = 0; i < searchResultGroups.length; i++) {
            resourceSet.add(searchResultGroups[i].getResource());
        }
        return (IResource[])resourceSet.toArray(new IResource[resourceSet.size()]);
    }

    public CompilationUnit getCompilationUnit() {
        if(getSearchResults() == null || getSearchResults().length == 0)
            return null;

        return Workspace.getInstance().getCompilationUnit(m_resource);
        //		return SearchUtils.getCompilationUnit(getSearchResults()[0]);
    }
}
