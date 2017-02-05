package org.zenframework.z8.pde.editor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

import org.zenframework.z8.compiler.core.ILanguageElement;
import org.zenframework.z8.compiler.core.IMethod;
import org.zenframework.z8.compiler.core.ISource;
import org.zenframework.z8.compiler.core.IType;
import org.zenframework.z8.compiler.core.IVariable;
import org.zenframework.z8.compiler.workspace.CompilationUnit;
import org.zenframework.z8.compiler.workspace.Resource;
import org.zenframework.z8.compiler.workspace.ResourceListener;
import org.zenframework.z8.compiler.workspace.Workspace;
import org.zenframework.z8.pde.navigator.Z8LabelProvider;
import org.zenframework.z8.pde.navigator.ISourceProvider;
import org.zenframework.z8.pde.navigator.NavigatorTreeContentProvider;
import org.zenframework.z8.pde.refactoring.LanguageElementImageProvider;
import org.zenframework.z8.pde.source.InitHelper;
import org.zenframework.z8.pde.source.MultipleTransactions;
import org.zenframework.z8.pde.source.Transaction;

@SuppressWarnings("deprecation")
public class ContentOutlinePage extends org.eclipse.ui.views.contentoutline.ContentOutlinePage {

	Menu fMenu;

	private final ViewerFilter FILTER = new ViewerFilter() {
		@Override
		public boolean select(Viewer viewer, Object parentElement, Object element) {
			if(element instanceof ILanguageElement) {
				CompilationUnit compilationUnit = ((ILanguageElement)element).getCompilationUnit();
				return compilationUnit == m_compilationUnit;
			}
			if(element instanceof ISource) {
				ISource src = (ISource)element;
				if(src.getCompilationUnit() != m_compilationUnit)
					return false;
			}
			return true;
		}
	};

	private ITextEditor m_editor;
	private CompilationUnit m_compilationUnit;

	private ResourceListener m_listener = new ResourceListener() {

		@Override
		public void event(int type, Resource resource, Object object) {
			if(type == RESOURCE_CHANGED && resource == m_compilationUnit || type == BUILD_COMPLETE) {
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						if(getTreeViewer() != null && !getTreeViewer().getTree().isDisposed()) {
							getTreeViewer().refresh();
							getTreeViewer().expandToLevel(2);
						}
					}
				});
			}

			if(type == RESOURCE_REMOVED && resource == m_compilationUnit) {
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						if(getTreeViewer() != null) {
							setInput(null);
						}
					}
				});
			}
		}

	};

	public ContentOutlinePage(IDocumentProvider provider, ITextEditor editor) {
		super();
		m_editor = editor;
	}

	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);

		Workspace.getInstance().installResourceListener(m_listener);

		TreeViewer treeViewer = getTreeViewer();
		treeViewer.setContentProvider(new NavigatorTreeContentProvider());
		treeViewer.setLabelProvider(new Z8LabelProvider(false));
		treeViewer.addSelectionChangedListener(this);

		treeViewer.setSorter(new ViewerSorter() {
			@Override
			public int compare(Viewer viewer, Object e1, Object e2) {
				if(e1 == e2)
					return 0;

				ISource s1, s2;

				if(e1 instanceof ISource) {
					s1 = (ISource)e1;
				} else {
					return -1;
				}

				if(e2 instanceof ISource) {
					s2 = (ISource)e2;
				} else {
					return 1;
				}

				return s1.getPosition().getOffset() > s2.getPosition().getOffset() ? 1 : -1;
			}
		});

		treeViewer.addFilter(FILTER);

		if(m_compilationUnit != null) {
			treeViewer.setInput(m_compilationUnit);
		}
	}

	@Override
	public void dispose() {
		super.dispose();
		Workspace.getInstance().uninstallResourceListener(m_listener);

		if(fMenu != null && !fMenu.isDisposed()) {
			fMenu.dispose();
			fMenu = null;
		}
	}

	@Override
	public void selectionChanged(SelectionChangedEvent event) {

		super.selectionChanged(event);
		IStructuredSelection selection = (IStructuredSelection)event.getSelection();
		Object element = selection.getFirstElement();
		if(element == null)
			return;
		if(element instanceof ISourceProvider) {
			ISourceProvider prov = (ISourceProvider)element;
			element = prov.getSource();
		}
		if(element instanceof ISource) {
			ISource source = (ISource)element;
			m_editor.getSelectionProvider().setSelection(new TextSelection(source.getPosition().getOffset(), source.getPosition().getLength()));
		}
		return;

	}

	public void setInput(FileEditorInput input) {
		if(input != null) {
			m_compilationUnit = Workspace.getInstance().getCompilationUnit(input.getFile());
		} else {
			m_compilationUnit = null;
		}

		update();
	}

	public void update() {
		TreeViewer viewer = getTreeViewer();

		if(viewer != null) {
			Control control = viewer.getTree();

			if(control != null && !control.isDisposed()) {
				control.setRedraw(false);
				viewer.setInput(m_compilationUnit);
				control.setRedraw(true);
			}
		}
	}

	@Override
	public void makeContributions(IMenuManager menuManager, IToolBarManager toolBarManager, IStatusLineManager statusLineManager) {

		// menuManager.setRemoveAllWhenShown(true);

		// menuManager.add(new Action("1"){});

		menuManager.addMenuListener(new IMenuListener() {

			@Override
			public void menuAboutToShow(final IMenuManager manager) {
				manager.removeAll();
				final CompilationUnit unit = Workspace.getInstance().getCompilationUnit(((FileEditorInput)m_editor.getEditorInput()).getFile());
				final IType type = unit.getReconciledType();
				final LanguageElementImageProvider leip = new LanguageElementImageProvider();

				List<IAction> actions = new ArrayList<IAction>();
				IType curr = type.getBaseType();
				while(curr != null) {
					for(final IMethod m : curr.getMethods())
						if(m.getBody() == null && m.isVirtual()) {
							IType curr1 = type;
							boolean found = false;
							while(curr1 != curr) {
								for(IMethod m1 : curr1.getMethods())
									if(m1.getSignature().equals(m.getSignature())) {
										found = true;
										break;
									}
								curr1 = curr1.getBaseType();
							}
							if(!found) {
								String method = m.getName() + "(";
								boolean first = true;
								for(IVariable v : m.getParameters()) {
									method += (first ? "" : ", ") + v.getSignature() + " " + v.getName();
									first = false;
								}
								method += ")  ";
								method += m.getVariableType().getSignature();
								actions.add(new Action(method) {
									@Override
									public ImageDescriptor getImageDescriptor() {
										return leip.getImageDescriptor(m, LanguageElementImageProvider.SMALL_ICONS);
									}

									@Override
									public void run() {
										MultipleTransactions mt = new MultipleTransactions((Z8Editor)m_editor);
										InitHelper.checkImport(mt, type, m.getVariableType().getType());
										String call = "super.";
										call += m.getName() + "(";
										String sign = m.getVariableType().getSignature() + " " + m.getName() + "(";
										if(m.isPublic())
											sign = "public " + sign;
										if(m.isProtected())
											sign = "protected " + sign;
										if(m.isPrivate())
											sign = "private " + sign;
										if(m.isStatic())
											sign = "static " + sign;
										if(m.isVirtual())
											sign = "virtual " + sign;
										boolean first = true;
										for(IVariable arg : m.getParameters()) {
											if(!first) {
												sign += ", ";
												call += ", ";
											} else
												first = false;
											call += arg.getName();
											sign += arg.getVariableType().getSignature() + " " + arg.getName();
											InitHelper.checkImport(mt, type, arg.getVariableType().getType());
										}
										call += ");";
										sign += ") {";
										String methodString = "\r\n\t" + sign + "\r\n\t\t";
										int len = methodString.length();
										methodString = methodString + "\r\n\t\t" + (m.getVariableType().getSignature().equals("void") ? "" : "return ") + call;
										List<Transaction> trs = mt.getTransactions();
										for(int i = 0; i < trs.size(); i++) {
											len += trs.get(i).getWhat().length();
										}
										methodString += "\r\n\t}";
										int posToInsert = new InitHelper(type).getNewMethodPosition(((Z8Editor)m_editor).getDocument(), m.getName());
										mt.add(0, posToInsert, methodString);
										mt.execute();

										m_editor.setFocus();
										m_editor.getSelectionProvider().setSelection(new TextSelection(posToInsert + len, 0));

									}
								});
								// methodsStrings.add(method);
								// methods.add(m);
							}
						}
					curr = curr.getBaseType();
				}
				IAction[] as = actions.toArray(new IAction[0]);
				Arrays.sort(as, new Comparator<IAction>() {

					@Override
					public int compare(IAction o1, IAction o2) {
						return o1.getText().compareToIgnoreCase(o2.getText());
					}

				});
				for(IAction a : as)
					manager.add(a);
			}

		});

	}

}
