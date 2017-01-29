package org.zenframework.z8.pde.debug.model.variable;

import java.text.MessageFormat;

import org.eclipse.debug.core.DebugException;

import org.zenframework.z8.pde.debug.JDXMessages;
import org.zenframework.z8.pde.debug.model.JDXDebugTarget;
import org.zenframework.z8.pde.debug.model.JDXThread;

import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.Type;
import com.sun.jdi.Value;

public class JDXThisVariable extends JDXVariable {
	private ObjectReference fObject;

	public JDXThisVariable(JDXDebugTarget target, JDXThread thread, ObjectReference object) {
		super(target, thread);
		fObject = object;
	}

	@Override
	public Value retrieveValue() {
		return fObject;
	}

	@Override
	public String getName() {
		return "this";
	}

	public String getSignature() throws DebugException {
		try {
			return retrieveValue().type().signature();
		} catch(RuntimeException e) {
			targetRequestFailed(MessageFormat.format(JDXMessages.JDXThisVariableexception_retrieving_type_signature, new Object[] { e.toString() }), e);
			return null;
		}
	}

	public String getGenericSignature() throws DebugException {
		return getSignature();
	}

	@Override
	public String getReferenceTypeName() throws DebugException {
		try {
			return getValue().getReferenceTypeName();
		} catch(RuntimeException e) {
			targetRequestFailed(MessageFormat.format(JDXMessages.JDXThisVariableexception_retrieving_reference_type_name, new Object[] { e.toString() }), e);
			return null;
		}
	}

	@Override
	protected Type getUnderlyingType() throws DebugException {
		try {
			return retrieveValue().type();
		} catch(RuntimeException e) {
			targetRequestFailed(MessageFormat.format(JDXMessages.JDXThisVariable_exception_while_retrieving_type_this, new Object[] { e.toString() }), e);
		}
		return null;
	}

	public boolean isPrivate() throws DebugException {
		try {
			return ((ReferenceType)getUnderlyingType()).isPrivate();
		} catch(RuntimeException e) {
			targetRequestFailed(JDXMessages.JDXThisVariable_Exception_occurred_while_retrieving_modifiers__1, e);
		}
		return false;
	}

	public boolean isProtected() throws DebugException {
		try {
			return ((ReferenceType)getUnderlyingType()).isProtected();
		} catch(RuntimeException e) {
			targetRequestFailed(JDXMessages.JDXThisVariable_Exception_occurred_while_retrieving_modifiers__1, e);
		}
		return false;
	}

	public boolean isPublic() throws DebugException {
		try {
			return ((ReferenceType)getUnderlyingType()).isPublic();
		} catch(RuntimeException e) {
			targetRequestFailed(JDXMessages.JDXThisVariable_Exception_occurred_while_retrieving_modifiers__1, e);
		}
		return false;
	}

	@Override
	public boolean equals(Object o) {
		if(o instanceof JDXThisVariable) {
			return ((JDXThisVariable)o).fObject.equals(fObject);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return fObject.hashCode();
	}
}
