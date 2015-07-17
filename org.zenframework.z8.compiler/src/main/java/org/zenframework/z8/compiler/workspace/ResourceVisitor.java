package org.zenframework.z8.compiler.workspace;

public class ResourceVisitor {
    public boolean visit(CompilationUnit compilationUnit) {
        return true;
    }

    public boolean visit(NlsUnit nlsUnit) {
        return true;
    }
}
