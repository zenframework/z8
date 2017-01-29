package org.zenframework.z8.pde;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.ITreeViewerListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeExpansionEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.CheckedTreeSelectionDialog;
import org.eclipse.ui.internal.WorkbenchMessages;

import org.zenframework.z8.compiler.core.IAttribute;
import org.zenframework.z8.compiler.core.ILanguageElement;
import org.zenframework.z8.compiler.core.IMember;
import org.zenframework.z8.compiler.core.IMethod;
import org.zenframework.z8.compiler.core.IPosition;
import org.zenframework.z8.compiler.core.IType;
import org.zenframework.z8.compiler.parser.type.MemberNestedType;
import org.zenframework.z8.compiler.parser.type.members.Member;
import org.zenframework.z8.compiler.workspace.CompilationUnit;
import org.zenframework.z8.compiler.workspace.Project;
import org.zenframework.z8.compiler.workspace.Workspace;
import org.zenframework.z8.pde.source.Transaction;

@SuppressWarnings("restriction")
public class MergeAction implements IWorkbenchWindowActionDelegate {

	private Map<Object, List<TreeElement>> map;
	private List<Object> selectedObjects;
	private CheckboxTreeViewer treeViewer;
	private List<Object> toDelete;

	private TreeElement[] inputElements;

	private class TreeElement {

		TreeElement(TreeElement parent, Object object) {
			this.parent = parent;
			this.object = object;
			List<TreeElement> other = getOther();
			if(other == null) {
				other = new ArrayList<TreeElement>();
				map.put(object, other);
			}
			other.add(this);
			List<Object> parentObjects = getParentsObjects();
			List<Object> result = new ArrayList<Object>();
			for(Object childObject : createChildren(object)) {
				if(!parentObjects.contains(childObject))
					result.add(childObject);
			}
			childrenObjects = result.toArray();
			children = new TreeElement[childrenObjects.length];
			for(int i = 0; i < childrenObjects.length; i++)
				children[i] = new TreeElement(this, childrenObjects[i]);
		}

		TreeElement parent;
		Object object;
		TreeElement[] children;
		Object[] childrenObjects;

		List<Object> getParentsObjects() {
			ArrayList<Object> result = new ArrayList<Object>();
			TreeElement current = this;
			while(current != null) {
				result.add(0, current.object);
				current = current.parent;
			}
			return result;
		}

		List<TreeElement> getOther() {
			return map.get(object);
		}

		boolean isChecked() {
			return selectedObjects.contains(object) && (parent == null ? true : parent.isChecked());
		}

		boolean isGrayed() {
			if(!isChecked())
				return false;
			/*
			 * for (Object childObject : childrenObjects) if
			 * (!selectedObjects.contains(childObject)) return true; return
			 * false;
			 */
			for(TreeElement child : children) {
				if(!child.isChecked() || child.isGrayed())
					return true;
			}
			return false;
		}

		void deselect() {

			selectedObjects.remove(object);
			for(TreeElement other : getOther()) {
				TreeElement parent1 = other.parent;
				if(parent1 != null) {
					boolean lastChild = true;
					for(Object child : parent1.childrenObjects)
						if(selectedObjects.contains(child)) {
							lastChild = false;
							break;
						}
					if(lastChild)
						parent1.deselect();
				}
			}

		}

		List<Object> internalSelect() {

			List<Object> result = new ArrayList<Object>();
			if(!selectedObjects.contains(object))
				result.add(object);
			boolean newCheck = true;
			for(Object childObject : childrenObjects) {
				if(selectedObjects.contains(childObject)) {
					newCheck = false;
					break;
				}
			}
			if(newCheck)
				for(TreeElement child : children)
					for(Object o : child.internalSelect())
						if(!result.contains(o))
							result.add(o);
			return result;

		}

		void select() {
			selectedObjects.addAll(internalSelect());
			if(parent != null)
				parent.select();
		}

	}

	private class MultipleTransactionsStringBuffer {
		StringBuffer buffer;
		List<Transaction> transes = new ArrayList<Transaction>();

		MultipleTransactionsStringBuffer(StringBuffer buf) {
			buffer = buf;
		}

		void add(Transaction trans) {
			transes.add(trans);
		}

		public void execute() {
			if(transes.size() == 0)
				return;
			List<Transaction> result = new ArrayList<Transaction>();
			for(Transaction t : transes) {
				boolean add = true;
				for(Transaction t1 : transes)
					if(t.getOffset() > t1.getOffset() && t.getOffset() + t.getLength() < t1.getOffset() + t1.getLength())
						add = false;
				if(add)
					result.add(t);
			}
			Transaction[] ts = result.toArray(new Transaction[0]);
			Arrays.sort(ts);
			int size = ts.length;
			for(int i = size - 1; i >= 0; i--) {
				buffer.replace(ts[i].getOffset(), ts[i].getLength() + ts[i].getOffset(), ts[i].getWhat());
			}
		}

	}

	@SuppressWarnings("deprecation")
	private void copyDirectory(File srcPath, File dstPath, IProgressMonitor monitor) {
		if(srcPath.isDirectory()) {
			if(srcPath.getName().equalsIgnoreCase(".svn") || srcPath.getName().equalsIgnoreCase("_svn"))
				return;
			if(!dstPath.exists()) {
				dstPath.mkdir();
			}
			String files[] = srcPath.list();
			for(int i = 0; i < files.length; i++) {
				copyDirectory(new File(srcPath, files[i]), new File(dstPath, files[i]), monitor);
			}
		} else {
			try {
				monitor.subTask("copying " + srcPath.getName());
				InputStream in = new FileInputStream(srcPath);
				OutputStream out = new FileOutputStream(dstPath);
				boolean viaString = false;
				String name = srcPath.getName();
				int length = name.length();
				if(length > 3)
					if(name.substring(length - 3).equalsIgnoreCase(".bl"))
						viaString = true;
				// String s = "";
				StringBuffer s = new StringBuffer();
				// Transfer bytes from in to out
				byte[] buf = new byte[1024];
				int len;
				while((len = in.read(buf)) > 0) {
					if(viaString)
						s.append(new String(buf, 0, len, "CP1251"));
					else
						out.write(buf, 0, len);
					// s = s + new String(buf, 0, len, "CP1251");
				}
				in.close();
				// byte[] bytes = s.getBytes("CP1251");
				// out.write(bytes, 0, bytes.length);
				if(viaString) {
					MultipleTransactionsStringBuffer mtsb = new MultipleTransactionsStringBuffer(s);
					IFile[] files = ResourcesPlugin.getWorkspace().getRoot().findFilesForLocation(new Path(srcPath.getCanonicalPath()));
					IFile file = files[0];
					CompilationUnit cunit = Workspace.getInstance().getCompilationUnit(file);
					IType type = cunit.getReconciledType();
					if(type != null) {
						if(toDelete.contains(type)) {
							IAttribute att = type.getAttribute(IAttribute.Entry);
							// s.delete(att.getPosition().getOffset(),
							// att.getPosition().getOffset()+);
							mtsb.add(new Transaction(att.getPosition().getLength(), att.getPosition().getOffset()));
						}
						if(type.getMembers() != null)
							for(IMember m : type.getMembers())
								if(toDelete.contains(m)) {
									IPosition pos = m.getSourceRange();
									for(IAttribute att : m.getAttributes())
										pos = pos.union(att.getPosition());
									Member member = (Member)m;
									if(member.getAccessTokenPosition() != null)
										pos = pos.union(member.getAccessTokenPosition());
									int end = pos.getOffset() + pos.getLength();
									while(s.charAt(end - 1) != ';')
										end++;
									mtsb.add(new Transaction(end - pos.getOffset(), pos.getOffset()));
								}
						if(type.getNestedTypes() != null)
							for(IType t : type.getNestedTypes())
								if(t instanceof MemberNestedType) {
									MemberNestedType mnt = (MemberNestedType)t;
									if(toDelete.contains(mnt)) {
										IPosition pos = mnt.getSourceRange();
										ILanguageElement element = mnt.getParent();

										if(element != null && element instanceof IMember) {
											IMember m = (IMember)element;
											for(IAttribute att : m.getAttributes())
												pos = pos.union(att.getPosition());
											Member member = (Member)m;
											if(member.getAccessTokenPosition() != null)
												pos = pos.union(member.getAccessTokenPosition());
											int end = pos.getOffset() + pos.getLength();
											while(s.charAt(end - 1) != ';')
												end++;
											mtsb.add(new Transaction(end - pos.getOffset(), pos.getOffset()));
										}
									}
								}
						mtsb.execute();
					}
					out.write(s.toString().getBytes("CP1251"));
				}
				out.close();
			} catch(IOException e) {
				Plugin.log(e);
			} finally {
				monitor.worked(1);
			}
		}
	}

	private int countFiles(File srcPath) {
		if(srcPath.isDirectory()) {
			if(srcPath.getName().equalsIgnoreCase(".svn") || srcPath.getName().equalsIgnoreCase("_svn"))
				return 0;
			String files[] = srcPath.list();
			int result = 0;
			for(int i = 0; i < files.length; i++)
				result += countFiles(new File(srcPath, files[i]));
			return result;
		} else {
			return 1;
		}
	}

	private Shell shell;

	@Override
	public void dispose() {
	}

	@Override
	public void init(IWorkbenchWindow window) {
		shell = window.getShell();
	}

	private Object[] createChildren(Object parentElement) {
		if(parentElement instanceof IMember) {
			IMember member = (IMember)parentElement;
			parentElement = member.getVariableType().getType();
		}
		if(parentElement instanceof IType) {
			IType t = (IType)parentElement;
			if(t.isSubtypeOf("Desktop")) {
				List<Object> members = new ArrayList<Object>();
				for(IMember m : t.getAllMembers()) {
					if(m.getVariableType().isArray())
						continue;
					if(m instanceof IMethod)
						continue;
					if(m.getVariableType().getType().isPrimary())
						continue;
					boolean found = false;
					IType t1 = t;
					while(t1 != null && (!found)) {
						if(t1.getNestedTypes() != null) {
							for(IType mnt : t1.getNestedTypes()) {
								if(mnt instanceof MemberNestedType) {
									if(((MemberNestedType)mnt).getParent() == m) {
										members.add(mnt);
										found = true;
										break;
									}
								}
							}
						}

						t1 = t1.getBaseType();
					}
					if(!found)
						members.add(m);
				}
				return members.toArray();
			}
		}
		return new Object[0];
	}

	@Override
	public void run(IAction action) {
		selectedObjects = new ArrayList<Object>();
		map = new HashMap<Object, List<TreeElement>>();
		inputElements = null;

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

		LabelProvider labelProvider = new LabelProvider() {
			@Override
			public String getText(Object element) {
				element = ((TreeElement)element).object;
				/*
				 * if (element instanceof Resource) { Resource res = (Resource)
				 * element; return res.getName(); }
				 */
				if(element instanceof IType) {
					IType type = (IType)element;
					return type.getUserName();
				}
				if(element instanceof IMember) {
					IMember member = (IMember)element;
					return member.getName()/*
											 * + " - " +
											 * member.getVariableType().getType(
											 * ).getUserName()
											 */;
				}
				return super.getText(element);
			}
		};
		final ITreeContentProvider treeContentProvider = new ITreeContentProvider() {

			@Override
			public Object[] getChildren(Object parentElement) {
				return ((TreeElement)parentElement).children;
			}

			@Override
			public Object getParent(Object element) {
				TreeElement parent = ((TreeElement)element).parent;
				if(parent == null)
					return Workspace.getInstance();
				else
					return parent;
			}

			@Override
			public boolean hasChildren(Object element) {
				return getChildren(element).length > 0;
			}

			@Override
			public Object[] getElements(Object inputElement) {
				if(inputElements == null) {
					Workspace w = (Workspace)inputElement;
					List<TreeElement> entryPoints = new ArrayList<TreeElement>();
					for(Project p : w.getProjects())
						for(IType t : p.getTypes())
							if(t.getAttribute(IAttribute.Entry) != null)
								entryPoints.add(new TreeElement(null, t));
					inputElements = entryPoints.toArray(new TreeElement[entryPoints.size()]);
					for(Object o : map.keySet())
						if(o instanceof IMember || o instanceof MemberNestedType)
							selectedObjects.add(o);
				}
				return inputElements;
			}

			@Override
			public void dispose() {
			}

			@Override
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}

		};

		CheckedTreeSelectionDialog treeDialog = new CheckedTreeSelectionDialog(shell, labelProvider, treeContentProvider) {
			@Override
			protected CheckboxTreeViewer createTreeViewer(Composite parent) {
				treeViewer = super.createTreeViewer(parent);
				treeViewer.addCheckStateListener(new ICheckStateListener() {

					@Override
					public void checkStateChanged(CheckStateChangedEvent event) {
						TreeElement element = (TreeElement)event.getElement();
						/*
						 * for (TreeElement other : element.getOther()){ if
						 * (other == element) continue;
						 * treeViewer.setChecked(other, event.getChecked()); }
						 */
						if(event.getChecked())
							element.select();
						else
							element.deselect();
						refreshTree();
					}

				});
				treeViewer.addTreeListener(new ITreeViewerListener() {

					@Override
					public void treeCollapsed(TreeExpansionEvent event) {
					}

					@Override
					public void treeExpanded(TreeExpansionEvent event) {
						refreshTree();
					}

				});
				return treeViewer;
			}

			@Override
			protected Composite createSelectionButtons(Composite composite) {
				Composite buttonComposite = new Composite(composite, SWT.RIGHT);
				GridLayout layout = new GridLayout();
				layout.numColumns = 2;
				buttonComposite.setLayout(layout);
				buttonComposite.setFont(composite.getFont());
				GridData data = new GridData(GridData.HORIZONTAL_ALIGN_END | GridData.GRAB_HORIZONTAL);
				data.grabExcessHorizontalSpace = true;
				composite.setData(data);
				Button selectButton = createButton(buttonComposite, IDialogConstants.SELECT_ALL_ID, WorkbenchMessages.CheckedTreeSelectionDialog_select_all, false);
				SelectionListener listener = new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						/*
						 * for (int i = 0; i < inputElements.length; i++) {
						 * treeViewer.setSubtreeChecked(inputElements[i], true);
						 * }
						 */
						selectedObjects.clear();
						selectedObjects.addAll(map.keySet());
						refreshTree();
						updateOKStatus();
					}
				};
				selectButton.addSelectionListener(listener);
				Button deselectButton = createButton(buttonComposite, IDialogConstants.DESELECT_ALL_ID, WorkbenchMessages.CheckedTreeSelectionDialog_deselect_all, false);
				listener = new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						// treeViewer.setCheckedElements(new Object[0]);
						selectedObjects.clear();
						refreshTree();
						updateOKStatus();
					}
				};
				deselectButton.addSelectionListener(listener);
				return buttonComposite;
			}

		};
		treeDialog.setTitle("���������������� ��������");
		treeDialog.setMessage("�������� ����������� �������");
		treeDialog.setContainerMode(false);
		treeDialog.setInput(Workspace.getInstance());
		if(treeDialog.open() != CheckedTreeSelectionDialog.OK)
			return;
		DirectoryDialog newFolderDialog = new DirectoryDialog(shell);
		newFolderDialog.setText("���������������� ��������");
		newFolderDialog.setMessage("�������� ���� ��� ����� ������������");
		newFolderDialog.setFilterPath(Workspace.getInstance().getResource().getLocation().toOSString());
		final String path = newFolderDialog.open();
		if(path == null)
			return;
		toDelete = new ArrayList<Object>();
		toDelete.addAll(map.keySet());
		toDelete.removeAll(selectedObjects);

		try {
			PlatformUI.getWorkbench().getProgressService().busyCursorWhile(new IRunnableWithProgress() {
				@Override
				public void run(IProgressMonitor monitor) {
					for(Project project : Workspace.getInstance().getProjects()) {
						int count = countFiles(project.getResource().getLocation().toFile());
						monitor.beginTask("Copying project " + project.getName(), count);
						copyDirectory(project.getResource().getLocation().toFile(), new File(path + "/" + project.getName()), monitor);
						monitor.done();
					}
				}

			});
		} catch(Exception e) {
			Plugin.log(e);
		}

		MessageDialog message = new MessageDialog(shell, "��������������� ��������", null, "���������� ��������:\n1. ������� � ����� workspace\n2. ������� ������ �������� � workspace �� �������� " + path + "\n3. ��������� ��������� �������� ��������������� ����",
				MessageDialog.INFORMATION, new String[] { "OK" }, 0);
		message.open();
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
	}

	private void refreshTree() {
		List<TreeElement> checked = new ArrayList<TreeElement>();
		List<TreeElement> grayed = new ArrayList<TreeElement>();
		for(List<TreeElement> list : map.values())
			for(TreeElement element : list) {
				if(element.isChecked())
					checked.add(element);
				if(element.isGrayed())
					grayed.add(element);
			}
		treeViewer.setCheckedElements(checked.toArray());
		treeViewer.setGrayedElements(grayed.toArray());
	}

}
