package org.zenframework.z8.compiler.parser.type;

import java.util.ArrayList;
import java.util.List;

import org.zenframework.z8.compiler.core.CodeGenerator;
import org.zenframework.z8.compiler.core.IMethod;
import org.zenframework.z8.compiler.core.IPosition;
import org.zenframework.z8.compiler.core.IToken;
import org.zenframework.z8.compiler.core.IType;
import org.zenframework.z8.compiler.core.IVariable;
import org.zenframework.z8.compiler.core.IVariableType;
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
		if(elements.length == 1)
			return elements[0].getSourceRange();

		return elements[0].getSourceRange().union(elements[elements.length - 1].getSourceRange());
	}

	@Override
	public IToken getFirstToken() {
		return elements[0].getFirstToken();
	}

	public CompilationUnit getImportedUnit(String simpleName) {
		if(importedElements != null) {
			for(ImportElement element : importedElements) {
				if(element.compare(simpleName))
					return element.getImportedUnit();
			}
		}

		return null;
	}

	protected boolean addImport(ImportElement element) {
		if(importedElements != null) {
			for(ImportElement e : importedElements) {
				if(element.compare(e))
					return false;
			}
		} else
			importedElements = new ArrayList<ImportElement>();

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

			if(!addImport(element))
				setError(element.getPosition(), "The import " + element.getQualifiedName() + " collides with another import statement");
		}

		return true;
	}

	@Override
	public boolean checkSemantics(CompilationUnit compilationUnit, IType declaringType, IMethod declaringMethod, IVariable leftHandValue, IVariableType context) {
		for(ImportElement element : elements)
			element.checkSemantics(compilationUnit, declaringType, declaringMethod, leftHandValue, context);
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
				if(element.wasUsedInResolving())
					result.add(element.getImportedUnit().getQualifiedName());
			}
		}

		return result;
	}

	public boolean checkImportUsage(CompilationUnit compilationUnit) {
		if(!compilationUnit.containsError() && importedElements != null) {
			for(ImportElement element : importedElements) {
				if(!element.wasUsedInResolving())
					compilationUnit.warning(element.getPosition(), "The import " + element.getQualifiedName() + " is never used");
			}
		}

		return true;
	}

	@Override
	public void getCode(CodeGenerator codeGenerator) {
		for(ImportElement element : elements)
			element.getCode(codeGenerator);
	}

	public ImportElement[] getImportElements() {
		return elements;
	}
}
