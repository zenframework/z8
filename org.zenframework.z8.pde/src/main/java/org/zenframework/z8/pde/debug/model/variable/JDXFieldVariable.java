package org.zenframework.z8.pde.debug.model.variable;

import java.text.MessageFormat;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;

import org.zenframework.z8.pde.debug.JDXMessages;
import org.zenframework.z8.pde.debug.model.JDXDebugTarget;
import org.zenframework.z8.pde.debug.model.JDXThread;
import org.zenframework.z8.pde.debug.model.type.JDXReferenceType;
import org.zenframework.z8.pde.debug.model.type.JDXType;
import org.zenframework.z8.pde.debug.model.value.JDXValue;

import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.Field;
import com.sun.jdi.InterfaceType;
import com.sun.jdi.InvalidTypeException;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.Type;
import com.sun.jdi.Value;

public class JDXFieldVariable extends JDXModificationVariable implements IVariable {
	private Field m_field;
	private ObjectReference m_object;
	private ReferenceType m_type;

	public JDXFieldVariable(JDXDebugTarget target, JDXThread thread, Field field, ObjectReference object) {
		super(target, thread);

		m_field = field;
		m_object = object;
		m_type = (ReferenceType)m_object.type();
	}

	public JDXFieldVariable(JDXDebugTarget target, JDXThread thread, Field field, ReferenceType refType) {
		super(target, thread);

		m_field = field;
		m_type = refType;
	}

	@Override
	protected Value retrieveValue() {
		Value value = null;

		if(getField().isStatic()) {
			value = (getField().declaringType().getValue(getField()));
		} else {
			value = getObjectReference().getValue(getField());

			if(JDXReferenceType.isWrapped(getField().typeName())) {
				ObjectReference object = (ObjectReference)value;
				ReferenceType type = (ReferenceType)object.type();
				Field field = type.fieldByName("m_object");
				return object.getValue(field);
			}
		}
		return value;
	}

	public JDXType getDeclaringType() {
		return JDXType.createType((JDXDebugTarget)getDebugTarget(), getJDXThread(), this, m_field.declaringType());
	}

	@Override
	public String getName() {
		return m_field.name();
	}

	@Override
	protected void setJDXValue(Value value) throws DebugException {
		try {
			getObjectReference().setValue(getField(), value);
			fireChangeEvent(DebugEvent.CONTENT);
		} catch(ClassNotLoadedException e) {
			targetRequestFailed(MessageFormat.format(JDXMessages.JDXFieldVariable_exception_modifying_value, new Object[] { e.toString() }), e);
		} catch(InvalidTypeException e) {
			targetRequestFailed(MessageFormat.format(JDXMessages.JDXFieldVariable_exception_modifying_value, new Object[] { e.toString() }), e);
		} catch(RuntimeException e) {
			targetRequestFailed(MessageFormat.format(JDXMessages.JDXFieldVariable_exception_modifying_value, new Object[] { e.toString() }), e);
		}
	}

	@Override
	public String getReferenceTypeName() throws DebugException {
		String typeName;

		String genericSignature = getField().genericSignature();

		if(genericSignature != null) {
			typeName = JDXReferenceType.getTypeName(genericSignature);
		} else {
			Type type = getUnderlyingType();

			if(type instanceof ReferenceType) {
				typeName = JDXReferenceType.getGenericName((ReferenceType)type);
			} else {
				typeName = getField().typeName();
			}
		}

		return JDXReferenceType.unwrap(typeName);
	}

	public Field getField() {
		return m_field;
	}

	public ObjectReference getObjectReference() {
		return m_object;
	}

	public ReferenceType getReferenceType() {
		return m_type;
	}

	@Override
	public boolean supportsValueModification() {
		if(getField().declaringType() instanceof InterfaceType) {
			return false;
		}
		return super.supportsValueModification();
	}

	@Override
	public String toString() {
		return getField().toString();
	}

	@Override
	public void setValue(IValue v) throws DebugException {
		if(verifyValue(v)) {
			setJDXValue(((JDXValue)v).getUnderlyingValue());
		}
	}

	@Override
	protected Type getUnderlyingType() throws DebugException {
		try {
			return getField().type();
		} catch(ClassNotLoadedException e) {
			targetRequestFailed(MessageFormat.format(JDXMessages.JDXFieldVariable_exception_while_retrieving_type_of_field, new Object[] { e.toString() }), e);
		} catch(RuntimeException e) {
			targetRequestFailed(MessageFormat.format(JDXMessages.JDXFieldVariable_exception_while_retrieving_type_of_field, new Object[] { e.toString() }), e);
		}
		return null;
	}

	public IValue getReceiver() {
		ObjectReference objectReference = getObjectReference();
		if(objectReference == null) {
			return null;
		}
		return JDXValue.createValue(getJDXDebugTarget(), getJDXThread(), this, objectReference);
	}

	public JDXReferenceType getReceivingType() {
		return (JDXReferenceType)JDXType.createType(getJDXDebugTarget(), getJDXThread(), this, getReferenceType());
	}
}
