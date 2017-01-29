package org.zenframework.z8.pde.debug.model.value;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.debug.core.DebugException;

import org.zenframework.z8.pde.debug.model.JDXDebugTarget;
import org.zenframework.z8.pde.debug.model.JDXThread;
import org.zenframework.z8.pde.debug.model.variable.JDXVariable;

import com.sun.jdi.Method;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.StringReference;
import com.sun.jdi.Value;

public class JDXPrimitiveValue extends JDXObjectValue {
	public JDXPrimitiveValue(JDXDebugTarget target, JDXThread thread, JDXVariable variable, ObjectReference value) {
		super(target, thread, variable, value);
	}

	@Override
	public String getValueString() throws DebugException {
		ObjectReference object = (ObjectReference)getUnderlyingValue();
		ReferenceType type = object.referenceType();

		List<Method> method = type.methodsByName("toString", "()Ljava/lang/String;");
		assert (method.size() == 1);

		Value stringValue = getJDXThread().invokeMethod(object, method.get(0), new ArrayList<Value>());

		if(stringValue != null) {
			return ((StringReference)stringValue).value();
		}

		return null;
	}

	@Override
	public boolean hasVariables() {
		return false;
	}
}
