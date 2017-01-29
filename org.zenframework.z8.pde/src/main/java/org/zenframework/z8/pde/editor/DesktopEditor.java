package org.zenframework.z8.pde.editor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.part.FileEditorInput;

import org.zenframework.z8.compiler.core.IAttribute;
import org.zenframework.z8.compiler.core.IMember;
import org.zenframework.z8.compiler.core.IMethod;
import org.zenframework.z8.compiler.core.IPosition;
import org.zenframework.z8.compiler.core.IType;
import org.zenframework.z8.compiler.core.IVariableType;
import org.zenframework.z8.compiler.parser.type.ImportBlock;
import org.zenframework.z8.compiler.parser.type.ImportElement;
import org.zenframework.z8.compiler.parser.type.MemberNestedType;
import org.zenframework.z8.compiler.workspace.CompilationUnit;
import org.zenframework.z8.compiler.workspace.Resource;
import org.zenframework.z8.compiler.workspace.ResourceListener;
import org.zenframework.z8.compiler.workspace.Workspace;
import org.zenframework.z8.pde.TypeSelectDialog;
import org.zenframework.z8.pde.LinkedModel;
import org.zenframework.z8.pde.Model;
import org.zenframework.z8.pde.Plugin;
import org.zenframework.z8.pde.navigator.actions.OpenSourceAction;
import org.zenframework.z8.pde.source.Init2;
import org.zenframework.z8.pde.source.InitHelper;
import org.zenframework.z8.pde.source.MultipleTransactions;

public class DesktopEditor extends EditorPart implements ResourceListener {
	private TreeViewer tree;
	private Z8Editor editor;
	private CompilationUnit compilationUnit;

	public Z8Editor getEditor() {
		return editor;
	}

	public DesktopEditor(Z8Editor editor) {
		this.editor = editor;
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
	}

	@Override
	public void doSaveAs() {
	}

	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		setSite(site);
		setInput(input);
	}

	@Override
	public void setInput(IEditorInput input) {
		super.setInput(input);
		setPartName(input.getName());
		// setTitleImage(icon);
		FileEditorInput cinput = (FileEditorInput)getEditorInput();
		compilationUnit = (CompilationUnit)Workspace.getInstance().getResource(cinput.getFile());
		// tree.setInput(compilationUnit);
	}

	@Override
	public boolean isDirty() {
		return false;
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	@Override
	public void createPartControl(Composite parent) {
		Composite topLevel = new Composite(parent, SWT.NONE);
		GridData data;
		topLevel.setLayout(new GridLayout());
		topLevel.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true));
		Label label = new Label(topLevel, SWT.NONE);
		label.setText("������ ������� ������:");
		tree = new TreeViewer(topLevel);
		data = new GridData(SWT.FILL, SWT.TOP, true, true);
		data.heightHint = 500;
		data.minimumHeight = 100;
		// data.widthHint = 300;
		tree.getTree().setLayoutData(data);

		final Map<IMember, IMember> prevMap = new HashMap<IMember, IMember>();
		final Map<IMember, IMember> nextMap = new HashMap<IMember, IMember>();

		ITreeContentProvider cp = new ITreeContentProvider() {

			@Override
			public Object[] getChildren(Object parentElement) {
				Model m = (Model)parentElement;
				IType t = null;
				if(m.getHelper().what.size() > 0) {
					Init2 init = m.getHelper().findInitDeep();
					if(init.context.getCompilationUnit() != compilationUnit)
						return null;
					if(init.element instanceof IType/* MemberNestedType */) {
						// MemberNestedType mnt = (MemberNestedType)
						// init.element;
						IType mnt = (IType)init.element;
						t = mnt;
					}
				} else
					t = m.getHelper().context;
				if(t == null)
					return new Object[0];
				if(!t.isSubtypeOf("Desktop"))
					return new Object[0];
				List<Model> children = new ArrayList<Model>();
				IMember prev = null;
				for(IMember member : t.getAllMembers()) {
					if(member instanceof IMethod)
						continue;
					IVariableType vt = member.getVariableType();
					if(vt.isArray())
						continue;
					if(vt.getType().isSubtypeOf("Runnable")) {
						prevMap.remove(member);
						nextMap.remove(member);
						children.add(new LinkedModel(m, member.getName()));
						if(prev != null) {
							nextMap.put(prev, member);
							prevMap.put(member, prev);
						}
						prev = member;
					}
				}
				return children.toArray(new Model[0]);
			}

			@Override
			public Object getParent(Object element) {
				if(element instanceof LinkedModel) {
					LinkedModel l = (LinkedModel)element;
					return l.getLink();
				}
				return new Object[0];
			}

			@Override
			public boolean hasChildren(Object element) {
				return getChildren(element).length > 0;
			}

			@Override
			public Object[] getElements(Object inputElement) {
				return getChildren(inputElement);
			}

			@Override
			public void dispose() {
			}

			@Override
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}

		};
		tree.setContentProvider(cp);

		tree.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				Model model = (Model)element;
				String caption = model.getAttributeStringValue(IAttribute.DisplayName);
				if(caption != null && caption.length() > 0)
					return caption;
				return model.getHelper().toQname();
			}
		});

		Model rootModel = new Model(new InitHelper(compilationUnit.getReconciledType()));
		rootModel.setDocument(editor.getDocument());

		tree.setInput(rootModel);

		tree.addDropSupport(DND.DROP_DEFAULT | DND.DROP_MOVE | DND.DROP_COPY, new Transfer[] { LocalSelectionTransfer.getTransfer() }, new DropTargetListener() {
			@Override
			public void dragEnter(org.eclipse.swt.dnd.DropTargetEvent event) {
			}

			@Override
			public void dragLeave(org.eclipse.swt.dnd.DropTargetEvent event) {
			}

			@Override
			public void dragOperationChanged(org.eclipse.swt.dnd.DropTargetEvent event) {
			}

			@Override
			public void dragOver(org.eclipse.swt.dnd.DropTargetEvent event) {
			}

			@Override
			public void drop(org.eclipse.swt.dnd.DropTargetEvent event) {
			}

			@Override
			public void dropAccept(org.eclipse.swt.dnd.DropTargetEvent event) {
				IStructuredSelection sel = (IStructuredSelection)LocalSelectionTransfer.getTransfer().getSelection();
				if(sel.isEmpty())
					return;
				if(sel.size() > 1)
					return;
				Object data = sel.getFirstElement();
				if(data instanceof IType) {
					IType type = (IType)data;
					// IStructuredSelection sel = (IStructuredSelection)
					// tree.getSelection();
					// TreeItem item = tree.getTree().getItem(new Point(event.x,
					// event.y));
					if(!type.isSubtypeOf("Runnable") || (type instanceof MemberNestedType)) {
						event.detail = DND.DROP_NONE;
						return;
					}
					Model m = null;
					if(event.item != null)
						m = (Model)event.item.getData();
					addObject(m, type);
				}
			}

		});

		tree.addDoubleClickListener(new IDoubleClickListener() {

			@Override
			public void doubleClick(DoubleClickEvent event) {
				IStructuredSelection sel = (IStructuredSelection)event.getSelection();
				if(sel.isEmpty())
					return;
				if(sel.getFirstElement() instanceof Model) {
					Model model = (Model)sel.getFirstElement();
					new OpenSourceAction(model.getHelper().getMember()).run();
				}
			}

		});

		Composite buttons = new Composite(topLevel, SWT.NONE);
		buttons.setLayout(new GridLayout(5, false));

		final Button buttonAddObject = new Button(buttons, SWT.NONE);
		buttonAddObject.setText("�������� ������");
		buttonAddObject.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				TypeSelectDialog dialog = new TypeSelectDialog(getSite().getShell(), compilationUnit.getProject(), "Runnable", "�������� ������", "�������� ����� �������:");
				int open = dialog.open();
				if(open == Dialog.OK) {
					Object[] result = dialog.getResult();
					if(result.length == 1) {
						addObject((Model)((TreeSelection)tree.getSelection()).getFirstElement(), (IType)result[0]);
					}
				}
			}

		});

		Button b = new Button(buttons, SWT.NONE);
		b.setText("�������� �����");
		b.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				MultipleTransactions tr = new MultipleTransactions(editor);
				IType where = compilationUnit.getReconciledType();
				String tname = "Desktop";
				String name = tname.toLowerCase();
				if(where.findMember(name) != null) {
					int i = 1;
					String name1 = name;
					while(where.findMember(name1) != null) {
						name1 = name + "_" + i;
						i++;
					}
					name = name1;
				}
				int add = new InitHelper(where).getNewMemberPosition(getEditor().getDocument());
				tr.add(0, add, "\r\n\t" + tname + " " + name + " = class {\r\n\t};");
				tr.execute();
			}

		});

		final Button buttonDelete = new Button(buttons, SWT.NONE);
		buttonDelete.setText("�������");
		buttonDelete.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				TreeSelection ts = (TreeSelection)tree.getSelection();
				MultipleTransactions tr = new MultipleTransactions(editor);
				for(Object o : ts.toArray()) {
					Model m = (Model)o;
					// if (m.getHelper().what.size()!=1) continue;
					IType t = compilationUnit.getReconciledType();
					IMember mem = m.getHelper().getMember();
					if(!mem.getCompilationUnit().getReconciledType().equals(t)) {
						MessageDialog d = new MessageDialog(editor.getSite().getShell(), "��������", null, "���������� ������� ������ " + m.getHelper().toQname() + ", �.�. �� ������ � ������, �������� �� ��������������", MessageDialog.ERROR, new String[] { "��" }, 0);
						d.open();
						return;
					}

					if(!m.getHelper().deleteObject(tr, editor.getSite().getShell()))
						return;
				}
				tr.execute();
			}

		});

		buttonDelete.setEnabled(false);

		final Button buttonUp = new Button(buttons, SWT.NONE);
		buttonUp.setText("�����");
		buttonUp.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				TreeSelection ts = (TreeSelection)tree.getSelection();
				MultipleTransactions tr = new MultipleTransactions(editor);
				final Map<IMember, IMember> sw = new HashMap<IMember, IMember>();
				final Map<IMember, IMember> swrev = new HashMap<IMember, IMember>();
				for(Object o : ts.toArray()) {
					Model m = (Model)o;
					// if (m.getHelper().what.size()!=1) continue;
					IType t = compilationUnit.getReconciledType();
					IMember mem = m.getHelper().getMember();
					if(!mem.getCompilationUnit().getReconciledType().equals(t)) {
						MessageDialog d = new MessageDialog(editor.getSite().getShell(), "�����������", null, "���������� ����������� ������ " + m.getHelper().toQname() + ", �.�. �� ������ � ������, �������� �� ��������������", MessageDialog.ERROR, new String[] { "��" }, 0);
						d.open();
						return;
					}

					IMember prev = prevMap.get(mem);
					if(prev == null) {
						MessageDialog d = new MessageDialog(editor.getSite().getShell(), "�����������", null, "���������� ����������� ������ " + m.getHelper().toQname() + " �����, �.�. �� � ��� ������", MessageDialog.ERROR, new String[] { "��" }, 0);
						d.open();
						return;
					}
					sw.put(prev, mem);
					swrev.put(mem, prev);
				}
				List<IMember> trgs = new ArrayList<IMember>();
				trgs.addAll(sw.values());
				for(IMember trg : trgs) {
					IMember src = swrev.get(trg);
					IMember trg2 = sw.get(trg);
					if(trg2 != null) {
						sw.remove(src);
						sw.remove(trg);
						swrev.remove(trg);
						swrev.remove(trg2);
						sw.put(src, trg2);
						swrev.put(trg2, src);
					}
				}
				for(IMember key : sw.keySet()) {
					IMember val = sw.get(key);
					IPosition posKey = key.getFirstToken().getPosition().union(key.getSourceRange());
					if(key.getInitializer() != null)
						posKey = posKey.union(key.getInitializer().getSourceRange());
					IPosition valKey = val.getFirstToken().getPosition().union(val.getSourceRange());
					if(val.getInitializer() != null)
						valKey = valKey.union(val.getInitializer().getSourceRange());
					try {
						tr.add(posKey.getLength(), posKey.getOffset(), editor.getDocument().get(valKey.getOffset(), valKey.getLength()));
						tr.add(valKey.getLength(), valKey.getOffset(), editor.getDocument().get(posKey.getOffset(), posKey.getLength()));
					} catch(Exception e1) {
						Plugin.log(e1);
					}
				}
				tr.execute();
			}

		});

		buttonUp.setEnabled(false);

		final Button buttonDown = new Button(buttons, SWT.NONE);
		buttonDown.setText("����");
		buttonDown.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				TreeSelection ts = (TreeSelection)tree.getSelection();
				MultipleTransactions tr = new MultipleTransactions(editor);
				final Map<IMember, IMember> sw = new HashMap<IMember, IMember>();
				final Map<IMember, IMember> swrev = new HashMap<IMember, IMember>();
				for(Object o : ts.toArray()) {
					Model m = (Model)o;
					// if (m.getHelper().what.size()!=1) continue;
					IType t = compilationUnit.getReconciledType();
					IMember mem = m.getHelper().getMember();
					if(!mem.getCompilationUnit().getReconciledType().equals(t)) {
						MessageDialog d = new MessageDialog(editor.getSite().getShell(), "�����������", null, "���������� ����������� ������ " + m.getHelper().toQname() + ", �.�. �� ������ � ������, �������� �� ��������������", MessageDialog.ERROR, new String[] { "��" }, 0);
						d.open();
						return;
					}

					IMember next = nextMap.get(mem);
					if(next == null) {
						MessageDialog d = new MessageDialog(editor.getSite().getShell(), "�����������", null, "���������� ����������� ������ " + m.getHelper().toQname() + " ����, �.�. �� � ��� ���������", MessageDialog.ERROR, new String[] { "��" }, 0);
						d.open();
						return;
					}
					sw.put(mem, next);
					swrev.put(next, mem);
				}
				List<IMember> trgs = new ArrayList<IMember>();
				trgs.addAll(sw.values());
				for(IMember trg : trgs) {
					IMember src = swrev.get(trg);
					IMember trg2 = sw.get(trg);
					if(trg2 != null) {
						sw.remove(src);
						sw.remove(trg);
						swrev.remove(trg);
						swrev.remove(trg2);
						sw.put(src, trg2);
						swrev.put(trg2, src);
					}
				}
				for(IMember key : sw.keySet()) {
					IMember val = sw.get(key);
					IPosition posKey = key.getFirstToken().getPosition().union(key.getSourceRange());
					if(key.getInitializer() != null)
						posKey = posKey.union(key.getInitializer().getSourceRange());
					IPosition valKey = val.getFirstToken().getPosition().union(val.getSourceRange());
					if(val.getInitializer() != null)
						valKey = valKey.union(val.getInitializer().getSourceRange());
					try {
						tr.add(posKey.getLength(), posKey.getOffset(), editor.getDocument().get(valKey.getOffset(), valKey.getLength()));
						tr.add(valKey.getLength(), valKey.getOffset(), editor.getDocument().get(posKey.getOffset(), posKey.getLength()));
					} catch(Exception e1) {
						Plugin.log(e1);
					}
				}
				tr.execute();
			}

		});

		buttonDown.setEnabled(false);

		tree.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				TreeSelection sel = (TreeSelection)event.getSelection();
				buttonDelete.setEnabled(!sel.isEmpty());
				buttonAddObject.setEnabled(sel.size() < 2);
				buttonUp.setEnabled(!sel.isEmpty());
				buttonDown.setEnabled(!sel.isEmpty());
			}

		});

		getSite().setSelectionProvider(getSelectionProvider());
	}

	private boolean addObject(Model to, IType type) {
		IType where = null;
		if(to == null) {
			where = compilationUnit.getReconciledType();
		} else {
			Model m = to;
			while(where == null && m != null) {
				if(m.getHelper().what.size() == 0) {
					where = m.getHelper().context;
					break;
				}
				Init2 init = m.getHelper().findInitDeep();
				if(init.context.getCompilationUnit() == compilationUnit) {
					if(init.element instanceof MemberNestedType) {
						MemberNestedType mnt = (MemberNestedType)init.element;
						if(mnt.isSubtypeOf("Desktop"))
							where = mnt;

					}
				}
				m = ((LinkedModel)m).getLink();
			}
		}
		if(where == null)
			where = compilationUnit.getReconciledType();
		MultipleTransactions tr = new MultipleTransactions(editor);
		addMember(tr, where, type);
		tr.execute();
		return true;
	}

	private String addMember(MultipleTransactions tr, IType where, IType newtype) {
		String tname = newtype.getUserName();
		String name = tname.toLowerCase();
		if(where.findMember(name) != null) {
			int i = 1;
			String name1 = name;
			while(where.findMember(name1) != null) {
				name1 = name + "_" + i;
				i++;
			}
			name = name1;
		}
		IType type = where;
		int add = new InitHelper(type).getNewMemberPosition(getEditor().getDocument());
		tr.add(0, add, "\r\n\t" + newtype.getUserName() + " " + name + ";");
		boolean newImport = true;
		ImportBlock block = type.getCompilationUnit().getReconciledType().getImportBlock();
		if(block != null) {
			for(ImportElement element : block.getImportElements()) {
				if(newtype.getCompilationUnit().equals(element.getImportedUnit())) {
					newImport = false;
					break;
				}
			}
		}
		if(newImport)
			tr.add(0, 0, "import " + newtype.getCompilationUnit().getQualifiedName() + ";\r\n");
		return name;

	}

	@Override
	public void setFocus() {
	}

	public ISelectionProvider getSelectionProvider() {
		return tree;
	}

	@Override
	public void event(int type, Resource resource, Object object) {
		((Model)tree.getInput()).getHelper().context = compilationUnit.getReconciledType();
		tree.refresh();
	}

}
