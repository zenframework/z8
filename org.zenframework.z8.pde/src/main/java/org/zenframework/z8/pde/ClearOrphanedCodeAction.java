package org.zenframework.z8.pde;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.RefreshAction;
import org.eclipse.ui.dialogs.CheckedTreeSelectionDialog;

import org.zenframework.z8.compiler.core.IAttribute;
import org.zenframework.z8.compiler.core.IType;
import org.zenframework.z8.compiler.util.Set;
import org.zenframework.z8.compiler.workspace.CompilationUnit;
import org.zenframework.z8.compiler.workspace.Folder;
import org.zenframework.z8.compiler.workspace.Project;
import org.zenframework.z8.compiler.workspace.Resource;
import org.zenframework.z8.compiler.workspace.Workspace;

public class ClearOrphanedCodeAction implements IWorkbenchWindowActionDelegate {

	private IWorkbenchWindow m_window = null;
	private IWorkbenchWindowActionDelegate orgImp = null;

	@Override
	public void dispose() {
	}

	@Override
	public void init(IWorkbenchWindow window) {
		orgImp = new OrganizeAllImportsAction();
		orgImp.init(window);
		m_window = window;
	}

	private Shell getShell() {
		return m_window.getShell();
	}

	@Override
	public void run(IAction action) {
		orgImp.run(action);
		final Set<CompilationUnit> needed = new Set<CompilationUnit>();
		for(Project p : Workspace.getInstance().getProjects())
			collect(needed, p);
		final List<CompilationUnit> neededList = new ArrayList<CompilationUnit>();
		for(CompilationUnit unit : needed)
			neededList.add(unit);

		LabelProvider labelProvider = new LabelProvider() {
			@Override
			public String getText(Object element) {
				return ((Resource)element).getName();
			}
		};

		ITreeContentProvider treeContentProvider = new ITreeContentProvider() {

			@Override
			public Object[] getChildren(Object parentElement) {
				if(parentElement instanceof Folder) {
					Folder folder = (Folder)parentElement;
					Resource[] resources = folder.getMembers();
					List<Resource> list = new ArrayList<Resource>();
					for(int i = 0; i < resources.length; i++) {
						if(resources[i] instanceof CompilationUnit) {
							CompilationUnit compU = (CompilationUnit)resources[i];
							if(!neededList.contains(compU))
								list.add(resources[i]);
						}
						if(resources[i] instanceof Folder) {
							if(hasChildren(resources[i]))
								list.add(resources[i]);
						}
					}
					return list.toArray();
				}
				return new Object[0];
			}

			@Override
			public Object getParent(Object element) {
				return ((Resource)element).getContainer();
			}

			@Override
			public boolean hasChildren(Object element) {
				return getChildren(element).length > 0;
			}

			@Override
			public Object[] getElements(Object inputElement) {
				return ((Workspace)inputElement).getProjects();
			}

			@Override
			public void dispose() {
			}

			@Override
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}

		};

		CheckedTreeSelectionDialog dialog = new CheckedTreeSelectionDialog(getShell(), labelProvider, treeContentProvider);
		dialog.setContainerMode(true);
		dialog.setTitle("�������� ��������������� ����");
		dialog.setMessage("�������� ������� ��� ��������");
		dialog.setInput(Workspace.getInstance());
		if(dialog.open() != Dialog.OK)
			return;
		List<Object> result = Arrays.asList(dialog.getResult());
		final List<Resource> collection = new ArrayList<Resource>();
		for(Project p : Workspace.getInstance().getProjects())
			scan(p, collection, result);

		try {
			PlatformUI.getWorkbench().getProgressService().busyCursorWhile(new IRunnableWithProgress() {
				@Override
				public void run(IProgressMonitor monitor) {
					monitor.beginTask("Deleting resources", collection.size());
					for(Resource r : collection) {
						try {
							monitor.subTask("Deleting " + r.getName());
							r.getResource().delete(true, null);
						} catch(Exception e) {
							Plugin.log(e);
						} finally {
							monitor.worked(1);
						}
					}
					monitor.subTask("Refreshing workspace");
					RefreshAction raction = new RefreshAction(m_window);
					raction.refreshAll();
					monitor.done();
				}

			});
		} catch(Exception e) {
			Plugin.log(e);
		}

		try {
			PlatformUI.getWorkbench().getProgressService().busyCursorWhile(new IRunnableWithProgress() {
				@Override
				public void run(IProgressMonitor monitor) {
					try {
						ResourcesPlugin.getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, monitor);
					} catch(Exception e) {
						Plugin.log(e);
					}
				}

			});
		} catch(Exception e) {
			Plugin.log(e);
		}
	}

	private boolean scan(Folder f, List<Resource> collection, List<Object> result) {
		boolean deleteMe = true;
		List<Resource> folders = new ArrayList<Resource>();
		List<Resource> units = new ArrayList<Resource>();
		for(Resource child : f.getMembers()) {
			if(child instanceof Folder) {
				if(!scan((Folder)child, collection, result))
					deleteMe = false;
				else
					folders.add(child);
			} else if(child instanceof CompilationUnit) {
				if(result.contains(child)) {
					units.add(child);
				} else
					deleteMe = false;
			} else
				deleteMe = false;
		}
		if(deleteMe) {
			collection.removeAll(folders);
			collection.add(f);
		} else
			collection.addAll(units);
		return deleteMe;
	}

	private void collect(Set<CompilationUnit> set, Folder f) {
		for(Resource r : f.getMembers()) {
			if(r instanceof CompilationUnit) {
				CompilationUnit compilationUnit = (CompilationUnit)r;
				IType type = compilationUnit.getReconciledType();
				if(type == null)
					continue;
				if(type.getAttribute(IAttribute.Entry) != null) {
					compilationUnit.getDependencies(set);
					set.add(compilationUnit);
				} else if(type.isNative())
					set.add(compilationUnit);
			}
			if(r instanceof Folder) {
				Folder folder = (Folder)r;
				collect(set, folder);
			}
		}
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
	}

}
