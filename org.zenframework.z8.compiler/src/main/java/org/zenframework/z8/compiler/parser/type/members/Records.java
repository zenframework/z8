package org.zenframework.z8.compiler.parser.type.members;

import org.zenframework.z8.compiler.core.CodeGenerator;
import org.zenframework.z8.compiler.core.IMethod;
import org.zenframework.z8.compiler.core.IPosition;
import org.zenframework.z8.compiler.core.IToken;
import org.zenframework.z8.compiler.core.IType;
import org.zenframework.z8.compiler.core.IVariable;
import org.zenframework.z8.compiler.core.IVariableType;
import org.zenframework.z8.compiler.parser.LanguageElement;
import org.zenframework.z8.compiler.workspace.CompilationUnit;

public class Records extends LanguageElement {
	private IToken recordsToken;
	@SuppressWarnings("unused")
	private IToken leftBrace;
	private IToken rightBrace;

	private Record[] elements;

	public Records(IToken recordsToken) {
		this.recordsToken = recordsToken;
	}

	@Override
	public IPosition getSourceRange() {
		if(rightBrace != null)
			return recordsToken.getPosition().union(rightBrace.getPosition());
		else if(elements != null)
			return recordsToken.getPosition().union(elements[elements.length - 1].getSourceRange());
		return recordsToken.getPosition();
	}

	@Override
	public IToken getFirstToken() {
		return recordsToken;
	}

	@Override
	public IPosition getPosition() {
		return recordsToken.getPosition();
	}

	public void setElements(IToken leftBrace, Record[] elements, IToken rightBrace) {
		this.elements = elements;
		this.leftBrace = leftBrace;
		this.rightBrace = rightBrace;
	}

	@Override
	public boolean resolveTypes(CompilationUnit compilationUnit, IType declaringType) {
		if(!super.resolveTypes(compilationUnit, declaringType))
			return false;

		if(elements != null) {
			for(Record element : elements)
				element.resolveTypes(compilationUnit, declaringType);
		}

		return true;
	}

	@Override
	public boolean resolveStructure(CompilationUnit compilationUnit, IType declaringType) {
		if(!super.resolveStructure(compilationUnit, declaringType))
			return false;

		if(elements != null) {
			for(Record element : elements)
				element.resolveStructure(compilationUnit, declaringType);
		}

		return true;
	}

	@Override
	public boolean checkSemantics(CompilationUnit compilationUnit, IType declaringType, IMethod declaringMethod, IVariable leftHandValue, IVariableType context) {
		if(!super.checkSemantics(compilationUnit, declaringType, declaringMethod, null, null))
			return false;

		if(elements != null) {
			for(Record element : elements)
				element.checkSemantics(compilationUnit, declaringType, declaringMethod, null, null);
		}

		return true;
	}

	@Override
	public void getCode(CodeGenerator codeGenerator) {
		if(elements == null)
			return;

		for(Record element : elements) {
			codeGenerator.indent();
			element.getCode(codeGenerator);
		}

		codeGenerator.breakLine();

		codeGenerator.indent();

		codeGenerator.append("public void initStaticRecords()");
		codeGenerator.breakLine();
		codeGenerator.indent();

		codeGenerator.append("{");
		codeGenerator.breakLine();

		codeGenerator.incrementIndent();

		codeGenerator.indent();
		codeGenerator.append("super.initStaticRecords();");
		codeGenerator.breakLine();

		for(Record element : elements)
			element.getAddRecordCode(codeGenerator);

		codeGenerator.decrementIndent();

		codeGenerator.breakLine();
		codeGenerator.indent();

		codeGenerator.append("}");
	}
}
