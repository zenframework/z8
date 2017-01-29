package org.zenframework.z8.pde.debug.model.variable;

import java.text.MessageFormat;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IValue;

import org.zenframework.z8.pde.debug.JDXMessages;
import org.zenframework.z8.pde.debug.model.JDXDebugTarget;
import org.zenframework.z8.pde.debug.model.JDXStackFrame;
import org.zenframework.z8.pde.debug.model.JDXThread;
import org.zenframework.z8.pde.debug.model.type.JDXReferenceType;
import org.zenframework.z8.pde.debug.model.value.JDXValue;

import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.Field;
import com.sun.jdi.InvalidTypeException;
import com.sun.jdi.LocalVariable;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.Type;
import com.sun.jdi.Value;

public class JDXLocalVariable extends JDXModificationVariable {
	private LocalVariable m_local;
	private JDXStackFrame m_stackFrame;

	public JDXLocalVariable(JDXStackFrame frame, JDXThread thread, LocalVariable local) {
		super((JDXDebugTarget)frame.getDebugTarget(), thread);
		m_stackFrame = frame;
		m_local = local;
	}

	@Override
	protected Value retrieveValue() throws DebugException {
		synchronized(m_stackFrame.getThread()) {
			if(getStackFrame().isSuspended()) {
				Value value = getStackFrame().getUnderlyingStackFrame().getValue(m_local);

				if(JDXReferenceType.isWrapped(m_local.typeName())) {
					ObjectReference object = (ObjectReference)value;
					ReferenceType type = (ReferenceType)object.type();
					Field field = type.fieldByName("m_object");
					return object.getValue(field);
				}

				return value;

			}
		}
		return getLastKnownValue();
	}

	@Override
	public String getName() throws DebugException {
		try {
			return getLocal().name();
		} catch(RuntimeException e) {
			targetRequestFailed(MessageFormat.format(JDXMessages.JDXLocalVariable_exception_retrieving_local_variable_name, new Object[] { e.toString() }), e);
			return null;
		}
	}

	@Override
	protected void setJDXValue(Value value) throws DebugException {
		try {
			synchronized(getStackFrame().getThread()) {
				getStackFrame().getUnderlyingStackFrame().setValue(getLocal(), value);
			}
			fireChangeEvent(DebugEvent.CONTENT);
		} catch(ClassNotLoadedException e) {
			targetRequestFailed(MessageFormat.format(JDXMessages.JDXLocalVariable_exception_modifying_local_variable_value, new Object[] { e.toString() }), e);
		} catch(InvalidTypeException e) {
			targetRequestFailed(MessageFormat.format(JDXMessages.JDXLocalVariable_exception_modifying_local_variable_value, new Object[] { e.toString() }), e);
		} catch(RuntimeException e) {
			targetRequestFailed(MessageFormat.format(JDXMessages.JDXLocalVariable_exception_modifying_local_variable_value, new Object[] { e.toString() }), e);
		}
	}

	@Override
	public String getReferenceTypeName() throws DebugException {
		String typeName;

		try {
			String genericSignature = getLocal().genericSignature();

			if(genericSignature != null) {
				typeName = JDXReferenceType.getTypeName(genericSignature);
			} else {
				Type underlyingType = getUnderlyingType();

				if(underlyingType instanceof ReferenceType) {
					typeName = JDXReferenceType.getGenericName((ReferenceType)underlyingType);
				} else {
					typeName = getLocal().typeName();
				}
			}
		} catch(RuntimeException e) {
			targetRequestFailed(MessageFormat.format(JDXMessages.JDXLocalVariable_exception_retrieving_local_variable_type_name, new Object[] { e.toString() }), e);
			return null;
		}

		return JDXReferenceType.unwrap(typeName);
	}

	public void setLocal(LocalVariable local) {
		m_local = local;
	}

	public LocalVariable getLocal() {
		return m_local;
	}

	protected JDXStackFrame getStackFrame() {
		return m_stackFrame;
	}

	@Override
	public String toString() {
		return getLocal().toString();
	}

	@Override
	public void setValue(IValue v) throws DebugException {
		if(verifyValue(v)) {
			JDXValue value = (JDXValue)v;
			setJDXValue(value.getUnderlyingValue());
		}
	}

	@Override
	protected Type getUnderlyingType() throws DebugException {
		try {
			return getLocal().type();
		} catch(ClassNotLoadedException e) {
			targetRequestFailed(MessageFormat.format(JDXMessages.JDXLocalVariable_exception_while_retrieving_type_of_local_variable, new Object[] { e.toString() }), e);
		} catch(RuntimeException e) {
			targetRequestFailed(MessageFormat.format(JDXMessages.JDXLocalVariable_exception_while_retrieving_type_of_local_variable, new Object[] { e.toString() }), e);
		}
		return null;
	}

	@Override
	public boolean isLocal() {
		return true;
	}
}
