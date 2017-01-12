package org.zenframework.z8.compiler.content;

import org.zenframework.z8.compiler.core.IPosition;
import org.zenframework.z8.compiler.workspace.CompilationUnit;

public class Hyperlink {
	private CompilationUnit compilationUnit;
	private IPosition position;
	private HyperlinkKind kind = HyperlinkKind.None;

	public Hyperlink(CompilationUnit compilationUnit, IPosition position) {
		this(compilationUnit, position, HyperlinkKind.None);
	}

	public Hyperlink(CompilationUnit compilationUnit, IPosition position, HyperlinkKind kind) {
		this.compilationUnit = compilationUnit;
		this.position = position;
		this.kind = kind;
	}

	public CompilationUnit getCompilationUnit() {
		return compilationUnit;
	}

	public IPosition getPosition() {
		return position;
	}

	public HyperlinkKind getKind() {
		return kind;
	}

	@Override
	public boolean equals(Object other) {
		Hyperlink otherHyperlink = (Hyperlink)other;
		return (compilationUnit.equals(otherHyperlink.compilationUnit) && position.equals(otherHyperlink.position));
	}
}
