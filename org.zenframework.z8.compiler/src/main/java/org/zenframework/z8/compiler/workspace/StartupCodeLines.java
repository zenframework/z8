package org.zenframework.z8.compiler.workspace;

import org.zenframework.z8.compiler.core.IAttribute;
import org.zenframework.z8.compiler.core.IType;

public class StartupCodeLines {
	public String addTable = null;
	public String addEntry = null;
	public String addJob = null;
	public String addRequest = null;
	public String addExecutable = null;
	public String addSecurityLog = null;

	public StartupCodeLines() {
	}

	public void clear() {
		addTable = null;
		addEntry = null;
		addJob = null;
		addRequest = null;
		addExecutable = null;
		addSecurityLog = null;
	}

	private String getJavaNew(IType type) {
		return "new " + type.getQualifiedJavaName() + ".CLASS(null)";
	}

	public void generate(IType type) {
		clear();

		if(type == null)
			return;

		IAttribute attribute = type.findAttribute(IAttribute.Name);

		if(attribute != null && attribute.getValueString().length() != 0) {
			if(type.getAttribute(IAttribute.Generatable) != null)
				addTable = "addTable(" + getJavaNew(type) + ");";
			else if(type.findAttribute(IAttribute.Executable) != null)
				addExecutable = "addExecutable(" + getJavaNew(type) + ");";
		}

		attribute = type.getAttribute(IAttribute.Job);

		if(attribute != null)
			addJob = "addJob(" + getJavaNew(type) + ");";

		attribute = type.getAttribute(IAttribute.Entry);

		if(attribute != null)
			addEntry = "addEntry(" + getJavaNew(type) + ");";

		attribute = type.findAttribute(IAttribute.Request);

		if(attribute != null) {
			String value = attribute.getValueString();
			if(value.isEmpty() || Boolean.parseBoolean(value))
				addRequest = "addRequest(" + getJavaNew(type) + ");";
		}

		attribute = type.getAttribute(IAttribute.Log);

		if(attribute != null)
			addSecurityLog = "addSecurityLog(" + getJavaNew(type) + ");";
	}
}
