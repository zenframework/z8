package org.zenframework.z8.pde.debug.model.value;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;

import org.zenframework.z8.pde.debug.JDXMessages;
import org.zenframework.z8.pde.debug.model.JDXDebugTarget;
import org.zenframework.z8.pde.debug.model.JDXThread;
import org.zenframework.z8.pde.debug.model.type.JDXReferenceType;
import org.zenframework.z8.pde.debug.model.variable.JDXFieldVariable;
import org.zenframework.z8.pde.debug.model.variable.JDXVariable;

import com.sun.jdi.ClassType;
import com.sun.jdi.Field;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VMDisconnectedException;

public class JDXObjectValue extends JDXValue {
	public JDXObjectValue(JDXDebugTarget target, JDXThread thread, JDXVariable variable, ObjectReference object) {
		super(target, thread, variable, object);
	}

	public ObjectReference getUnderlyingObject() {
		return (ObjectReference)getUnderlyingValue();
	}

	public IVariable getField(String name, boolean superField) throws DebugException {
		ReferenceType ref = getUnderlyingReferenceType();
		try {
			if(superField) {
				// begin lookup in superclass
				ref = ((ClassType)ref).superclass();
			}

			Field field = ref.fieldByName(name);

			if(field != null) {
				return new JDXFieldVariable((JDXDebugTarget)getDebugTarget(), getJDXThread(), field, getUnderlyingObject());
			}

			Field enclosingThis = null;

			Iterator<Field> fields = ref.fields().iterator();

			while(fields.hasNext()) {
				Field fieldTmp = fields.next();

				if(fieldTmp.name().startsWith("this$")) {
					enclosingThis = fieldTmp;
					break;
				}
			}
			return ((JDXObjectValue)(new JDXFieldVariable((JDXDebugTarget)getDebugTarget(), getJDXThread(), enclosingThis, getUnderlyingObject())).getValue()).getField(name, false);
		} catch(RuntimeException e) {
			targetRequestFailed(MessageFormat.format(JDXMessages.JDXObjectValue_exception_retrieving_field, new Object[] { e.toString() }), e);
		}
		return null;
	}

	public IVariable getField(String name, String declaringTypeSignature) throws DebugException {
		ReferenceType ref = getUnderlyingReferenceType();
		try {
			Field field = null;
			Field fieldTmp = null;
			Iterator<Field> fields = ref.allFields().iterator();

			while(fields.hasNext()) {
				fieldTmp = fields.next();

				if(name.equals(fieldTmp.name()) && declaringTypeSignature.equals(fieldTmp.declaringType().signature())) {
					field = fieldTmp;
					break;
				}
			}
			if(field != null) {
				return new JDXFieldVariable((JDXDebugTarget)getDebugTarget(), getJDXThread(), field, getUnderlyingObject());
			}
		} catch(RuntimeException e) {
			targetRequestFailed(MessageFormat.format(JDXMessages.JDXObjectValue_exception_retrieving_field, new Object[] { e.toString() }), e);
		}
		return null;
	}

	public IVariable getField(String name, int superClassLevel) throws DebugException {
		ReferenceType ref = getUnderlyingReferenceType();
		try {
			for(int i = 0; i < superClassLevel; i++) {
				ref = ((ClassType)ref).superclass();
			}
			Field field = ref.fieldByName(name);
			if(field != null) {
				return new JDXFieldVariable((JDXDebugTarget)getDebugTarget(), getJDXThread(), field, getUnderlyingObject());
			}
		} catch(RuntimeException e) {
			targetRequestFailed(MessageFormat.format(JDXMessages.JDXObjectValue_exception_retrieving_field, new Object[] { e.toString() }), e);
		}
		return null;
	}

	protected ReferenceType getUnderlyingReferenceType() throws DebugException {
		try {
			return getUnderlyingObject().referenceType();
		} catch(RuntimeException e) {
			targetRequestFailed(MessageFormat.format(JDXMessages.JDXObjectValue_exception_retrieving_reference_type, new Object[] { e.toString() }), e);
		}
		return null;
	}

	public IValue getEnclosingObject(int enclosingLevel) throws DebugException {
		JDXObjectValue res = this;
		for(int i = 0; i < enclosingLevel; i++) {
			ReferenceType ref = res.getUnderlyingReferenceType();
			try {
				Field enclosingThis = null, fieldTmp = null;
				Iterator<Field> fields = ref.fields().iterator();

				while(fields.hasNext()) {
					fieldTmp = fields.next();
					if(fieldTmp.name().startsWith("this$")) {
						enclosingThis = fieldTmp;
					}
				}
				if(enclosingThis != null) {
					res = (JDXObjectValue)(new JDXFieldVariable((JDXDebugTarget)getDebugTarget(), getJDXThread(), enclosingThis, res.getUnderlyingObject())).getValue();
				} else {
					return null;
				}
			} catch(RuntimeException e) {
				targetRequestFailed(MessageFormat.format(JDXMessages.JDXObjectValue_exception_retrieving_field, new Object[] { e.toString() }), e);
			}
		}
		return res;
	}

	public IThread[] getWaitingThreads() throws DebugException {
		List<IThread> waiting = new ArrayList<IThread>();
		try {
			List<ThreadReference> threads = getUnderlyingObject().waitingThreads();
			JDXDebugTarget debugTarget = getJDXDebugTarget();

			for(Iterator<ThreadReference> iter = threads.iterator(); iter.hasNext();) {
				JDXThread thread = debugTarget.findThread(iter.next());
				if(thread != null) {
					waiting.add(thread);
				}
			}
		} catch(IncompatibleThreadStateException e) {
			targetRequestFailed(JDXMessages.JDXObjectValue_0, e);
		} catch(VMDisconnectedException e) {
			// Ignore
		} catch(RuntimeException e) {
			targetRequestFailed(JDXMessages.JDXObjectValue_0, e);
		}
		return waiting.toArray(new IThread[waiting.size()]);
	}

	public IThread getOwningThread() throws DebugException {
		IThread owningThread = null;

		try {
			ThreadReference thread = getUnderlyingObject().owningThread();
			JDXDebugTarget debugTarget = getJDXDebugTarget();
			if(thread != null) {
				owningThread = debugTarget.findThread(thread);
			}
		} catch(IncompatibleThreadStateException e) {
			targetRequestFailed(JDXMessages.JDXObjectValue_1, e);
		} catch(VMDisconnectedException e) {
			return null;
		} catch(RuntimeException e) {
			targetRequestFailed(JDXMessages.JDXObjectValue_1, e);
		}
		return owningThread;
	}

	@Override
	public String getReferenceTypeName() throws DebugException {
		try {
			return JDXReferenceType.getGenericName(getUnderlyingReferenceType());
		} catch(RuntimeException e) {
			targetRequestFailed(MessageFormat.format(JDXMessages.JDXValue_exception_retrieving_reference_type_name, new Object[] { e.toString() }), e);
			return null;
		}
	}
}
