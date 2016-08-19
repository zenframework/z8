package org.zenframework.z8.compiler.parser.type;

import org.eclipse.core.runtime.IPath;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;

import org.zenframework.z8.compiler.core.CodeGenerator;
import org.zenframework.z8.compiler.core.IPosition;
import org.zenframework.z8.compiler.core.IToken;
import org.zenframework.z8.compiler.core.IType;
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

		if(importedUnit == null) {
			Project[] projects = thisProject.getReferencedProjects();

			for(Project project : projects) {
				importedUnit = resolveToCompilationUnit(project);

				if(importedUnit != null) {
					if(importedUnit.resolveType() != null) {
						compilationUnit.addHyperlink(qualifiedName.getLastToken().getPosition(), importedUnit.getType());
						compilationUnit.addContributor(importedUnit);
						return true;
					}
					return false;
				}
			}
		}

		if(importedUnit == null) {
			setError(qualifiedName.getFirstToken().getPosition().union(qualifiedName.getTokens()[lastResolvedToken].getPosition()), "The import " + qualifiedName.toString(0, lastResolvedToken + 1) + " cannot be resolved");
			return false;
		}

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
			} else {
				return folder.getCompilationUnit(name + ".bl");
			}
		}

		return null;
	}

	@Override
	public void getCode(CodeGenerator codeGenerator) {
		String qualifiedName = importedUnit.getType().getQualifiedJavaName();
		codeGenerator.append("import " + qualifiedName + ';');
		codeGenerator.breakLine();
	}

	@Override
	public void replaceTypeName(TextEdit parent, IType type, String newTypeName) {
		if(importedUnit != null && importedUnit.getType() == type) {
			IPosition position = qualifiedName.getLastToken().getPosition();
			parent.addChild(new ReplaceEdit(position.getOffset(), position.getLength(), newTypeName));
		}
	}

	@Override
	public void replaceImport(TextEdit parent, IPath oldImport, IPath newImport) {
		String qName = qualifiedName.toString();

		if(qName.equals(oldImport.toString().replace(IPath.SEPARATOR, '.'))) {
			IPosition position = qualifiedName.getPosition();
			parent.addChild(new ReplaceEdit(position.getOffset(), position.getLength(), newImport.toString().replace(IPath.SEPARATOR, '.')));
		}
	}
}
