package org.zenframework.z8.compiler.workspace;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.zenframework.z8.compiler.core.IType;
import org.zenframework.z8.compiler.error.DefaultBuildMessageConsumer;
import org.zenframework.z8.compiler.error.IBuildMessageConsumer;
import org.zenframework.z8.compiler.util.Set;

public class Project extends Folder {
	static final private Object lockObject = new Object();

	static final public int BUILD = 0;
	static final public int RECONCILE = 1;
	static final public int IDLE = 2;

	static private int state = IDLE;
	static private IBuildMessageConsumer messageConsumer;

	private ProjectProperties properties;
	private IPath[] sourcePaths;
	private IPath outputPath;

	public static int filesWritten = 0;
	public static int filesSkipped = 0;

	private List<Project> referencedProjects;
	private Project[] collectedReferencedProjects = null;
	private boolean collectingReferencedProjects;

	private long memoryUsedInitially = 0;

	private ResourceListener resourceListener = new ResourceListener() {
		@Override
		public void event(int type, Resource resource, Object object) {
			if(type == ResourceListener.RESOURCE_REMOVED && resource instanceof Project) {
				Project project = (Project)resource;

				if(referencedProjects != null) {
					if(referencedProjects.remove(project)) {
						collectedReferencedProjects = null;
						updateDependencies();
					}
				}

				if(project.resourceListener == this)
					getWorkspace().uninstallResourceListener(this);
			}
		}

	};

	public Project(Workspace workspace, IResource resource) {
		super(workspace, resource);
		getWorkspace().installResourceListener(resourceListener);
	}

	public void initialize() throws CoreException {
		IPath[] sourcePaths = getSourcePaths();

		if (sourcePaths == null || sourcePaths.length == 0) {
			super.initialize();
			return;
		}

		IContainer container = (IContainer) getResource();
		for (IPath sourcePath : sourcePaths) {
			if (sourcePath.isEmpty()) {
				super.initialize();
			} else if (container.exists(sourcePath)) {
				Folder folder = this;
				IContainer resource = container;
				for (String name : sourcePath.segments())
					folder = folder.createFolder(resource = (IContainer) resource.findMember(name));
				folder.initialize();
			}
		}
	}

	public void setProperties(ProjectProperties properties) {
		this.properties = properties;
		this.sourcePaths = properties.getSourcePaths();
		this.outputPath = properties.getOutputPath();
	}

	public ProjectProperties getProperties() {
		return properties;
	}

	public IPath[] getSourcePaths() {
		return sourcePaths;
	}

	public IPath getOutputPath() {
		return outputPath;
	}

	@Override
	public IPath getPath() {
		return new Path("");
	}

	static private void setBuildState(int state) {
		if(state == BUILD || state == RECONCILE) {
			filesWritten = 0;
			filesSkipped = 0;
		}

		Project.state = state;
	}

	static public boolean isReconciling() {
		return state == Project.RECONCILE;
	}

	static public boolean isBuilding() {
		return state == Project.BUILD;
	}

	static public boolean isIdle() {
		return state == Project.IDLE;
	}

	public IBuildMessageConsumer getMessageConsumer() {
		return messageConsumer;
	}

	public void setMessageConsumer(IBuildMessageConsumer messageConsumer) {
		if(messageConsumer == null)
			messageConsumer = new DefaultBuildMessageConsumer();
		else
			Project.messageConsumer = messageConsumer;
	}

	private void updateDependencies() {
		iterate(new ResourceVisitor() {
			@Override
			public boolean visit(CompilationUnit compilationUnit) {
				compilationUnit.contentChanged();
				compilationUnit.updateDependencies();
				return true;
			}
		});
	}

	public void setReferencedProjects(IResource[] resources) {
		List<Project> projects = new ArrayList<Project>();

		for(IResource resource : resources) {
			Project project = Workspace.getInstance().getProject(resource);
			if(project != null && project != this)
				projects.add(project);
		}

		if(!projects.equals(referencedProjects)) {
			referencedProjects = projects;
			collectedReferencedProjects = null;
			updateDependencies();
		}
	}

	public Project[] getReferencedProjects() {
		if(referencedProjects == null)
			return new Project[0];

		if(collectedReferencedProjects == null) {
			Set<Project> projects = new Set<Project>();
			collectReferencedProjects(projects);
			projects.remove(this);
			collectedReferencedProjects = projects.toArray(new Project[projects.size()]);
		}

		return collectedReferencedProjects;
	}

	public boolean inSourcePaths(IResource resource) {
		IPath fullPath = resource.getFullPath();
		for (IPath sourcePath : getSourcePaths()) {
			sourcePath = getResource().getFullPath().append(sourcePath);
			if (sourcePath.isPrefixOf(fullPath) || fullPath.isPrefixOf(sourcePath))
				return true;
		}
		return false;
	}

	private void collectReferencedProjects(Set<Project> result) {
		if(collectingReferencedProjects)
			return;

		collectingReferencedProjects = true;

		if(referencedProjects != null) {
			for(Project project : referencedProjects) {
				result.add(project);
				project.collectReferencedProjects(result);
			}
		}

		collectingReferencedProjects = false;
	}

	public IType[] getTypes() {
		final List<IType> types = new ArrayList<IType>();

		ResourceVisitor visitor = new ResourceVisitor() {
			@Override
			public boolean visit(CompilationUnit compilationUnit) {
				IType type = compilationUnit.getType();

				if(type != null)
					types.add(type);
				return true;
			}
		};
		iterate(visitor);

		return types.toArray(new IType[types.size()]);
	}

	@Override
	public CompilationUnit[] getCompilationUnits() {
		return getCompilationUnits(false);
	}

	public CompilationUnit[] getCompilationUnits(boolean useReferencedProjects) {
		List<Project> projects = new ArrayList<Project>();

		projects.add(this);

		if(useReferencedProjects) {
			for(Project referencedProject : getReferencedProjects())
				projects.add(referencedProject);
		}

		final List<CompilationUnit> result = new ArrayList<CompilationUnit>();

		for(Project project : projects) {
			project.iterate(new ResourceVisitor() {
				@Override
				public boolean visit(CompilationUnit compilationUnit) {
					result.add(compilationUnit);
					return true;
				}
			});
		}

		return result.toArray(new CompilationUnit[result.size()]);
	}

	public NlsUnit[] getNLSUnits() {
		final List<NlsUnit> contents = new ArrayList<NlsUnit>();

		iterate(new ResourceVisitor() {
			@Override
			public boolean visit(NlsUnit nlsUnit) {
				contents.add(nlsUnit);
				return true;
			}
		});

		return contents.toArray(new NlsUnit[contents.size()]);
	}

	public CompilationUnit[] getDependencies() {
		final Set<CompilationUnit> dependencies = new Set<CompilationUnit>();

		iterate(new ResourceVisitor() {
			@Override
			public boolean visit(CompilationUnit compilationUnit) {
				dependencies.add(compilationUnit);
				compilationUnit.getDependencies(dependencies);
				return true;
			}
		});

		return dependencies.toArray(new CompilationUnit[dependencies.size()]);
	}

	public synchronized void build(IBuildMessageConsumer messageConsumer) {
		buildOrReconcile(Project.BUILD, messageConsumer, new NullProgressMonitor());
	}

	public synchronized void build(IBuildMessageConsumer messageConsumer, IProgressMonitor monitor) {
		buildOrReconcile(Project.BUILD, messageConsumer, monitor);
	}

	public synchronized void reconcile(IBuildMessageConsumer messageConsumer, IProgressMonitor monitor) {
		buildOrReconcile(Project.RECONCILE, messageConsumer, monitor);
	}

	private int getCompilationUnitsCount() {
		final int[] compilationUnitsCount = new int[] { 0 };

		iterate(new ResourceVisitor() {
			@Override
			public boolean visit(CompilationUnit compilationUnit) {
				compilationUnitsCount[0] += 1;
				return true;
			}
		});

		return compilationUnitsCount[0];
	}

	private boolean checkProjectSynchronization(final Project project) {
		final boolean[] isSynchronized = new boolean[] { true };

		iterate(new ResourceVisitor() {
			@Override
			public boolean visit(CompilationUnit compilationUnit) {
				if(!compilationUnit.isSynchronized()) {
					project.error("Some resources is out of sync with the file system. Refresh operation is needed prior the build process.");
					isSynchronized[0] = false;
					return false;
				}
				return true;
			}
		});

		return isSynchronized[0];
	}

	private void buildOrReconcile(int state, final IBuildMessageConsumer messageConsumer, final IProgressMonitor monitor) {
		synchronized(lockObject) {
			long ticks = System.currentTimeMillis();

			setBuildState(state);
			setMessageConsumer(messageConsumer);

			clearMessages();

			System.out.println("Building(reconciling) project " + getName() + "...");
			System.out.println("Heap: " + (ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed() >> 10) + 'K');

			memoryUsedInitially = getMemoryUsage();

			if(!monitor.isCanceled()) {
				boolean resourcesInSync = true;

				resourcesInSync = checkProjectSynchronization(this) && resourcesInSync;

				Project[] projects = getReferencedProjects();

				for(Project project : projects) {
					resourcesInSync = checkProjectSynchronization(project) && resourcesInSync;
				}

				if(resourcesInSync) {
					for(CompilationUnit compilationUnit : getDependencies())
						compilationUnit.updateDependencies();

					try {
						String taskName = "Building project '" + getResource().getFullPath() + "'";
						monitor.beginTask(taskName, getCompilationUnitsCount());
						monitor.setTaskName(taskName);

						iterate(new ResourceVisitor() {
							@Override
							public boolean visit(CompilationUnit compilationUnit) {
								if(monitor.isCanceled()) {
									monitor.done();
									return false;
								}

								monitor.subTask("Compiling " + compilationUnit.getQualifiedName());

								compilationUnit.build(messageConsumer);
								checkMemoryAndClean();

								monitor.worked(1);
								return true;
							}
						});
					} finally {
						monitor.done();
					}

				}

				if(!monitor.isCanceled()) {
					generateStartupCode();

					getMessageConsumer().report(this, getMessages());

					fireResourceEvent(ResourceListener.BUILD_COMPLETE, this, null);

					String result = "Project compiled(reconciled) in " + (System.currentTimeMillis() - ticks) / 1000.0 + " seconds\n" + "Files written " + filesWritten + ", skipped " + filesSkipped + '\n' + "Error(s) " + getMessageConsumer().getErrorCount() + ", warning(s) "
							+ getMessageConsumer().getWarningCount();
					System.out.println(result);
				} else
					System.out.println("Building(reconciling) project " + getName() + " cancelled by user");
			}

			setMessageConsumer(null);
			setBuildState(Project.IDLE);
		}
	}

	private long getMemoryUsage() {
		MemoryMXBean memoryMxBean = ManagementFactory.getMemoryMXBean();
		return memoryMxBean.getHeapMemoryUsage().getUsed() >> 10;
	}

	public void checkMemoryAndClean() {
		long memoryUsed = getMemoryUsage();

		if(memoryUsed > memoryUsedInitially + 300 * 1024) {
			final boolean[] cleaned = new boolean[] { false };

			getWorkspace().iterate(new ResourceVisitor() {
				@Override
				public boolean visit(CompilationUnit compilationUnit) {
					IType type = compilationUnit.getType();

					if(type != null && !type.isNative()) {
						cleaned[0] = true;
						compilationUnit.cleanup(false); // не удаляем сообщения
														// об ошибках
					}
					return true;
				}
			});

			if(cleaned[0]) {
				System.gc();
				System.out.println("Heap: " + memoryUsed + "K; after GC " + getMemoryUsage() + 'K');
			}
		}
	}

	public synchronized void reconcile(IResource resource, char[] content, IBuildMessageConsumer messageConsumer) {
		synchronized(lockObject) {
			long ticks = System.currentTimeMillis();

			setBuildState(Project.RECONCILE);
			setMessageConsumer(messageConsumer);

			CompilationUnit compilationUnit = getCompilationUnit(resource);

			if(compilationUnit != null) {
				System.out.println("Reconciling resource " + compilationUnit.getName() + "...");
				System.out.println("Heap: " + (ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed() >> 10) + 'K');

				compilationUnit.reconcile(content);

				String result = resource.getName() + " reconciled in " + ((System.currentTimeMillis() - ticks) / 1000.0) + " seconds\n" + "Error(s) " + getMessageConsumer().getErrorCount() + ", warning(s) " + getMessageConsumer().getWarningCount();
				System.out.println(result);
			}

			setMessageConsumer(null);
			setBuildState(Project.IDLE);
		}
	}

	@Override
	public boolean containsError() {
		CompilationUnit[] dependencies = getDependencies();

		for(CompilationUnit compilationUnit : dependencies) {
			if(compilationUnit.containsError())
				return true;
		}

		return false;
	}

	@Override
	public boolean containsWarning() {
		CompilationUnit[] dependencies = getDependencies();

		for(CompilationUnit compilationUnit : dependencies) {
			if(compilationUnit.containsWarning())
				return true;
		}

		return false;
	}

	public boolean generateStartupCode() {
		if(!containsError() && Project.isBuilding() && getOutputPath() != null)
			return StartupCodeGenerator.generate(this);
		return false;
	}

	public CompilationUnit findCompilationUnit(IPath path) {
		return findCompilationUnit(path, new HashSet<Project>());
	}

	private CompilationUnit findCompilationUnit(IPath path, java.util.Set<Project> checked) {
		for (IPath sourcePath : sourcePaths) {
			CompilationUnit unit = getCompilationUnit(sourcePath.append(path));
			if(unit != null)
				return unit;
		}

		checked.add(this);

		Project[] referencedProjects = getReferencedProjects();

		for(Project project : referencedProjects) {
			if (checked.contains(project))
				continue;
			CompilationUnit unit = project.findCompilationUnit(path, checked);
			if(unit != null)
				return unit;
		}

		return null;
	}

	public CompilationUnit[] lookupCompilationUnits(String simpleName) {
		final List<CompilationUnit> result = new ArrayList<CompilationUnit>();

		lookupCompilationUnits(simpleName, result);

		Project[] referencedProjects = getReferencedProjects();

		for(Project project : referencedProjects)
			project.lookupCompilationUnits(simpleName, result);

		return result.toArray(new CompilationUnit[result.size()]);
	}

	private void lookupCompilationUnits(final String simpleName, final List<CompilationUnit> result) {
		ResourceVisitor visitor = new ResourceVisitor() {
			@Override
			public boolean visit(CompilationUnit compilationUnit) {
				if(simpleName.equals(compilationUnit.getSimpleName()))
					result.add(compilationUnit);
				return true;
			}
		};

		iterate(visitor);
	}

}
