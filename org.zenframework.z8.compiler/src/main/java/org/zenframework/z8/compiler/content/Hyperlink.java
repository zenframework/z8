package org.zenframework.z8.compiler.content;

import org.zenframework.z8.compiler.core.IPosition;
import org.zenframework.z8.compiler.workspace.CompilationUnit;

public class Hyperlink {
	private CompilationUnit compilationUnit;
	private IPosition position;

	public Hyperlink(CompilationUnit compilationUnit, IPosition position) {
		this.compilationUnit = compilationUnit;
		this.position = position;
	}

	public CompilationUnit getCompilationUnit() {
		return compilationUnit;
	}

	public IPosition getPosition() {
		return position;
	}

	@Override
	public boolean equals(Object other) {
		Hyperlink otherHyperlink = (Hyperlink)other;
		return (compilationUnit.equals(otherHyperlink.compilationUnit) && position.equals(otherHyperlink.position));
	}
}
