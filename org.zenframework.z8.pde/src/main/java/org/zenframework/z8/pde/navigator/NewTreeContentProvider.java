package org.zenframework.z8.pde.navigator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.progress.UIJob;

import org.zenframework.z8.compiler.core.IInitializer;
import org.zenframework.z8.compiler.core.ILanguageElement;
import org.zenframework.z8.compiler.core.IMember;
import org.zenframework.z8.compiler.core.IMethod;
import org.zenframework.z8.compiler.core.IType;
import org.zenframework.z8.compiler.parser.expressions.ArrayInitializer;
import org.zenframework.z8.compiler.workspace.CompilationUnit;
import org.zenframework.z8.compiler.workspace.Folder;
import org.zenframework.z8.compiler.workspace.Resource;
import org.zenframework.z8.compiler.workspace.ResourceListener;
import org.zenframework.z8.compiler.workspace.Workspace;
import org.zenframework.z8.pde.build.ReconcileMessageConsumer;

public class NewTreeContentProvider implements ITreeContentProvider {

	private Viewer m_viewer;

	@Override
	public Object[] getChildren(Object element) {
		List<ILanguageElement> result = new ArrayList<ILanguageElement>();

		if(element instanceof Folder) {
			Folder folder = (Folder)element;

			for(Resource resource : folder.getMembers()) {
				if(resource instanceof CompilationUnit || resource instanceof Folder && resource.getName().charAt(0) != '.') {
					result.add(resource);
				}
			}

			return result.toArray(new ILanguageElement[0]);
		}

		if(element instanceof IFile) {
			Resource r = Workspace.getInstance().getResource((IFile)element);
			if(r instanceof CompilationUnit) {
				element = (CompilationUnit)r;

			}
			// element =
			// Workspace.getInstance().getCompilationUnit((IFile)element);

		}

		if(element instanceof CompilationUnit) {
			final CompilationUnit compilationUnit = (CompilationUnit)element;
			if(!compilationUnit.isChanged()) {
				IType type = compilationUnit.getType();

				if(type != null) {
					return new Object[] { type };
				}

				return new Object[0];
			}
			Job job = new Job("Build") {
				@Override
				public IStatus run(IProgressMonitor monitor) {
					ReconcileMessageConsumer consumer = new ReconcileMessageConsumer();
					compilationUnit.getProject().reconcile(compilationUnit.getResource(), null, consumer);
					return Status.OK_STATUS;
				}
			};
			compilationUnit.installResourceListener(new CUListener(compilationUnit));
			job.schedule();

			return new String[] { "..." };
		}

		if(element instanceof IMethod) {
			return null;
		}

		if(element instanceof IMember) {
			IMember member = (IMember)element;
			IInitializer initializer = member.getInitializer();

			if(initializer != null) {
				element = (ILanguageElement)initializer;
			}
		}

		if(element instanceof IInitializer) {
			IInitializer initializer = (IInitializer)element;

			ILanguageElement rightElement = initializer.getRightElement();

			if(rightElement instanceof ArrayInitializer) {
				element = rightElement;
			}
		}

		if(element instanceof ArrayInitializer) {
			ArrayInitializer arrayInitializer = (ArrayInitializer)element;
			return arrayInitializer.getElements();
		}

		if(element instanceof IType) {
			IType type = (IType)element;

			IMember[] members = type.getAllMembers();

			List<IInitializer> initializers = new ArrayList<IInitializer>();
			initializers.addAll(Arrays.asList(type.getInitializers()));

			for(IMember member : members) {
				if(type.isEnum() && member instanceof IMethod) {
					continue;
				}

				for(IInitializer initializer : initializers) {
					if(initializer.getParent() == member) {
						initializers.remove(initializer);
						break;
					}
				}

				result.add(member);
			}

			result.addAll(initializers);
		}

		return result.toArray(new Object[0]);
	}

	@Override
	public Object getParent(Object object) {
		if(object instanceof ILanguageElement) {
			ILanguageElement element = (ILanguageElement)object;
			ILanguageElement result = element.getParent();
			if(result instanceof CompilationUnit) {
				CompilationUnit compilationUnit = (CompilationUnit)result;
				return compilationUnit.getResource();
			}
			return result;
		}
		return null;
	}

	@Override
	public boolean hasChildren(Object object) {
		if(object instanceof IFile) {
			Resource r = Workspace.getInstance().getResource((IFile)object);
			if(r instanceof CompilationUnit) {
				return true;
			}
			return false;
		}
		Object[] children = getChildren(object);
		return children != null && children.length != 0;
	}

	@Override
	public Object[] getElements(Object object) {
		return getChildren(object);
	}

	@Override
	public void dispose() {
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		m_viewer = viewer;
	}

	private class CUListener implements ResourceListener {

		private CompilationUnit compilationUnit;

		public CUListener(CompilationUnit cu) {
			this.compilationUnit = cu;
		}

		@Override
		public void event(int type, Resource resource, Object object) {
			if(type == RESOURCE_CHANGED) {
				UIJob job = new UIJob("refresh") {

					@Override
					public IStatus runInUIThread(IProgressMonitor monitor) {
						((TreeViewer)m_viewer).refresh(compilationUnit.getResource());
						((TreeViewer)m_viewer).expandToLevel(compilationUnit.getResource(), 2);
						return Status.OK_STATUS;
					}

				};
				job.schedule();
			}
		}

	}
}
