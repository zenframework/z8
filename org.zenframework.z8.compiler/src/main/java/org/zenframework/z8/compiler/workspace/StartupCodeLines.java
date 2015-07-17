package org.zenframework.z8.compiler.workspace;

import org.zenframework.z8.compiler.core.IAttribute;
import org.zenframework.z8.compiler.core.IType;

public class StartupCodeLines {
    public String addTable = null;
    public String addEntry = null;
    public String addJob = null;

    public StartupCodeLines() {}

    public void clear() {
        addTable = null;
        addEntry = null;
        addJob = null;
    }

    private String getJavaNew(IType type) {
        return "new " + type.getQualifiedJavaName() + ".CLASS(null)";
    }

    public void generate(IType type) {
        clear();

        if(type == null) {
            return;
        }

        IAttribute name = type.findAttribute(IAttribute.Name);

        if(name != null && name.getValueString().length() != 0 && type.getAttribute(IAttribute.Generatable) != null) {
            addTable = "addTable(" + getJavaNew(type) + ");\n";
        }

        IAttribute attribute = type.getAttribute(IAttribute.Job);

        if(attribute != null) {
            addJob = "addJob(" + getJavaNew(type) + ");";
        }

        attribute = type.getAttribute(IAttribute.Entry);

        if(attribute != null) {
            addEntry = "addEntry(" + getJavaNew(type) + ");";
        }
    }
}
