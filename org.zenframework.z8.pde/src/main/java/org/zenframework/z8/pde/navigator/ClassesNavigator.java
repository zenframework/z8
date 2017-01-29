package org.zenframework.z8.pde.navigator;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeViewerListener;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeExpansionEvent;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.actions.ActionContext;
import org.eclipse.ui.actions.ActionGroup;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.views.properties.IPropertySheetPage;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.IPropertySourceProvider;
import org.eclipse.ui.views.properties.PropertySheetPage;
import org.zenframework.z8.compiler.core.IType;
import org.zenframework.z8.compiler.workspace.CompilationUnit;
import org.zenframework.z8.compiler.workspace.Resource;
import org.zenframework.z8.compiler.workspace.ResourceListener;
import org.zenframework.z8.compiler.workspace.Workspace;
import org.zenframework.z8.pde.Model;
import org.zenframework.z8.pde.MyPropertySheetEnrty;
import org.zenframework.z8.pde.Plugin;
import org.zenframework.z8.pde.Z8Transfer;
import org.zenframework.z8.pde.build.ReconcileMessageConsumer;
import org.zenframework.z8.pde.navigator.actions.ClassesNavigatorActionGroup;
import org.zenframework.z8.pde.source.InitHelper;

public class ClassesNavigator extends ViewPart implements ResourceListener {
	private static final String PREFERENCE_STRING_LINKED = "Navigator.Linked";

	private boolean m_linkedWithEditor = Plugin.getDefault().getPreferenceStore().getBoolean(PREFERENCE_STRING_LINKED);

	protected TreeViewer m_viewer;
	private ClassesNavigatorActionGroup actionGroup;

	private PropertySheetPage m_propertyPage;
	private MyPropertySheetEnrty m_propertySheetEntry;

	@SuppressWarnings("deprecation")
	@Override
	public void createPartControl(Composite parent) {
		m_viewer = createViewer(parent);
		m_viewer.setInput(Workspace.getInstance());
		m_viewer.setSorter(new Sorter());

		Workspace.getInstance().installResourceListener(this);

		m_viewer.addTreeListener(new ITreeViewerListener() {
			@Override
			public void treeCollapsed(TreeExpansionEvent event) {
			}

			@Override
			public void treeExpanded(TreeExpansionEvent event) {
				Object element = event.getElement();

				if(element instanceof CompilationUnit) {
					final CompilationUnit compilationUnit = (CompilationUnit)element;

					if(compilationUnit.isChanged()) {
						Job job = new Job("Build") {
							@Override
							public IStatus run(IProgressMonitor monitor) {
								ReconcileMessageConsumer consumer = new ReconcileMessageConsumer();
								compilationUnit.getProject().reconcile(compilationUnit.getResource(), null, consumer);
								return Status.OK_STATUS;
							}
						};
						job.schedule();
					}
				}
			}
		});

		initContextMenu();
		makeActions();
		getSite().setSelectionProvider(m_viewer);
		getActionGroup().fillActionBars(getViewSite().getActionBars());

		m_propertyPage = new PropertySheetPage();
		// m_propertyPage.setPropertySourceProvider(new
		// AttributedPropertySourceProvider());
		m_propertyPage.setPropertySourceProvider(new IPropertySourceProvider() {

			@Override
			public IPropertySource getPropertySource(Object object) {
				if(object instanceof IType) {
					IType t = (IType)object;
					Model model = new Model(new InitHelper(t));
					model.setCompilationUnit(t.getCompilationUnit());
					return model;
				}
				if(object instanceof IPropertySource) {
					IPropertySource ips = (IPropertySource)object;
					return ips;
				}
				return null;
			}

		});

		m_propertySheetEntry = new MyPropertySheetEnrty();
		m_propertyPage.setRootEntry(m_propertySheetEntry);
		m_propertySheetEntry.setPropertySourceProvider(new IPropertySourceProvider() {

			@Override
			public IPropertySource getPropertySource(Object object) {
				if(object instanceof IType) {
					IType t = (IType)object;
					Model model = new Model(new InitHelper(t));
					model.setCompilationUnit(t.getCompilationUnit());
					return model;
				}
				if(object instanceof IPropertySource) {
					IPropertySource ips = (IPropertySource)object;
					return ips;
				}
				return null;
			}

		});

		if(isLinkedWithEditor()) {
			IEditorPart editor = Plugin.getActiveEditor();
			if(editor != null && editor.getEditorInput() instanceof FileEditorInput) {
				FileEditorInput input = (FileEditorInput)editor.getEditorInput();
				Resource resource = Workspace.getInstance().getResource(input.getFile());
				if(resource != null) {
					if(resource instanceof CompilationUnit) {
						CompilationUnit unit = (CompilationUnit)resource;
						updateSelection(unit);
					}
				}
			}
		}
		m_viewer.addDragSupport(DND.DROP_COPY | DND.DROP_MOVE, new Transfer[] { Z8Transfer.getInstance() }, new DragSourceListener() {

			@Override
			public void dragFinished(DragSourceEvent event) {
				Z8Transfer.getInstance().setObject(null);
			}

			@Override
			public void dragSetData(DragSourceEvent event) {
				event.data = getObject();
			}

			@Override
			public void dragStart(DragSourceEvent event) {
				Z8Transfer.getInstance().setObject(getObject());
			}

			private Object getObject() {
				return ((IStructuredSelection)m_viewer.getSelection()).getFirstElement();
			}

		});
	}

	@Override
	public void event(final int kind, final Resource resource, final Object object) {
		switch(kind) {
		case RESOURCE_CHANGED:
		case RESOURCE_ADDED:
		case RESOURCE_REMOVED:
		case BUILD_COMPLETE:
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					if(kind == RESOURCE_ADDED || kind == RESOURCE_REMOVED) {
						m_viewer.refresh(resource.getFolder());
					} else {
						m_viewer.refresh(resource);
						m_propertySheetEntry.event(kind, resource, object);
					}
				}
			});
			break;
		default:
			assert (false);
		}
	}

	@Override
	public void dispose() {
		if(getActionGroup() != null) {
			getActionGroup().dispose();
		}
		Workspace.getInstance().uninstallResourceListener(this);
		super.dispose();
	}

	protected void initContextMenu() {
		MenuManager menuManager = new MenuManager("#PopupMenu");
		menuManager.setRemoveAllWhenShown(true);
		menuManager.addMenuListener(new IMenuListener() {
			@Override
			public void menuAboutToShow(IMenuManager manager) {
				fillContextMenu(manager);
			}
		});
		m_viewer.getTree().setMenu(menuManager.createContextMenu(m_viewer.getTree()));
		getSite().registerContextMenu(menuManager, m_viewer);
	}

	protected ActionGroup getActionGroup() {
		return actionGroup;
	}

	IAction m_renameAction;
	IAction m_moveAction;

	protected void fillContextMenu(IMenuManager menu) {
		IStructuredSelection selection = (IStructuredSelection)m_viewer.getSelection();
		getActionGroup().setContext(new ActionContext(selection));
		getActionGroup().fillContextMenu(menu);
	}

	@Override
	public void setFocus() {
		m_viewer.getTree().setFocus();
	}

	protected TreeViewer createViewer(Composite parent) {
		TreeViewer viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		initContentProvider(viewer);
		initLabelProvider(viewer);
		initSorter(viewer);
		initListeners(viewer);
		return viewer;
	}

	protected void initSorter(TreeViewer viewer) {
		// viewer.setSorter(new ViewerSorter());
	}

	protected void initLabelProvider(TreeViewer viewer) {
		viewer.setLabelProvider(new DecoratingLabelProvider(new Z8LabelProvider(true), Plugin.getDefault().getWorkbench().getDecoratorManager().getLabelDecorator()));
	}

	protected void initContentProvider(TreeViewer viewer) {
		viewer.setContentProvider(new NavigatorTreeContentProvider());
	}

	public TreeViewer getTreeViewer() {
		return m_viewer;
	}

	protected void initListeners(TreeViewer viewer) {
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				handleSelectionChanged(event);
			}
		});
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				handleDoubleClick(event);
			}
		});
		viewer.addOpenListener(new IOpenListener() {
			@Override
			public void open(OpenEvent event) {
				handleOpen(event);
			}
		});
		viewer.getControl().addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent event) {
				handleKeyPressed(event);
			}

			@Override
			public void keyReleased(KeyEvent event) {
				handleKeyReleased(event);
			}
		});
	}

	protected void handleOpen(OpenEvent event) {
		IAction action = actionGroup.createOpenAction(((StructuredSelection)event.getSelection()).getFirstElement());

		if(action != null) {
			action.run();
		}
	}

	protected void handleDoubleClick(DoubleClickEvent event) {
	}

	protected void handleSelectionChanged(SelectionChangedEvent event) {
	}

	protected void handleKeyPressed(KeyEvent event) {
	}

	protected void handleKeyReleased(KeyEvent event) {
	}

	protected void makeActions() {
		actionGroup = new ClassesNavigatorActionGroup(this);
	}

	public boolean isLinkedWithEditor() {
		return m_linkedWithEditor;
	}

	public void setLinkedWithEditor(boolean linkWithEditor) {
		this.m_linkedWithEditor = linkWithEditor;
		Plugin.getDefault().getPreferenceStore().setValue(PREFERENCE_STRING_LINKED, linkWithEditor);
	}

	public void updateSelection(CompilationUnit compilationUnit) {
		List<Resource> path = new ArrayList<Resource>();

		Resource resource = compilationUnit;

		if(resource == null)
			return;

		while(resource.getContainer() != null) {
			path.add(0, resource);
			resource = resource.getContainer();
		}

		if(m_linkedWithEditor && compilationUnit.isChanged()) {
			ReconcileMessageConsumer consumer = new ReconcileMessageConsumer();
			compilationUnit.getProject().reconcile(compilationUnit.getResource(), null, consumer);
		}

		m_viewer.expandToLevel(new TreePath(path.toArray()), 2);

		IType type = compilationUnit.getType();

		if(type != null) {
			m_viewer.setSelection(new StructuredSelection(type), true);
		}
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Object getAdapter(Class type) {
		if(type == IPropertySheetPage.class) {
			return m_propertyPage;
		}
		return super.getAdapter(type);
	}
}
