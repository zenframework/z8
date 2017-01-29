package org.zenframework.z8.pde.debug.model.value;

import org.zenframework.z8.pde.debug.model.JDXDebugTarget;
import org.zenframework.z8.pde.debug.model.JDXThread;
import org.zenframework.z8.pde.debug.model.variable.JDXVariable;

import com.sun.jdi.ObjectReference;

public class JDXStringValue extends JDXPrimitiveValue {
	public JDXStringValue(JDXDebugTarget target, JDXThread thread, JDXVariable variable, ObjectReference value) {
		super(target, thread, variable, value);
	}
}
