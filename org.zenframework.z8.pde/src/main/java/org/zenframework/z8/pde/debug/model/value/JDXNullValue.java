package org.zenframework.z8.pde.debug.model.value;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.debug.core.model.IVariable;

import org.zenframework.z8.pde.debug.model.JDXDebugTarget;
import org.zenframework.z8.pde.debug.model.JDXThread;
import org.zenframework.z8.pde.debug.model.type.JDXType;
import org.zenframework.z8.pde.debug.model.variable.JDXVariable;

public class JDXNullValue extends JDXValue {
	public JDXNullValue(JDXDebugTarget target, JDXThread thread, JDXVariable variable) {
		super(target, thread, variable, null);
	}

	@Override
	protected List<IVariable> getVariablesList() {
		return new ArrayList<IVariable>();
	}

	@Override
	public String getReferenceTypeName() {
		return "null";
	}

	@Override
	public String getValueString() {
		return "null";
	}

	@Override
	public String getSignature() {
		return null;
	}

	public int getArrayLength() {
		return -1;
	}

	@Override
	public JDXType getJDXType() {
		return null;
	}

	@Override
	public String toString() {
		return "null";
	}
}
