package org.zenframework.z8.pde.debug.model.type;

import java.text.MessageFormat;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IIndexedValue;

import org.zenframework.z8.pde.debug.JDXMessages;
import org.zenframework.z8.pde.debug.model.JDXDebugTarget;
import org.zenframework.z8.pde.debug.model.JDXThread;
import org.zenframework.z8.pde.debug.model.value.JDXValue;
import org.zenframework.z8.pde.debug.model.variable.JDXVariable;

import com.sun.jdi.ArrayReference;
import com.sun.jdi.ArrayType;
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.Type;

public class JDXArrayType extends JDXReferenceType {
	public JDXArrayType(JDXDebugTarget target, JDXThread thread, JDXVariable variable, ArrayType type) {
		super(target, thread, variable, type);
	}

	public IIndexedValue newInstance(int size) throws DebugException {
		try {
			ArrayReference ar = ((ArrayType)getUnderlyingType()).newInstance(size);
			return (IIndexedValue)JDXValue.createValue(getDebugTarget(), getJDXThread(), getJDXVariable(), ar);
		} catch(RuntimeException e) {
			getDebugTarget().targetRequestFailed(MessageFormat.format(JDXMessages.JDXArrayType_exception_while_creating_new_instance_of_array, new Object[] { e.toString() }), e);
		}
		return null;
	}

	public JDXType getComponentType() throws DebugException {
		try {
			Type type = ((ArrayType)getUnderlyingType()).componentType();
			return JDXType.createType(getDebugTarget(), getJDXThread(), getJDXVariable(), type);
		} catch(ClassNotLoadedException e) {
			getDebugTarget().targetRequestFailed(MessageFormat.format(JDXMessages.JDXArrayType_exception_while_retrieving_component_type_of_array, new Object[] { e.toString() }), e);
		} catch(RuntimeException e) {
			getDebugTarget().targetRequestFailed(MessageFormat.format(JDXMessages.JDXArrayType_exception_while_retrieving_component_type_of_array, new Object[] { e.toString() }), e);
		}
		return null;
	}
}
