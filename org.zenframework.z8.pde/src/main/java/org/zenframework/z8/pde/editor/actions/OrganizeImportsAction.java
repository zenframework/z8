package org.zenframework.z8.pde.editor.actions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.dialogs.ListDialog;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.TextEditorAction;

import org.zenframework.z8.compiler.core.IType;
import org.zenframework.z8.compiler.parser.type.ImportBlock;
import org.zenframework.z8.compiler.workspace.CompilationUnit;
import org.zenframework.z8.compiler.workspace.Project;
import org.zenframework.z8.compiler.workspace.Workspace;
import org.zenframework.z8.pde.Z8EditorMessages;
import org.zenframework.z8.pde.Plugin;

public class OrganizeImportsAction extends TextEditorAction {
	protected static Image RESOURCE_IMAGE;

	static {
		try {
			RESOURCE_IMAGE = new Image((Device)null, FileLocator.openStream(Plugin.getDefault().getBundle(), new Path("icons/obj16/z8.gif"), true));
		} catch(Exception e) {
			Plugin.log(e);
		}
	}

	public OrganizeImportsAction(String prefix, ITextEditor editor) {
		super(Z8EditorMessages.getResourceBundle(), prefix, editor);
	}

	@Override
	public void run() {
		IResource resource = ((FileEditorInput)getTextEditor().getEditorInput()).getFile();
		CompilationUnit compilationUnit = Workspace.getInstance().getCompilationUnit(resource);
		IType type = compilationUnit.getReconciledType();
		if(type == null)
			return;
		ImportBlock importBlock = type.getImportBlock();

		Project project = compilationUnit.getProject();

		String[] unresolvedTypes = compilationUnit.getUnresolvedTypes();

		List<String> imports = new ArrayList<String>();

		for(String typeName : unresolvedTypes) {
			CompilationUnit[] otherCompilationUnits = project.lookupCompilationUnits(typeName);

			if(otherCompilationUnits.length == 0)
				continue;

			String qualifiedName = null;// otherCompilationUnits[0].getQualifiedName();

			if(otherCompilationUnits.length == 1) {
				qualifiedName = otherCompilationUnits[0].getQualifiedName();
			} else {
				ListDialog dialog = new ListDialog(getTextEditor().getSite().getShell());
				dialog.setContentProvider(new IStructuredContentProvider() {
					@Override
					public Object[] getElements(Object inputElement) {
						return (Object[])inputElement;
					}

					@Override
					public void dispose() {
					}

					@Override
					public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
					}
				});
				dialog.setLabelProvider(new LabelProvider() {
					@Override
					public String getText(Object element) {
						return ((CompilationUnit)element).getQualifiedName();
					}

					@Override
					public Image getImage(Object element) {
						return RESOURCE_IMAGE;
					}
				});
				dialog.setInput(otherCompilationUnits);
				dialog.setTitle("Organize imports");
				dialog.setMessage("Выберите подходящий import");
				dialog.setInitialSelections(new Object[] { otherCompilationUnits[0] });
				int open = dialog.open();
				if(Dialog.CANCEL == open)
					continue;
				else {
					Object[] result = dialog.getResult();
					if(result.length != 1)
						continue;
					Object first = result[0];
					if(first instanceof IType) {
						IType typ = (IType)first;
						qualifiedName = typ.getCompilationUnit().getQualifiedName();
					}
					if(first instanceof CompilationUnit) {
						CompilationUnit unit = (CompilationUnit)first;
						qualifiedName = unit.getQualifiedName();
					}
				}
			}
			if(importBlock == null || importBlock.getImportedUnit(typeName) == null) {
				if(qualifiedName != null)
					imports.add(qualifiedName);
			}
		}
		if(importBlock != null) {
			List<String> qualifiedNames = importBlock.getResolvedNames();
			for(String name : qualifiedNames)
				imports.add(name);
		}

		Collections.sort(imports);

		String organizedImports = "";

		for(String qualifiedName : imports)
			organizedImports += (organizedImports.length() > 0 ? "\r\n" : "") + "import " + qualifiedName + ";";

		if(validateEditorInputState()) {
			try {
				if(importBlock == null)
					getDocument().replace(0, 0, organizedImports + (organizedImports.length() > 0 ? "\r\n" : ""));
				else
					getDocument().replace(importBlock.getPosition().getOffset(), importBlock.getPosition().getLength(), organizedImports);
			} catch(BadLocationException e) {
			}
		}
	}

	protected IDocument getDocument() {
		return getTextEditor().getDocumentProvider().getDocument(getTextEditor().getEditorInput());
	}
}
