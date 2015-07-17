package org.zenframework.z8.compiler.content;

import org.zenframework.z8.compiler.core.IType;

public class TypeHyperlink extends Hyperlink {

    private IType type;

    public TypeHyperlink(IType type) {
        super(type.getCompilationUnit(), type.getPosition());
        this.type = type;
    }

    public IType getType() {
        return type;
    }
}
