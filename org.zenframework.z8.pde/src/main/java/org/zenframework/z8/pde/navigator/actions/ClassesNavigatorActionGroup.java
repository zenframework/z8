package org.zenframework.z8.pde.navigator.actions;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourceAttributes;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.actions.ActionGroup;

import org.zenframework.z8.compiler.core.ISource;
import org.zenframework.z8.compiler.core.IType;
import org.zenframework.z8.compiler.parser.type.ImportBlock;
import org.zenframework.z8.compiler.parser.type.MemberNestedType;
import org.zenframework.z8.compiler.workspace.CompilationUnit;
import org.zenframework.z8.compiler.workspace.Folder;
import org.zenframework.z8.compiler.workspace.Project;
import org.zenframework.z8.compiler.workspace.Resource;
import org.zenframework.z8.pde.Plugin;
import org.zenframework.z8.pde.navigator.ClassesNavigator;

public class ClassesNavigatorActionGroup extends ActionGroup {

	private IAction swSuper, swBase, swLinked;

	private ClassesNavigator navigator;

	public ClassesNavigatorActionGroup(ClassesNavigator provider) {
		this.navigator = provider;
	}

	@Override
	public void fillActionBars(IActionBars actionBars) {
		swSuper = new SwitchFilterSuper(navigator.getTreeViewer());
		swBase = new SwitchFilterBase(navigator.getTreeViewer());
		swLinked = new SwitchLinkedWithEditor(navigator);
		swLinked.setChecked(navigator.isLinkedWithEditor());

		actionBars.getToolBarManager().add(swSuper);
		actionBars.getToolBarManager().add(swBase);
		actionBars.getToolBarManager().add(swLinked);

		actionBars.getMenuManager().add(swSuper);
		actionBars.getMenuManager().add(swBase);
		actionBars.getMenuManager().add(swLinked);
	}

	@Override
	public void fillContextMenu(IMenuManager menu) {
		IStructuredSelection selection = (IStructuredSelection)getContext().getSelection();

		Object element = selection.getFirstElement();

		if(element != null) {
			IAction openAction = createOpenAction(element);

			if(openAction != null) {
				menu.add(openAction);
			}

			if(element instanceof IType) {
				menu.add(new OpenHierarchyAction((IType)element));
			}

			if(element instanceof IType) {
				IType type = (IType)element;
				element = type.getCompilationUnit();
			}

			if(element instanceof Resource) {
				final Resource r = (Resource)element;

				menu.add(new Action("����������� �������") {
					@Override
					public void run() {
						organizeImports(r);
					}
				});
			}
		}

		menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	public IAction createOpenAction(final Object element) {
		if(element instanceof CompilationUnit) {
			CompilationUnit cu = (CompilationUnit)element;
			return new OpenCompilationUnitAction(cu);
		}

		if(element instanceof IType) {
			IType type = (IType)element;
			if(!(type instanceof MemberNestedType))
				return new OpenCompilationUnitAction(type.getCompilationUnit());
		}

		if(element instanceof Resource) {
			return new Action() {
				@Override
				public void run() {
					navigator.getTreeViewer().expandToLevel((Resource)element, 1);
				}

				@Override
				public String getText() {
					return "��������";
				}
			};
		}

		if(element instanceof ISource) {
			return new OpenSourceAction((ISource)element);
		}

		return null;
	}

	private void organizeImports(Resource resource) {
		if(resource instanceof Folder) {
			for(Resource res : resource.getMembers()) {
				organizeImports(res);
			}
		}

		if(resource instanceof CompilationUnit) {
			organizeImports((CompilationUnit)resource);
		}
	}

	private void organizeImports(CompilationUnit compilationUnit) {
		IType type = compilationUnit.getType();

		if(type == null) {
			return;
		}

		ImportBlock importBlock = type.getImportBlock();

		Project project = compilationUnit.getProject();

		String[] unresolvedTypes = compilationUnit.getUnresolvedTypes();
		String imports = "";

		for(String typeName : unresolvedTypes) {
			CompilationUnit[] otherCompilationUnits = project.lookupCompilationUnits(typeName);

			if(otherCompilationUnits.length == 0)
				continue;

			String qualifiedName = null;
			qualifiedName = otherCompilationUnits[0].getQualifiedName();

			if(importBlock == null || importBlock.getImportedUnit(typeName) == null) {
				if(qualifiedName != null) {
					imports += "import " + qualifiedName + ";\r\n";
				}
			}
		}

		if(importBlock != null) {
			List<String> qualifiedNames = importBlock.getResolvedNames();

			for(String name : qualifiedNames) {
				imports += "import " + name + ";\r\n";
			}
		}

		try {
			IFile file = (IFile)compilationUnit.getResource();
			InputStreamReader reader = new InputStreamReader(file.getContents());

			String contents = "";

			while(reader.ready()) {
				contents += (char)reader.read();
			}

			reader.close();

			if(importBlock == null) {
				contents = imports + contents;
			} else {
				if(imports.length() > 2) {
					imports = imports.substring(0, imports.length() - 1);
				}

				int offset = importBlock.getPosition().getOffset();
				int length = importBlock.getPosition().getLength();
				contents = contents.substring(0, offset) + imports + contents.substring(offset + length, contents.length());
			}

			ResourceAttributes attributes = file.getResourceAttributes();
			attributes.setReadOnly(false);

			file.setResourceAttributes(attributes);
			file.setContents(new ByteArrayInputStream(contents.getBytes()), true, false, null);
		} catch(Exception e) {
			Plugin.log(e);
		}
	}
}
