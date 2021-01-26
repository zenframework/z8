package org.zenframework.z8.compiler.cmd;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.FileInfoMatcherDescription;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceFilterDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.zenframework.z8.compiler.file.File;

public class DummyContainer extends DummyResource implements IContainer {

	private final Map<String, DummyResource> members = new HashMap<String, DummyResource>();
	private final boolean isProject;

	public DummyContainer() {
		this(null, null);
	}

	public DummyContainer(DummyContainer parent, IPath path) {
		this(parent, path, false);
	}

	public DummyContainer(DummyContainer parent, IPath path, boolean isProject) {
		super(parent, path);
		this.isProject = isProject;
	}

	public IPath getProjectPath() {
		return isProject ? path : super.getProjectPath();
	}

	@Override
	public boolean exists(IPath path) {
		return File.fromPath(this.path.append(path)).exists();
	}

	@Override
	public IResource findMember(String name) {
		return getMembers().get(name);
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
		return getMembers().values().toArray(new IResource[members.size()]);
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
	public IResourceFilterDescription createFilter(int arg0, FileInfoMatcherDescription arg1, int arg2, IProgressMonitor arg3) throws CoreException {
		throw new UnsupportedOperationException();
	}

	@Override
	public IResourceFilterDescription[] getFilters() throws CoreException {
		throw new UnsupportedOperationException();
	}

	protected void addMember(DummyResource member) {
		members.put(member.getName(), member);
	}

	protected Map<String, DummyResource> getMembers() {
		if (members.isEmpty()) {
			File[] files = File.fromPath(path).getFiles();
			for (File file : files) {
				if (file.isContainer())
					new DummyContainer(this, file.getPath());
				else
					new DummyResource(this, file.getPath());
			}
		}
		return members;
	}

}
