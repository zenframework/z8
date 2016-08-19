package org.zenframework.z8.compiler.parser.grammar.lexer;

import java.io.UnsupportedEncodingException;

import org.zenframework.z8.compiler.core.IPosition;
import org.zenframework.z8.compiler.file.File;
import org.zenframework.z8.compiler.file.FileException;
import org.zenframework.z8.compiler.workspace.CompilationUnit;

public class FileBuffer {
	private char[] content;

	public FileBuffer(char[] content) {
		this.content = content;
	}

	public FileBuffer(CompilationUnit compilationUnit, char[] content) throws FileException, UnsupportedEncodingException {
		this.content = content == null ? new File(compilationUnit.getAbsolutePath()).read() : content;
	}

	public char[] getContent() {
		return content;
	}

	@Override
	public int hashCode() {
		return new String(content).hashCode();
	}

	public boolean isEOF(IPosition position) {
		return position.getOffset() >= content.length;
	}

	public char charAt(IPosition position) {
		return content[position.getOffset()];
	}

	public char nextCharAt(IPosition position) {
		return position.getOffset() < content.length - 1 ? content[position.getOffset() + 1] : 0;
	}

	public char nextCharAt(IPosition position, int offset) {
		return position.getOffset() < content.length - offset ? content[position.getOffset() + offset] : 0;
	}

	public String getString(IPosition position) {
		return new String(content, position.getOffset(), position.getLength());
	}

	public void advance(IPosition position) {
		if(!isEOF(position) && ABC.isTabulator(charAt(position))) {
			position.setColumn(position.getColumn() + ABC.TabulatorLength);
		} else {
			position.setColumn(position.getColumn() + 1);
		}

		position.setOffset(position.getOffset() + 1);
	}

	public void advance(IPosition position, int count) {
		for(int i = 0; i < count; i++) {
			advance(position);
		}
	}

	public void breakLine(IPosition position) {
		position.setColumn(0);
		position.setLine(position.getLine() + 1);
	}
}
