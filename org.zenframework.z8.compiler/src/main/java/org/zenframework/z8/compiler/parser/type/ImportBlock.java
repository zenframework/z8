package org.zenframework.z8.compiler.parser.type;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.text.edits.TextEdit;

import org.zenframework.z8.compiler.core.CodeGenerator;
import org.zenframework.z8.compiler.core.IPosition;
import org.zenframework.z8.compiler.core.IToken;
import org.zenframework.z8.compiler.core.IType;
import org.zenframework.z8.compiler.parser.LanguageElement;
import org.zenframework.z8.compiler.workspace.CompilationUnit;

public class ImportBlock extends LanguageElement {
	private ImportElement[] elements;

	private List<ImportElement> importedElements;

	public ImportBlock(ImportElement[] elements) {
		this.elements = elements;
	}

	@Override
	public IPosition getSourceRange() {
		if(elements.length == 1) {
			return elements[0].getSourceRange();
		}

		return elements[0].getSourceRange().union(elements[elements.length - 1].getSourceRange());
	}

	@Override
	public IToken getFirstToken() {
		return elements[0].getFirstToken();
	}

	public CompilationUnit getImportedUnit(String simpleName) {
		if(importedElements != null) {
			for(ImportElement element : importedElements) {
				if(element.compare(simpleName)) {
					return element.getImportedUnit();
				}
			}
		}

		return null;
	}

	protected boolean addImport(ImportElement element) {
		if(importedElements == null) {
			importedElements = new ArrayList<ImportElement>();
		} else {
			for(ImportElement e : importedElements) {
				if(element.compare(e))
					return false;
			}
		}

		importedElements.add(element);
		return true;
	}

	@Override
	public boolean resolveTypes(CompilationUnit compilationUnit, IType declaringType) {
		if(!super.resolveTypes(compilationUnit, declaringType))
			return false;

		for(ImportElement element : elements) {
			if(!element.resolveTypes(compilationUnit, null))
				continue;

			if(!addImport(element)) {
				setError(element.getPosition(), "The import " + element.getQualifiedName() + " collides with another import statement");
			}
		}

		return true;
	}

	public boolean hasUnusedNames() {
		for(ImportElement element : elements) {
			if(element.hasError() || !element.wasUsedInResolving())
				return true;
		}
		return false;
	}

	public List<String> getResolvedNames() {
		List<String> result = new ArrayList<String>();

		if(importedElements != null) {
			for(ImportElement element : importedElements) {
				if(element.wasUsedInResolving()) {
					result.add(element.getImportedUnit().getQualifiedName());
				}
			}
		}

		return result;
	}

	public boolean checkImportUsage(CompilationUnit compilationUnit) {
		if(!compilationUnit.containsError() && importedElements != null) {
			for(ImportElement element : importedElements) {
				if(!element.wasUsedInResolving()) {
					compilationUnit.warning(element.getPosition(), "The import " + element.getQualifiedName() + " is never used");
				}
			}
		}

		return true;
	}

	@Override
	public void getCode(CodeGenerator codeGenerator) {
		for(ImportElement element : elements) {
			element.getCode(codeGenerator);
		}
	}

	public ImportElement[] getImportElements() {
		return elements;
	}

	@Override
	public void replaceTypeName(TextEdit parent, IType type, String newTypeName) {
		if(importedElements != null) {
			for(ImportElement element : importedElements) {
				element.replaceTypeName(parent, type, newTypeName);
			}
		}
	}

	@Override
	public void replaceImport(TextEdit parent, IPath oldImport, IPath newImport) {
		for(ImportElement element : elements) {
			element.replaceImport(parent, oldImport, newImport);
		}
	}
}
