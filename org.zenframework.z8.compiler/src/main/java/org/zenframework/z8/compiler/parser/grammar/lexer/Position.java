package org.zenframework.z8.compiler.parser.grammar.lexer;

import org.zenframework.z8.compiler.core.IPosition;

public class Position implements IPosition {
	private int offset;
	private int column;
	private int line;
	private int length;

	private IPosition savedState;

	public Position() {
	}

	public Position(IPosition position) {
		this.offset = position.getOffset();
		this.column = position.getColumn();
		this.line = position.getLine();
		this.length = position.getLength();
	}

	public Position(IPosition position, int length) {
		this(position);
		this.length = length;
	}

	public Position(IPosition begin, IPosition end) {
		this(begin);
		this.length = begin.distance(end) + end.getLength();
	}

	@Override
	public int getOffset() {
		return offset;
	}

	@Override
	public int getColumn() {
		return column;
	}

	@Override
	public int getLine() {
		return line;
	}

	@Override
	public int getLength() {
		return length;
	}

	@Override
	public void setOffset(int offset) {
		this.offset = offset;
	}

	@Override
	public void setColumn(int column) {
		this.column = column;
	}

	@Override
	public void setLine(int line) {
		this.line = line;
	}

	@Override
	public void setLength(int length) {
		this.length = length;
	}

	@Override
	public int distance(IPosition position) {
		return Math.abs(this.offset - position.getOffset());
	}

	@Override
	public boolean contains(IPosition position) {
		return offset <= position.getOffset() && offset + length <= position.getOffset() + position.getLength();
	}

	@Override
	public IPosition union(IPosition position) {
		Position pos = getOffset() < position.getOffset() ? new Position(this) : new Position(position);

		int right1 = getOffset() + getLength();
		int right2 = position.getOffset() + position.getLength();

		pos.length = Math.max(right1, right2) - pos.getOffset();

		return pos;
	}

	@Override
	public void saveState() {
		savedState = new Position(this);
	}

	@Override
	public void restoreState() {
		offset = savedState.getOffset();
		column = savedState.getColumn();
		line = savedState.getLine();
		length = savedState.getLength();
	}
}
