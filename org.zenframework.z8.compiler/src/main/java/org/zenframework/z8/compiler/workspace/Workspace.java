package org.zenframework.z8.compiler.workspace;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.zenframework.z8.compiler.core.IType;
import org.zenframework.z8.compiler.file.FileException;

public class Workspace extends Folder {
	static private Workspace instance;

	private Map<Integer, Resource> resources = new HashMap<Integer, Resource>();
	private List<CompilationLoop> compilationLoops = new ArrayList<CompilationLoop>();

	protected Workspace(IResource resource) {
		super(null, resource);
	}

	static public Workspace initialize(IResource resource) {
		if(instance == null)
			instance = new Workspace(resource);
		return instance;
	}

	static public Workspace getInstance() {
		return instance;
	}

	private static int getResourceId(IResource resource) {
		IPath location = resource.getLocation();
		return location != null ? location.toString().hashCode() : 0;
	}

	private static int getResourceId(Resource resource) {
		return getResourceId(resource.getResource());
	}

	protected void addResource(Resource resource) {
		int resourceId = getResourceId(resource);
		Resource current = resources.get(resourceId);
		if (current == null || !Resource.isProject(current))
			resources.put(resourceId, resource);
	}

	protected void removeResource(Resource resource) {
		resources.remove(getResourceId(resource));
	}

	public Resource getResource(IResource resource) {
		return resources.get(getResourceId(resource));
	}

	@Override
	public CompilationUnit getCompilationUnit(IResource resource) {
		return (CompilationUnit)getResource(resource);
	}

	@Override
	public NlsUnit getNLSUnit(IResource resource) {
		return (NlsUnit)getResource(resource);
	}

	@Override
	public Folder getFolder(IResource resource) {
		return (Folder)getResource(resource);
	}

	public Project createProject(IResource resource) {
		return createProject(resource, loadProjectProperties(resource));
	}

	public Project createProject(IResource resource, ProjectProperties properties) {
		Resource projectResource = getResource(resource);
		Project project = projectResource instanceof Project ? (Project) projectResource : new Project(this, resource);
		project.setProperties(properties);
		return project;
	}

	public void removeProject(IResource resource) {
		removeFolder(resource);
	}

	public Project[] getProjects() {
		Resource[] members = getMembers();

		if(members.length == 0)
			return new Project[0];

		Project[] projects = new Project[members.length];

		for(int i = 0; i < members.length; i++)
			projects[i] = (Project)members[i];

		return projects;
	}

	public Project getProject(IResource resource) {
		return (Project)getResource(resource);
	}

	public IType lookupType(String qualifiedTypeName) {
		return lookupType(qualifiedTypeName, null);
	}

	public IType lookupType(final String qualifiedTypeName, String qualifiedNestedTypeName) {
		final CompilationUnit[] unit = new CompilationUnit[1];

		Workspace.getInstance().iterate(new ResourceVisitor() {
			@Override
			public boolean visit(CompilationUnit compilationUnit) {
				IType type = compilationUnit.getType();

				if(compilationUnit.getQualifiedName().equals(qualifiedTypeName) || type != null && type.getQualifiedJavaName().equals(qualifiedTypeName)) {
					unit[0] = compilationUnit;
					return false;
				}
				return true;
			}
		});

		if(unit[0] != null) {
			IType type = unit[0].getReconciledType();

			if(qualifiedNestedTypeName != null)
				return type.lookupNestedType(qualifiedNestedTypeName);

			return type;
		}

		return null;
	}

	protected CompilationLoop getCompilationLoop() {
		if(compilationLoops.size() != 0) {
			CompilationLoop compilationLoop = compilationLoops.get(compilationLoops.size() - 1);

			if(compilationLoop.isOpen())
				return compilationLoop;
		}

		CompilationLoop compilationLoop = new CompilationLoop();
		compilationLoops.add(compilationLoop);
		return compilationLoop;
	}

	protected boolean runCompilationLoop(CompilationUnit compilationUnit) {
		CompilationLoop compilationLoop = getCompilationLoop();

		boolean finished = compilationLoop.run(compilationUnit);

		if(finished)
			compilationLoops.remove(compilationLoop);

		return finished;
	}

	static public void addResources(Folder folder) throws CoreException {
		addResources(folder, new Path(""), folder.getProject().getSourcePaths());
	}

	static private void addResources(Folder folder, IPath folderRelativePath, IPath[] sourcePaths) throws CoreException {
		IContainer iContainer = (IContainer) folder.getResource();
		for (IResource resource : iContainer.members()) {
			IPath relativePath = folderRelativePath.append(resource.getName());
			boolean isContainer = resource instanceof IContainer;
			for (IPath sourcePath : sourcePaths) {
				boolean sourceInResource = relativePath.isPrefixOf(sourcePath);
				boolean resourceInSource = sourcePath.isPrefixOf(relativePath);
				if (isContainer && (sourceInResource || resourceInSource)) {
					Folder newFolder = folder.createFolder(resource);
					addResources(newFolder, relativePath, sourcePaths);
				} else if (!isContainer && resourceInSource) {
					if (Resource.isBLResource(resource))
						folder.createCompilationUnit(resource);
					else if (Resource.isNLSResource(resource))
						folder.createNLSUnit(resource);
				}
			}
		}
	}

	static private ProjectProperties loadProjectProperties(IResource project) {
		ProjectProperties properties = new ProjectProperties();
		properties.setProjectPath(project.getLocation());
		try {
			properties.load();
		} catch (FileException e) {
			System.out.println("Warning: Can't load project properties from '" + properties.getProjectPath() + "': " + e.getMessage());
		}
		return properties;
	}

}
