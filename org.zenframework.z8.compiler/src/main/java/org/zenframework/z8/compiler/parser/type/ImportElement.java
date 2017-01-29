package org.zenframework.z8.compiler.parser.type;

import org.zenframework.z8.compiler.core.CodeGenerator;
import org.zenframework.z8.compiler.core.IMethod;
import org.zenframework.z8.compiler.core.IPosition;
import org.zenframework.z8.compiler.core.IToken;
import org.zenframework.z8.compiler.core.IType;
import org.zenframework.z8.compiler.core.IVariable;
import org.zenframework.z8.compiler.core.IVariableType;
import org.zenframework.z8.compiler.parser.LanguageElement;
import org.zenframework.z8.compiler.parser.expressions.QualifiedName;
import org.zenframework.z8.compiler.workspace.CompilationUnit;
import org.zenframework.z8.compiler.workspace.Folder;
import org.zenframework.z8.compiler.workspace.Project;

public class ImportElement extends LanguageElement {
	private IToken importToken;
	private IToken semicolonToken;

	private QualifiedName qualifiedName;
	private CompilationUnit importedUnit;

	private int lastResolvedToken;
	private boolean wasUsedInResolving;

	public ImportElement(IToken importToken, QualifiedName qualifiedName, IToken semicolonToken) {
		this.importToken = importToken;
		this.qualifiedName = qualifiedName;
		this.semicolonToken = semicolonToken;
	}

	@Override
	public IPosition getPosition() {
		return qualifiedName.getPosition();
	}

	@Override
	public IPosition getSourceRange() {
		return importToken.getPosition().union(semicolonToken.getPosition());
	}

	@Override
	public IToken getFirstToken() {
		return importToken;
	}

	public boolean wasUsedInResolving() {
		return wasUsedInResolving;
	}

	public CompilationUnit extractCompilationUnit() {
		return importedUnit;
	}

	public CompilationUnit getImportedUnit() {
		wasUsedInResolving = true;
		return importedUnit;
	}

	public String getQualifiedName() {
		return qualifiedName.toString();
	}

	public boolean compare(String simpleName) {
		return importedUnit.getSimpleName().equals(simpleName);
	}

	public boolean compare(ImportElement element) {
		return compare(element.importedUnit.getSimpleName());
	}

	@Override
	public boolean resolveTypes(CompilationUnit compilationUnit, IType declaringType) {
		if(!super.resolveTypes(compilationUnit, declaringType))
			return false;

		Project thisProject = compilationUnit.getProject();

		importedUnit = resolveToCompilationUnit(thisProject);

		if(importedUnit != null) {
			compilationUnit.addContributor(importedUnit);
			return true;
		}

		Project[] projects = thisProject.getReferencedProjects();

		for(Project project : projects) {
			importedUnit = resolveToCompilationUnit(project);

			if(importedUnit != null) {
				if(importedUnit.resolveType() != null) {
					compilationUnit.addContributor(importedUnit);
					return true;
				}
				return false;
			}
		}

		setError(qualifiedName.getFirstToken().getPosition().union(qualifiedName.getTokens()[lastResolvedToken].getPosition()), "The import " + qualifiedName.toString(0, lastResolvedToken + 1) + " cannot be resolved");
		return false;
	}

	public boolean checkSemantics(CompilationUnit compilationUnit, IType declaringType, IMethod declaringMethod, IVariable leftHandValue, IVariableType context) {
		if(importedUnit == null)
			return true;

		IType type = importedUnit.getType();
		if(type != null)
			compilationUnit.addHyperlink(qualifiedName.getLastToken().getPosition(), type);

		return true;
	}

	private CompilationUnit resolveToCompilationUnit(Project project) {
		Folder folder = project;

		IToken[] tokens = qualifiedName.getTokens();

		for(int i = 0; i < tokens.length; i++) {
			lastResolvedToken = Math.max(lastResolvedToken, i);

			IToken token = tokens[i];

			String name = token.getRawText();

			if(i < tokens.length - 1) {
				folder = folder.getFolder(name);

				if(folder == null)
					break;
			} else
				return folder.getCompilationUnit(name + ".bl");
		}

		return null;
	}

	@Override
	public void getCode(CodeGenerator codeGenerator) {
		String qualifiedName = importedUnit.getType().getQualifiedJavaName();
		codeGenerator.append("import " + qualifiedName + ';');
		codeGenerator.breakLine();
	}
}
