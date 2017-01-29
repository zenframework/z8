package org.zenframework.z8.pde.navigator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.actions.ActionContext;
import org.eclipse.ui.actions.ActionGroup;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.views.properties.IPropertySheetPage;
import org.eclipse.ui.views.properties.PropertySheetPage;

import org.zenframework.z8.compiler.core.IAttribute;
import org.zenframework.z8.compiler.core.ISource;
import org.zenframework.z8.compiler.core.IType;
import org.zenframework.z8.compiler.parser.type.DeclaratorNestedType;
import org.zenframework.z8.compiler.parser.type.MemberNestedType;
import org.zenframework.z8.compiler.workspace.Project;
import org.zenframework.z8.compiler.workspace.Resource;
import org.zenframework.z8.compiler.workspace.ResourceListener;
import org.zenframework.z8.compiler.workspace.Workspace;
import org.zenframework.z8.pde.Plugin;
import org.zenframework.z8.pde.build.ReconcileMessageConsumer;
import org.zenframework.z8.pde.navigator.actions.HierarchyNavigatorActionGroup;
import org.zenframework.z8.pde.navigator.actions.OpenCompilationUnitAction;
import org.zenframework.z8.pde.navigator.actions.OpenSourceAction;

public class HierarchyNavigator extends ViewPart implements ResourceListener {

	protected TreeViewer m_viewer;

	private Map<IType, List<IType>> m_map = new HashMap<IType, List<IType>>();

	private IType[] baseTypes = new IType[0];

	private PropertySheetPage m_propertyPage;

	private ActionGroup m_actionGroup;

	private IType m_inputType = null;

	private static final String PREFERENCE_STRING_SUPER = "Hierarchy.Super";

	private boolean m_showSuper = Plugin.getDefault().getPreferenceStore().getBoolean(PREFERENCE_STRING_SUPER);

	@SuppressWarnings("deprecation")
	@Override
	public void createPartControl(Composite parent) {
		// ProjectBuilder.addPropertyChangeListener(this);
		m_viewer = createViewer(parent);
		m_viewer.setSorter(new Sorter());
		initContextMenu();
		makeActions();
		getSite().setSelectionProvider(m_viewer);

		getActionGroup().fillActionBars(getViewSite().getActionBars());

		m_propertyPage = new PropertySheetPage();
		m_propertyPage.setPropertySourceProvider(new AttributedPropertySourceProvider());

		setInput(m_inputType);

		Workspace.getInstance().installResourceListener(this);

		Job job = new Job("") {
			@Override
			public IStatus run(IProgressMonitor monitor) {
				for(Project project : Workspace.getInstance().getProjects()) {
					try

					{
						ReconcileMessageConsumer consumer = new ReconcileMessageConsumer();
						project.reconcile(consumer, monitor);
					} catch(Exception e) {
						return Status.CANCEL_STATUS;
					}
					;
				}

				/*
				 * Display.getDefault().asyncExec(new Runnable() { public void
				 * run() { setInput(m_inputType); } });
				 */

				return Status.OK_STATUS;
			}
		};
		job.schedule();
	}

	@Override
	public void event(int kind, final Resource resource, Object object) {
		if(kind == BUILD_COMPLETE) {
			updateMap();
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					setInput(m_inputType);
				}
			});
		}
	}

	@Override
	public void dispose() {
		if(getActionGroup() != null) {
			getActionGroup().dispose();
		}

		Workspace.getInstance().uninstallResourceListener(this);
		// ProjectBuilder.removePropertyChangeListener(this);
		super.dispose();
	}

	protected void initContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			@Override
			public void menuAboutToShow(IMenuManager manager) {
				fillContextMenu(manager);
			}
		});
		TreeViewer viewer = m_viewer;
		Menu menu = menuMgr.createContextMenu(viewer.getTree());
		viewer.getTree().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);

	}

	protected void fillContextMenu(IMenuManager menu) {
		IStructuredSelection selection = (IStructuredSelection)m_viewer.getSelection();
		getActionGroup().setContext(new ActionContext(selection));
		getActionGroup().fillContextMenu(menu);
	}

	public ActionGroup getActionGroup() {
		return m_actionGroup;
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
	}

	protected void initLabelProvider(TreeViewer viewer) {
		viewer.setLabelProvider(new DecoratingLabelProvider(new Z8LabelProvider(false), Plugin.getDefault().getWorkbench().getDecoratorManager().getLabelDecorator()));
	}

	protected void initContentProvider(TreeViewer viewer) {
		viewer.setContentProvider(new HierarchyContentProvider(m_map));
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
		if(event.getSelection() instanceof IStructuredSelection) {
			IStructuredSelection structuredSelection = (IStructuredSelection)event.getSelection();
			Object element = structuredSelection.getFirstElement();
			if(element instanceof MemberNestedType || element instanceof DeclaratorNestedType) {
				(new OpenSourceAction((ISource)element)).run();
			} else if(element instanceof IType) {
				IType type = (IType)element;
				(new OpenCompilationUnitAction(type.getCompilationUnit())).run();
			}
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
		m_actionGroup = new HierarchyNavigatorActionGroup(this);
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Object getAdapter(Class type) {
		if(type == IPropertySheetPage.class) {
			return m_propertyPage;
		}
		return super.getAdapter(type);
	}

	public void setInput(final IType input) {
		m_inputType = input;

		if(input == null) {
			if(baseTypes.length > 0)
				m_viewer.setInput(baseTypes);
			else
				m_viewer.setInput("build processing");
		} else {
			if(baseTypes.length == 0)
				m_viewer.setInput(input.getUserName());
			else {
				if(!m_showSuper) {
					m_viewer.setInput(new IType[] { input });
				} else {
					m_viewer.setInput(baseTypes);
				}
				m_viewer.expandToLevel(input, 1);
				m_viewer.setSelection(new StructuredSelection(input));
			}
		}

	}

	private void updateMap() {
		List<IType> allTypes = new ArrayList<IType>();

		List<IType> base = new ArrayList<IType>();

		for(Project project : Workspace.getInstance().getProjects()) {

			IType[] types = project.getTypes();

			for(IType type : types) {
				allTypes.add(type);

				base.add(type);

				IType[] nestedTypes = type.getNestedTypes();

				for(IType nestedType : nestedTypes) {
					allTypes.add(nestedType);
				}
			}
		}

		m_map.clear();

		for(IType type : allTypes) {
			IType baseType = type.getBaseType();
			if(baseType != null) {
				List<IType> list = m_map.get(baseType);
				if(list == null) {
					list = new ArrayList<IType>();
					m_map.put(type.getBaseType(), list);
				}
				list.add(type);
				base.remove(type);
			} else if(type.getAttribute(IAttribute.Native) == null) {
				base.remove(type);
			}

		}

		baseTypes = new IType[base.size()];
		base.toArray(baseTypes);
	}

	public boolean isShowSuper() {
		return m_showSuper;
	}

	public void setShowSuper(boolean newSuper) {
		if(m_showSuper != newSuper) {
			m_showSuper = newSuper;
			setInput(m_inputType);
			Plugin.getDefault().getPreferenceStore().setValue(PREFERENCE_STRING_SUPER, newSuper);

		}
	}

	public IType getInput() {
		return m_inputType;
	}

}
