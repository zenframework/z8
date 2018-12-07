package org.zenframework.z8.compiler.parser.variable;

import java.util.ArrayList;
import java.util.List;

import org.zenframework.z8.compiler.core.CodeGenerator;
import org.zenframework.z8.compiler.workspace.CompilationUnit;

public class TempVariables {
	private CompilationUnit compilationUnit;
	private List<String> tempVariables;

	public TempVariables(CompilationUnit compilationUnit) {
		this.compilationUnit = compilationUnit;
	}

	public String createVariable() {
		if(tempVariables == null)
			tempVariables = new ArrayList<String>();

		String variableName = compilationUnit.createUniqueName();
		tempVariables.add(variableName);
		return variableName;
	}

	public void getCode(CodeGenerator codeGenerator) {
		if(tempVariables == null)
			return;

		String variables = "";

		for(String variable : tempVariables)
			variables += (variables.isEmpty() ? "" : ", ") + variable;

		codeGenerator.indent();
		codeGenerator.append("Object " + variables + ";");
		codeGenerator.breakLine();
	}
}
