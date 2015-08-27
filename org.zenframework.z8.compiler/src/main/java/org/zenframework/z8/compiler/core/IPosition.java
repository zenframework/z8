package org.zenframework.z8.compiler.core;

public interface IPosition {
    int getOffset();

    int getColumn();

    int getLine();

    int getLength();

    void setOffset(int offset);

    void setColumn(int column);

    void setLine(int line);

    void setLength(int length);

    int distance(IPosition position);

    IPosition union(IPosition position);

    boolean contains(IPosition position);

    void saveState();

    void restoreState();
}
