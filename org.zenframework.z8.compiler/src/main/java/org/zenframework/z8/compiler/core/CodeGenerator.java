package org.zenframework.z8.compiler.core;

import org.zenframework.z8.compiler.workspace.CompilationUnit;

public class CodeGenerator {

    private StringBuffer buffer = new StringBuffer(1024);

    private int line;
    private String indent = "";
    private CompilationUnit compilationUnit;

    public CodeGenerator(CompilationUnit compilationUnit) {
        this.compilationUnit = compilationUnit;
    }

    public CompilationUnit getCompilationUnit() {
        return compilationUnit;
    }

    public void append(char c) {
        buffer.append(c);
    }

    public int length() {
        return buffer.length();
    }

    public void append(String text) {
        buffer.append(text);
    }

    public void append(CodeGenerator generator) {
        buffer.append(generator.buffer);
        line += generator.line;
    }

    public void breakLine() {
        buffer.append("\n");
        line++;
    }

    public int getCurrentLine() {
        return line;
    }

    @Override
    public String toString() {
        return buffer.toString();
    }

    public void indent() {
        buffer.append(indent);
    }

    public void incrementIndent() {
        indent += "\t";
    }

    public void decrementIndent() {
        indent = indent.substring(0, indent.length() - 1);
    }
}
