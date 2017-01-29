package org.zenframework.z8.pde.debug.model.value;

import org.zenframework.z8.pde.debug.model.JDXDebugTarget;
import org.zenframework.z8.pde.debug.model.JDXThread;
import org.zenframework.z8.pde.debug.model.type.JDXType;
import org.zenframework.z8.pde.debug.model.variable.JDXVariable;

import com.sun.jdi.ClassObjectReference;

public class JDXClassObjectValue extends JDXObjectValue {
	public JDXClassObjectValue(JDXDebugTarget target, JDXThread thread, JDXVariable variable, ClassObjectReference object) {
		super(target, thread, variable, object);
	}

	public JDXType getInstanceType() {
		return JDXType.createType(getJDXDebugTarget(), getJDXThread(), getJDXVariable(), getUnderlyingClassObject().reflectedType());
	}

	protected ClassObjectReference getUnderlyingClassObject() {
		return (ClassObjectReference)getUnderlyingValue();
	}
}
