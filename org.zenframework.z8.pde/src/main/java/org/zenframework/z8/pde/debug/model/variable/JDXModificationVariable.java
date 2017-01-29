package org.zenframework.z8.pde.debug.model.variable;

import java.util.ArrayList;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IValue;

import org.zenframework.z8.pde.debug.JDXMessages;
import org.zenframework.z8.pde.debug.model.JDXDebugTarget;
import org.zenframework.z8.pde.debug.model.JDXThread;
import org.zenframework.z8.pde.debug.model.value.JDXValue;

import com.sun.jdi.Value;
import com.sun.jdi.VirtualMachine;

public abstract class JDXModificationVariable extends JDXVariable {
	private final static ArrayList<String> m_validSignatures = new ArrayList<String>(9);

	static {
		m_validSignatures.add("B");
		m_validSignatures.add("C");
		m_validSignatures.add("D");
		m_validSignatures.add("F");
		m_validSignatures.add("I");
		m_validSignatures.add("J");
		m_validSignatures.add("S");
		m_validSignatures.add("Z");
		m_validSignatures.add(m_jdxStringSignature);
	}

	public JDXModificationVariable(JDXDebugTarget target, JDXThread thread) {
		super(target, thread);
	}

	@Override
	public boolean supportsValueModification() {
		return true;
	}

	protected Value generateVMValue(String expression) throws DebugException {
		String signature = null;
		Value cValue = getCurrentValue();
		VirtualMachine vm = getVM();
		if(vm == null) {
			requestFailed(JDXMessages.JDXModificationVariable_Unable_to_generate_value___VM_disconnected__1, null);
		}
		if(cValue == null) {
			signature = m_jdxStringSignature;
		} else {
			signature = cValue.type().signature();
		}
		if(signature.length() > 1 && !signature.equals(m_jdxStringSignature)) {
			return null;
		}
		Value vmValue = null;
		try {
			switch(signature.charAt(0)) {
			case 'Z':
				String flse = Boolean.FALSE.toString();
				String tre = Boolean.TRUE.toString();
				if(expression.equals(tre) || expression.equals(flse)) {
					boolean booleanValue = Boolean.valueOf(expression).booleanValue();
					vmValue = vm.mirrorOf(booleanValue);
				}
				break;
			case 'B':
				byte byteValue = Byte.valueOf(expression).byteValue();
				vmValue = vm.mirrorOf(byteValue);
				break;
			case 'C':
				if(expression.length() == 1) {
					char charValue = expression.charAt(0);
					vmValue = vm.mirrorOf(charValue);
				} else if(expression.length() == 2) {
					char charValue;
					if(!(expression.charAt(0) == '\\')) {
						return null;
					}
					switch(expression.charAt(1)) {
					case 'b':
						charValue = '\b';
						break;
					case 'f':
						charValue = '\f';
						break;
					case 'n':
						charValue = '\n';
						break;
					case 'r':
						charValue = '\r';
						break;
					case 't':
						charValue = '\t';
						break;
					case '\'':
						charValue = '\'';
						break;
					case '\"':
						charValue = '\"';
						break;
					case '\\':
						charValue = '\\';
						break;
					default:
						return null;
					}
					vmValue = vm.mirrorOf(charValue);
				}
				break;
			case 'S':
				short shortValue = Short.valueOf(expression).shortValue();
				vmValue = vm.mirrorOf(shortValue);
				break;
			case 'I':
				int intValue = Integer.valueOf(expression).intValue();
				vmValue = vm.mirrorOf(intValue);
				break;
			case 'J':
				long longValue = Long.valueOf(expression).longValue();
				vmValue = vm.mirrorOf(longValue);
				break;
			case 'F':
				float floatValue = Float.valueOf(expression).floatValue();
				vmValue = vm.mirrorOf(floatValue);
				break;
			case 'D':
				double doubleValue = Double.valueOf(expression).doubleValue();
				vmValue = vm.mirrorOf(doubleValue);
				break;
			case 'L':
				if(expression.equals("null")) {
					vmValue = null;
				} else if(expression.equals("\"null\"")) {
					vmValue = vm.mirrorOf("null");
				} else {
					vmValue = vm.mirrorOf(expression);
				}
				break;
			}
		} catch(NumberFormatException nfe) {
			return null;
		}
		return vmValue;
	}

	@Override
	public boolean verifyValue(String expression) {
		try {
			IValue value = JDXValue.createValue(getJDXDebugTarget(), getJDXThread(), this, generateVMValue(expression));
			return verifyValue(value);
		} catch(DebugException e) {
			return false;
		}
	}

	@Override
	public boolean verifyValue(IValue value) {
		return value.getDebugTarget().equals(getDebugTarget());
	}

	@Override
	public final void setValue(String expression) throws DebugException {
		Value value = generateVMValue(expression);
		setJDXValue(value);
	}

	protected abstract void setJDXValue(Value value) throws DebugException;
}
