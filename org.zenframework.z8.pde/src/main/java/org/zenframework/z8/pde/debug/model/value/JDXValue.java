package org.zenframework.z8.pde.debug.model.value;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;

import org.zenframework.z8.compiler.core.IType;
import org.zenframework.z8.compiler.parser.type.Primary;
import org.zenframework.z8.compiler.workspace.Workspace;
import org.zenframework.z8.pde.debug.JDXMessages;
import org.zenframework.z8.pde.debug.model.JDXDebugElement;
import org.zenframework.z8.pde.debug.model.JDXDebugTarget;
import org.zenframework.z8.pde.debug.model.JDXStackFrame;
import org.zenframework.z8.pde.debug.model.JDXThread;
import org.zenframework.z8.pde.debug.model.type.JDXReferenceType;
import org.zenframework.z8.pde.debug.model.type.JDXType;
import org.zenframework.z8.pde.debug.model.variable.JDXFieldVariable;
import org.zenframework.z8.pde.debug.model.variable.JDXVariable;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.ClassObjectReference;
import com.sun.jdi.Field;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectCollectedException;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.PrimitiveValue;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.StringReference;
import com.sun.jdi.Type;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.Value;

public class JDXValue extends JDXDebugElement implements IValue {
	private Value m_value;
	private JDXVariable m_variable;
	private JDXThread m_thread;
	private boolean m_allocated = true;

	public JDXValue(JDXDebugTarget target, JDXThread thread, JDXVariable variable, Value value) {
		super(target);
		m_thread = thread;
		m_variable = variable;
		m_value = value;
	}

	@Override
	@SuppressWarnings("rawtypes")
	public Object getAdapter(Class adapter) {
		if(adapter == IValue.class) {
			return this;
		}
		return super.getAdapter(adapter);
	}

	static public boolean isArray(Value value) {
		return JDXReferenceType.isArray(value.type());
	}

	static public boolean isMap(Value value) {
		return JDXReferenceType.isMap(value.type());
	}

	static public boolean isMapEntry(Value value) {
		return JDXReferenceType.isMapEntry(value.type());
	}

	public static JDXValue createValue(JDXDebugTarget target, JDXThread thread, JDXVariable variable, Value value) {
		if(value == null) {
			return new JDXNullValue(target, thread, variable);
		}
		if(value instanceof ClassObjectReference) {
			return new JDXClassObjectValue(target, thread, variable, (ClassObjectReference)value);
		}
		if(isArray(value)) {
			return new JDXArrayValue(target, thread, variable, (ObjectReference)value);
		}
		if(isMap(value)) {
			return new JDXMapValue(target, thread, variable, (ObjectReference)value);
		}
		if(isMapEntry(value)) {
			return new JDXMapEntryValue(target, thread, variable, (ObjectReference)value);
		}
		if(value instanceof ObjectReference) {
			ObjectReference object = (ObjectReference)value;
			String typeName = value.type().name();

			if(typeName.indexOf("$RCLASS") != -1 || typeName.indexOf(".RCLASS") != -1) {
				return createValue(target, thread, variable, getFieldValue(value, "m_object"));
			}

			IType type = Workspace.getInstance().lookupType(value.type().name());

			if(type != null) {
				typeName = type.getUserName();

				if(typeName.equals(Primary.Boolean) || typeName.equals(Primary.Integer) || typeName.equals(Primary.Decimal) || typeName.equals(Primary.Guid) || typeName.equals(Primary.Date) || typeName.equals(Primary.Datespan))
					return new JDXPrimitiveValue(target, thread, variable, object);

				if(typeName.equals(Primary.String))
					return new JDXStringValue(target, thread, variable, object);
			}
			return new JDXObjectValue(target, thread, variable, (ObjectReference)value);
		}
		if(value instanceof PrimitiveValue) {
			return new JavaPrimitiveValue(target, thread, variable, value);
		}
		return new JDXValue(target, thread, variable, value);
	}

	protected static Value getFieldValue(Value value, String name) {
		assert (value instanceof ObjectReference);

		ObjectReference object = (ObjectReference)value;
		Field field = object.referenceType().fieldByName(name);

		assert (field != null);

		return object.getValue(field);
	}

	public JDXThread getJDXThread() {
		return m_thread;
	}

	public JDXVariable getJDXVariable() {
		return m_variable;
	}

	@Override
	public String getValueString() throws DebugException {
		if(m_value == null) {
			return JDXMessages.JDXValue_null_4;
		}
		if(m_value instanceof StringReference) {
			try {
				return ((StringReference)m_value).value();
			} catch(ObjectCollectedException e) {
				return JDXMessages.JDXValue_deallocated;
			} catch(RuntimeException e) {
				targetRequestFailed(MessageFormat.format(JDXMessages.JDXValue_exception_retrieving_value, new Object[] { e.toString() }), e);
				return null;
			}
		}

		if(m_value instanceof ObjectReference) {
			StringBuffer name = new StringBuffer();

			ObjectReference object = (ObjectReference)m_value;
			ReferenceType type = object.referenceType();

			List<Method> method = type.methodsByName("toDebugString", "()Ljava/lang/String;");
			assert (method.size() <= 1);

			if(method.size() != 0) {
				Value stringValue = getJDXThread().invokeMethod(object, method.get(0), new ArrayList<Value>());

				if(stringValue != null) {
					String value = ((StringReference)stringValue).value();

					if(value == null) {
						return JDXMessages.JDXValue_null_4;
					}

					return value;
				}
			}

			/*
			 * if(m_value instanceof ClassObjectReference) { name.append('(');
			 * name.append(((ClassObjectReference)m_value).reflectedType());
			 * name.append(')'); }
			 * 
			 * name.append(" ("); name.append(JDXMessages.JDXValue_id_8);
			 * name.append('=');
			 * 
			 * try { name.append(((ObjectReference)m_value).uniqueID()); }
			 * catch(RuntimeException e) {
			 * targetRequestFailed(MessageFormat.format(JDXMessages.
			 * JDXValue_exception_retrieving_unique_id, new Object[] {
			 * e.toString() }), e); return null; } name.append(')');
			 */
			return name.toString();
		}
		return String.valueOf(m_value);
	}

	@Override
	public String getReferenceTypeName() throws DebugException {
		try {
			if(m_value == null) {
				return JDXMessages.JDXValue_null_4;
			}
			return getUnderlyingType().name();
		} catch(RuntimeException e) {
			targetRequestFailed(MessageFormat.format(JDXMessages.JDXValue_exception_retrieving_reference_type_name, new Object[] { e.toString() }), e);
			return null;
		}
	}

	@Override
	public IVariable[] getVariables() throws DebugException {
		List<IVariable> list = getVariablesList();
		return list.toArray(new IVariable[list.size()]);
	}

	private String getSourcePath(ReferenceType refType) {
		try {
			List<String> paths = refType.sourcePaths(null);
			return paths.size() != 0 ? paths.get(0) : null;
		} catch(AbsentInformationException e) {
			return null;
		}
	}

	protected synchronized List<IVariable> getVariablesList() throws DebugException {
		List<IVariable> variables = new ArrayList<IVariable>();

		if(m_value instanceof ObjectReference) {
			ObjectReference object = (ObjectReference)m_value;

			IType type = null;
			List<Field> fields = null;

			try {
				ReferenceType refType = object.referenceType();
				fields = refType.allFields();
				type = JDXStackFrame.getType(getSourcePath(refType));

			} catch(ObjectCollectedException e) {
				return new ArrayList<IVariable>();
			} catch(RuntimeException e) {
				targetRequestFailed(MessageFormat.format(JDXMessages.JDXValue_exception_retrieving_fields, new Object[] { e.toString() }), e);
				return null;
			}

			Iterator<Field> list = fields.iterator();

			while(list.hasNext()) {
				Field field = list.next();

				if(type != null && type.findMember(field.name()) != null || field.name().equals("container"))
				/*
				 * String typeName = field.typeName();
				 * 
				 * if(JDXReferenceType.isWrapped(typeName) ||
				 * JDXReferenceType.isArray(typeName) ||
				 * JDXReferenceType.isMap(typeName) ||
				 * Workspace.getInstance().lookupType(typeName) != null ||
				 * field.name().equals("container"))
				 */
				{
					variables.add(new JDXFieldVariable(getJDXDebugTarget(), getJDXThread(), field, object));
				}
			}

			Collections.sort(variables, new Comparator<IVariable>() {
				@Override
				public int compare(IVariable a, IVariable b) {
					return sortChildren(a, b);
				}
			});
		}

		return variables;
	}

	protected int sortChildren(IVariable a, IVariable b) {
		try {
			return a.getName().compareToIgnoreCase(b.getName());
		} catch(DebugException de) {
			logError(de);
			return -1;
		}
	}

	@Override
	public boolean isAllocated() throws DebugException {
		if(m_allocated) {
			if(m_value instanceof ObjectReference) {
				try {
					m_allocated = !((ObjectReference)m_value).isCollected();
				} catch(VMDisconnectedException e) {
					m_allocated = false;
				} catch(RuntimeException e) {
					targetRequestFailed(MessageFormat.format(JDXMessages.JDXValue_exception_is_collected, new Object[] { e.toString() }), e);
				}
			} else {
				m_allocated = getJDXDebugTarget().isAvailable();
			}
		}
		return m_allocated;
	}

	public String getSignature() throws DebugException {
		try {
			if(m_value != null) {
				return m_value.type().signature();
			}
			return null;
		} catch(RuntimeException e) {
			targetRequestFailed(MessageFormat.format(JDXMessages.JDXValue_exception_retrieving_type_signature, new Object[] { e.toString() }), e);
			return null;
		}
	}

	public String getGenericSignature() throws DebugException {
		try {
			if(m_value != null) {
				Type type = m_value.type();
				if(type instanceof ReferenceType) {
					return ((ReferenceType)type).genericSignature();
				}
				return null;
			}
			return null;
		} catch(RuntimeException e) {
			targetRequestFailed(MessageFormat.format(JDXMessages.JDXValue_exception_retrieving_type_signature, new Object[] { e.toString() }), e);
			return null;
		}
	}

	public Value getUnderlyingValue() {
		return m_value;
	}

	public JDXType getJDXType() throws DebugException {
		if(getUnderlyingValue() == null) {
			return null;
		}

		return JDXType.createType(getJDXDebugTarget(), getJDXThread(), getJDXVariable(), getUnderlyingType());
	}

	protected Type getUnderlyingType() throws DebugException {
		try {
			return getUnderlyingValue().type();
		} catch(RuntimeException e) {
			targetRequestFailed(MessageFormat.format(JDXMessages.JDXValue_exception_retrieving_type, new Object[] { e.toString() }), e);
			return null;
		}
	}

	@Override
	public String toString() {
		return getUnderlyingValue().toString();
	}

	@Override
	public boolean hasVariables() throws DebugException {
		return getVariablesList().size() > 0;
	}
}
