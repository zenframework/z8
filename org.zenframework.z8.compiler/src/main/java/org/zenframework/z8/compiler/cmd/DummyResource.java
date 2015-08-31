package org.zenframework.z8.compiler.cmd;

import java.net.URI;
import java.util.Map;

import org.eclipse.core.resources.FileInfoMatcherDescription;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IPathVariableManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceFilterDescription;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.resources.IResourceProxyVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourceAttributes;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.jobs.ISchedulingRule;

public class DummyResource implements IResource, IContainer {
    private IResource m_parent;
    private IPath m_path;
    boolean m_isProject;

    public DummyResource() {}

    public DummyResource(IResource parent, IPath path) {
        this(parent, path, false);
    }

    public DummyResource(IResource parent, IPath path, boolean isProject) {
        m_parent = parent;
        m_path = path;
        m_isProject = isProject;
    }

    public IPath getProjectPath() {
        if (m_isProject) {
            return m_path;
        }

        if (m_parent != null) {
            return ((DummyResource) m_parent).getProjectPath();
        }

        throw new IllegalStateException();
        //return null;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof DummyResource) {
            DummyResource resource = (DummyResource) other;
            return m_path.equals(resource.m_path);
        }
        return false;
    }

    @Override
    public String getFileExtension() {
        return m_path.getFileExtension();
    }

    @Override
    public IPath getFullPath() {
        return m_path.removeFirstSegments(getProjectPath().segmentCount() - 1).setDevice(null);
    }

    @Override
    public IPath getLocation() {
        return m_path;
    }

    @Override
    public String getName() {
        return m_path.lastSegment();
    }

    @Override
    public IPath getProjectRelativePath() {
        return getFullPath().removeFirstSegments(1);
    }

    @Override
    public IPath getRawLocation() {
        throw new UnsupportedOperationException();
    }

    @Override
    public IContainer getParent() {
        return (IContainer) m_parent;
    }

    @Override
    public int getType() {
        throw new UnsupportedOperationException();
    }

    @Override
    public IWorkspace getWorkspace() {
        throw new UnsupportedOperationException();
    }

    @Override
    public IProject getProject() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void accept(final IResourceProxyVisitor visitor, int memberFlags) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void accept(IResourceVisitor visitor) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void accept(IResourceVisitor visitor, int depth, boolean includePhantoms) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void accept(IResourceVisitor visitor, int depth, int memberFlags) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clearHistory(IProgressMonitor monitor) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void copy(IPath destination, boolean force, IProgressMonitor monitor) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void copy(IPath destination, int updateFlags, IProgressMonitor monitor) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void copy(IProjectDescription description, boolean force, IProgressMonitor monitor) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void copy(IProjectDescription description, int updateFlags, IProgressMonitor monitor) {
        throw new UnsupportedOperationException();
    }

    @Override
    public IMarker createMarker(String type) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void delete(boolean force, IProgressMonitor monitor) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void delete(int updateFlags, IProgressMonitor monitor) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteMarkers(String type, boolean includeSubtypes, int depth) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean exists() {
        throw new UnsupportedOperationException();
    }

    @Override
    public IMarker findMarker(long id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public IMarker[] findMarkers(String type, boolean includeSubtypes, int depth) {
        throw new UnsupportedOperationException();
    }

    @Override
    public long getLocalTimeStamp() {
        throw new UnsupportedOperationException();
    }

    @Override
    public IMarker getMarker(long id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public long getModificationStamp() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getPersistentProperty(QualifiedName key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ResourceAttributes getResourceAttributes() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getSessionProperty(QualifiedName key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isAccessible() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isDerived() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isLocal(int depth) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isLinked() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isPhantom() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isReadOnly() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isSynchronized(int depth) {
        return true;
    }

    @Override
    public boolean isTeamPrivateMember() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void move(IPath destination, boolean force, IProgressMonitor monitor) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void move(IPath destination, int updateFlags, IProgressMonitor monitor) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void move(IProjectDescription description, boolean force, boolean keepHistory, IProgressMonitor monitor) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void move(IProjectDescription description, int updateFlags, IProgressMonitor monitor) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void refreshLocal(int depth, IProgressMonitor monitor) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void revertModificationStamp(long value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setDerived(boolean isDerived) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setLocal(boolean flag, int depth, IProgressMonitor monitor) {
        throw new UnsupportedOperationException();
    }

    @Override
    public long setLocalTimeStamp(long value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setPersistentProperty(QualifiedName key, String value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setResourceAttributes(ResourceAttributes attributes) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setSessionProperty(QualifiedName key, Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setTeamPrivateMember(boolean isTeamPrivate) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void touch(IProgressMonitor monitor) {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Object getAdapter(Class adapter) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean contains(ISchedulingRule rule) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isConflicting(ISchedulingRule rule) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean exists(IPath path) {
        throw new UnsupportedOperationException();
    }

    @Override
    public IResource findMember(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public IResource findMember(String name, boolean includePhantoms) {
        throw new UnsupportedOperationException();
    }

    @Override
    public IResource findMember(IPath path) {
        throw new UnsupportedOperationException();
    }

    @Override
    public IResource findMember(IPath path, boolean includePhantoms) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getDefaultCharset() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getDefaultCharset(boolean checkImplicit) {
        throw new UnsupportedOperationException();
    }

    @Override
    public IFile getFile(IPath path) {
        throw new UnsupportedOperationException();
    }

    @Override
    public IFolder getFolder(IPath path) {
        throw new UnsupportedOperationException();
    }

    @Override
    public IResource[] members() {
        throw new UnsupportedOperationException();
    }

    @Override
    public IResource[] members(boolean includePhantoms) {
        throw new UnsupportedOperationException();
    }

    @Override
    public IResource[] members(int memberFlags) {
        throw new UnsupportedOperationException();
    }

    @Override
    public IFile[] findDeletedMembersWithHistory(int depth, IProgressMonitor monitor) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setDefaultCharset(String charset) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setDefaultCharset(String charset, IProgressMonitor monitor) {
        throw new UnsupportedOperationException();
    }

    @Override
    public IResourceProxy createProxy() {
        throw new UnsupportedOperationException();
    }

    @Override
    public URI getLocationURI() {
        throw new UnsupportedOperationException();
    }

    @Override
    public URI getRawLocationURI() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isLinked(int arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public IResourceFilterDescription createFilter(int arg0, FileInfoMatcherDescription arg1, int arg2, IProgressMonitor arg3)
            throws CoreException {
        throw new UnsupportedOperationException();
    }

    @Override
    public IResourceFilterDescription[] getFilters() throws CoreException {
        throw new UnsupportedOperationException();
    }

    @Override
    public int findMaxProblemSeverity(String arg0, boolean arg1, int arg2) throws CoreException {
        throw new UnsupportedOperationException();
    }

    @Override
    public IPathVariableManager getPathVariableManager() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<QualifiedName, String> getPersistentProperties() throws CoreException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<QualifiedName, Object> getSessionProperties() throws CoreException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isDerived(int arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isHidden() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isHidden(int arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isTeamPrivateMember(int arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isVirtual() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setDerived(boolean arg0, IProgressMonitor arg1) throws CoreException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setHidden(boolean arg0) throws CoreException {
        throw new UnsupportedOperationException();
    }

}
