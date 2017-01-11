package org.zenframework.z8.pde;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.zenframework.z8.compiler.workspace.Folder;
import org.zenframework.z8.compiler.workspace.Project;
import org.zenframework.z8.compiler.workspace.Resource;
import org.zenframework.z8.compiler.workspace.Workspace;

public class WorkspaceInitializer {
	static public void run() {
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();

		final Workspace workspace = Workspace.initialize(workspaceRoot);

		IResourceChangeListener resourceChangeListener = new IResourceChangeListener() {
			@Override
			public void resourceChanged(IResourceChangeEvent event) {
				IResourceDeltaVisitor visitor = new IResourceDeltaVisitor() {
					@Override
					public boolean visit(IResourceDelta delta) {
						IResource resource = delta.getResource();
						IProject iProject = resource.getProject();

						if(resource instanceof IWorkspaceRoot || !isZ8Project(iProject)) {
							return true;
						}

						Project project = workspace.getProject(iProject);

						if(resource.getType() == IResource.FILE) {
							if(Resource.isBLResource(resource)) {
								switch(delta.getKind()) {
								case IResourceDelta.ADDED:
									project.createCompilationUnit(resource);
									break;
								case IResourceDelta.REMOVED:
									project.removeCompilationUnit(resource);
									break;
								case IResourceDelta.CHANGED:
									if((delta.getFlags() & IResourceDelta.CONTENT) != 0) {
										project.updateCompilationUnit(resource);
									}
								}
							}
							if(Resource.isNLSResource(resource)) {
								switch(delta.getKind()) {
								case IResourceDelta.ADDED:
									project.createNLSUnit(resource);
									break;
								case IResourceDelta.REMOVED:
									project.removeNLSUnit(resource);
									break;
								case IResourceDelta.CHANGED:
									if((delta.getFlags() & IResourceDelta.CONTENT) != 0) {
										project.updateNLSUnit(resource);
									}
								}
							}
						} else if(resource.getType() == IResource.FOLDER) {
							if(project != null) {
								switch(delta.getKind()) {
								case IResourceDelta.REMOVED:
									project.removeFolder(resource);
									return false;
								case IResourceDelta.ADDED:
									project.createFolder(resource);
								}
							}
						} else if(resource.getType() == IResource.PROJECT) {
							switch(delta.getKind()) {
							case IResourceDelta.ADDED:
							case IResourceDelta.CHANGED:
								if(iProject.exists() && iProject.isOpen()) {
									project = workspace.createProject(resource);
									project.setOutputPath(BuildPathManager.getJavaOutputPath(iProject));
									project.setReferencedProjects(getReferencedProjects(iProject));
									return true;
								}
							case IResourceDelta.REMOVED:
								workspace.removeProject(resource);
								return false;
							}
						}

						return true;
					}
				};

				IResourceDelta delta = event.getDelta();

				if(delta != null) {
					try {
						delta.accept(visitor);
					} catch(CoreException e) {
						Plugin.log(e);
					}
				}
			}
		};

		ResourcesPlugin.getWorkspace().addResourceChangeListener(resourceChangeListener);

		for(IProject iProject : workspaceRoot.getProjects()) {
			if(!isZ8Project(iProject)) {
				continue;
			}

			Project project = workspace.createProject(iProject);
			project.setOutputPath(BuildPathManager.getJavaOutputPath(iProject));

			addResources(project);
		}

		for(Project project : Workspace.getInstance().getProjects()) {
			IProject iProject = (IProject)project.getResource();
			project.setReferencedProjects(getReferencedProjects(iProject));
		}
	}

	static IProject[] getReferencedProjects(IProject project) {
		IProject[] referencedProjects = new IProject[0];

		List<IProject> result = new ArrayList<IProject>();

		try {
			referencedProjects = project.getReferencedProjects();
		} catch(CoreException e) {
			Plugin.log(e);
		}

		for(IProject referencedProject : referencedProjects) {
			if(referencedProject.isAccessible()) {
				result.add(referencedProject);
			}
		}

		return result.toArray(new IProject[0]);
	}

	static public boolean isZ8Project(IProject project) {
		try {
			return project.exists() && project.isOpen() && project.hasNature(Z8ProjectNature.Id);
		} catch(CoreException e) {
			Plugin.log(e);
			return false;
		}
	}

	static protected boolean isBlResource(IResource resource) {
		return Resource.isBLResource(resource);
	}

	static protected boolean isNLSResource(IResource resource) {
		return Resource.isNLSResource(resource);
	}

	static protected void addResources(Folder folder) {
		try {
			IContainer iContainer = (IContainer)folder.getResource();
			IResource[] resources = iContainer.members();

			for(IResource resource : resources) {
				if(resource instanceof IContainer) {
					Folder newFolder = folder.createFolder(resource);
					addResources(newFolder);
				} else {
					if(isBlResource(resource)) {
						folder.createCompilationUnit(resource);
					}

					if(isNLSResource(resource)) {
						folder.createNLSUnit(resource);
					}

				}
			}
		} catch(CoreException e) {
			Plugin.log(e);
		}
	}
}
