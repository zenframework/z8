package org.zenframework.z8.pde.debug.model.value;

import org.zenframework.z8.pde.debug.model.JDXDebugTarget;
import org.zenframework.z8.pde.debug.model.JDXThread;
import org.zenframework.z8.pde.debug.model.variable.JDXVariable;

import com.sun.jdi.PrimitiveValue;
import com.sun.jdi.Value;

public class JavaPrimitiveValue extends JDXValue {
	public JavaPrimitiveValue(JDXDebugTarget target, JDXThread thread, JDXVariable variable, Value value) {
		super(target, thread, variable, value);
	}

	protected PrimitiveValue getUnderlyingPrimitiveValue() {
		return (PrimitiveValue)getUnderlyingValue();
	}

	public boolean getBooleanValue() {
		return getUnderlyingPrimitiveValue().booleanValue();
	}

	public byte getByteValue() {
		return getUnderlyingPrimitiveValue().byteValue();
	}

	public char getCharValue() {
		return getUnderlyingPrimitiveValue().charValue();
	}

	public double getDoubleValue() {
		return getUnderlyingPrimitiveValue().doubleValue();
	}

	public float getFloatValue() {
		return getUnderlyingPrimitiveValue().floatValue();
	}

	public int getIntValue() {
		return getUnderlyingPrimitiveValue().intValue();
	}

	public long getLongValue() {
		return getUnderlyingPrimitiveValue().longValue();
	}

	public short getShortValue() {
		return getUnderlyingPrimitiveValue().shortValue();
	}

	@Override
	public boolean hasVariables() {
		return false;
	}
}
