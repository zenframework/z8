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

    public int length() {
        return buffer.length();
    }

    public int getCurrentLine() {
        return line;
    }

    @Override
    public String toString() {
        return buffer.toString();
    }

    public CodeGenerator append(char c) {
        buffer.append(c);
        return this;
    }

    public CodeGenerator append(String text) {
        buffer.append(text);
        return this;
    }

    public CodeGenerator append(CodeGenerator generator) {
        buffer.append(generator.buffer);
        line += generator.line;
        return this;
    }

    public CodeGenerator breakLine() {
        buffer.append("\n");
        line++;
        return this;
    }

    public CodeGenerator indent() {
        buffer.append(indent);
        return this;
    }

    public CodeGenerator incrementIndent() {
        indent += "\t";
        return this;
    }

    public CodeGenerator decrementIndent() {
        indent = indent.substring(0, indent.length() - 1);
        return this;
    }
}
