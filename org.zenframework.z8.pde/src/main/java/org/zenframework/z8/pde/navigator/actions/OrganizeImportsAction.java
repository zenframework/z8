package org.zenframework.z8.pde.navigator.actions;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourceAttributes;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchSite;

import org.zenframework.z8.compiler.core.IType;
import org.zenframework.z8.compiler.parser.type.ImportBlock;
import org.zenframework.z8.compiler.workspace.CompilationUnit;
import org.zenframework.z8.compiler.workspace.Folder;
import org.zenframework.z8.compiler.workspace.Project;
import org.zenframework.z8.compiler.workspace.Resource;
import org.zenframework.z8.compiler.workspace.Workspace;
import org.zenframework.z8.pde.Plugin;

public class OrganizeImportsAction extends Action {

	private IWorkbenchSite site;

	public OrganizeImportsAction(IWorkbenchSite site) {
		super("Organize imports");
		this.site = site;
	}

	@Override
	public void run() {
		ISelection s = site.getSelectionProvider().getSelection();
		IStructuredSelection ss = (IStructuredSelection)s;
		if(ss.isEmpty())
			return;
		if(ss.size() > 1)
			return;
		if(ss.getFirstElement() instanceof IResource) {
			Resource r = Workspace.getInstance().getResource((IResource)ss.getFirstElement());
			organizeImports(r);
		}
	}

	@Override
	public boolean isEnabled() {
		ISelection s = site.getSelectionProvider().getSelection();
		IStructuredSelection ss = (IStructuredSelection)s;
		if(ss.isEmpty())
			return false;
		if(ss.size() > 1)
			return false;
		if(ss.getFirstElement() instanceof IResource) {
			return true;
		}

		return false;
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
		if(imports.length() > 0)
			imports = imports.substring(0, imports.length() - 2);

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
