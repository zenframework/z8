package org.zenframework.z8.pde.debug.model.type;

import java.text.MessageFormat;
import java.util.Iterator;
import java.util.List;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IValue;

import org.zenframework.z8.compiler.core.IType;
import org.zenframework.z8.compiler.parser.BuiltinNative;
import org.zenframework.z8.compiler.workspace.Workspace;
import org.zenframework.z8.pde.debug.JDXMessages;
import org.zenframework.z8.pde.debug.model.JDXDebugTarget;
import org.zenframework.z8.pde.debug.model.JDXThread;
import org.zenframework.z8.pde.debug.model.value.JDXClassObjectValue;
import org.zenframework.z8.pde.debug.model.value.JDXValue;
import org.zenframework.z8.pde.debug.model.variable.JDXVariable;
import org.zenframework.z8.pde.debug.util.Signature;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.ArrayType;
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.Field;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.Type;

public abstract class JDXReferenceType extends JDXType {
	private String[] m_declaredFields = null;
	private String[] m_allFields = null;

	public JDXReferenceType(JDXDebugTarget target, JDXThread thread, JDXVariable variable, Type type) {
		super(target, thread, variable, type);
	}

	public String[] getAvailableStrata() {
		List<String> strata = getReferenceType().availableStrata();
		return strata.toArray(new String[strata.size()]);
	}

	protected ReferenceType getReferenceType() {
		return (ReferenceType)getUnderlyingType();
	}

	public String getDefaultStratum() throws DebugException {
		try {
			return getReferenceType().defaultStratum();
		} catch(RuntimeException e) {
			targetRequestFailed(JDXMessages.JDXReferenceType_1, e);
		}
		return null;
	}

	/*
	 * public IVariable getField(String name) throws DebugException { try {
	 * ReferenceType type = (ReferenceType)getUnderlyingType(); Field field =
	 * type.fieldByName(name); if(field != null && field.isStatic()) { return
	 * new JDXFieldVariable(getDebugTarget(), getJDXThread(), field, type); } }
	 * catch(RuntimeException e) {
	 * getDebugTarget().targetRequestFailed(MessageFormat.format(JDXMessages.
	 * JDXClassType_exception_while_retrieving_field, new Object[] {
	 * e.toString(), name }), e); } return null; }
	 */
	public JDXClassObjectValue getClassObject() throws DebugException {
		try {
			ReferenceType type = (ReferenceType)getUnderlyingType();
			return (JDXClassObjectValue)JDXValue.createValue(getDebugTarget(), getJDXThread(), getJDXVariable(), type.classObject());
		} catch(RuntimeException e) {
			getDebugTarget().targetRequestFailed(MessageFormat.format(JDXMessages.JDXClassType_exception_while_retrieving_class_object, new Object[] { e.toString() }), e);
		}
		return null;
	}

	public String[] getAllFieldNames() throws DebugException {
		if(m_allFields == null) {
			try {
				List<Field> fields = ((ReferenceType)getUnderlyingType()).allFields();

				m_allFields = new String[fields.size()];

				Iterator<Field> iterator = fields.iterator();

				int i = 0;

				while(iterator.hasNext()) {
					Field field = iterator.next();
					m_allFields[i] = field.name();
					i++;
				}
			} catch(RuntimeException e) {
				getDebugTarget().targetRequestFailed(JDXMessages.JDXReferenceType_2, e);
			}
		}
		return m_allFields;
	}

	public String[] getDeclaredFieldNames() throws DebugException {
		if(m_declaredFields == null) {
			try {
				List<Field> fields = ((ReferenceType)getUnderlyingType()).fields();

				m_declaredFields = new String[fields.size()];

				Iterator<Field> iterator = fields.iterator();

				int i = 0;
				while(iterator.hasNext()) {
					Field field = iterator.next();
					m_declaredFields[i] = field.name();
					i++;
				}
			} catch(RuntimeException e) {
				getDebugTarget().targetRequestFailed(JDXMessages.JDXReferenceType_3, e);
			}
		}
		return m_declaredFields;
	}

	public String[] getSourcePaths(String stratum) {
		try {
			List<String> sourcePaths = getReferenceType().sourcePaths(stratum);
			return sourcePaths.toArray(new String[sourcePaths.size()]);
		} catch(AbsentInformationException e) {
			return new String[0];
		}
	}

	public IValue getClassLoaderObject() throws DebugException {
		try {
			ReferenceType type = (ReferenceType)getUnderlyingType();
			return JDXValue.createValue(getDebugTarget(), getJDXThread(), getJDXVariable(), type.classLoader());
		} catch(RuntimeException e) {
			getDebugTarget().targetRequestFailed(MessageFormat.format(JDXMessages.JDXReferenceType_0, new Object[] { e.toString() }), e);
		}
		return null;
	}

	static public boolean isArray(Type type) {
		return isArray(type.name());
	}

	static public boolean isArray(String typeName) {
		return typeName.startsWith(BuiltinNative.ArrayQualifiedName);
	}

	static public boolean isMap(Type type) {
		return isMap(type.name());
	}

	static public boolean isMap(String typeName) {
		return typeName.startsWith(BuiltinNative.MapQualifiedName);
	}

	static public boolean isMapEntry(Type type) {
		return isMapEntry(type.name());
	}

	static public boolean isMapEntry(String typeName) {
		return typeName.startsWith(BuiltinNative.MapEntry);
	}

	static public boolean isWrapped(Type type) {
		return isWrapped(type.name());
	}

	static public boolean isWrapped(String typeName) {
		return typeName.startsWith(BuiltinNative.ClassQualifiedName);
	}

	static public String unwrap(String typeName) {
		if(isWrapped(typeName)) {
			int lastSpaceIndex = typeName.lastIndexOf(' ');
			return typeName.substring(lastSpaceIndex + 1, typeName.length() - 1);
		}
		return typeName;
	}

	static public String getGenericName(ReferenceType type) throws DebugException {
		if(type instanceof ArrayType) {
			try {
				Type componentType;
				componentType = ((ArrayType)type).componentType();
				if(componentType instanceof ReferenceType) {
					return getGenericName((ReferenceType)componentType) + "[]"; //$NON-NLS-1$
				}
				return type.name();
			} catch(ClassNotLoadedException e) {
				// we cannot create the generic name using the component type,
				// just try to create one with the infos
			}
		}

		String signature = type.signature();
		StringBuffer res = new StringBuffer(getTypeName(signature));

		/*
		 * String genericSignature = type.genericSignature();
		 * 
		 * if(genericSignature != null) { String[] typeParameters =
		 * Signature.getTypeParameters(genericSignature);
		 * if(typeParameters.length > 0) {
		 * res.append('<').append(Signature.getTypeVariable(typeParameters[0]));
		 * for(int i = 1; i < typeParameters.length; i++) {
		 * res.append(',').append(Signature.getTypeVariable(typeParameters[i]));
		 * } res.append('>'); } }
		 */
		return res.toString();
	}

	static public String getTypeName(String genericTypeSignature) {
		// return genericTypeSignature;

		int arrayDimension = 0;

		while(genericTypeSignature.charAt(arrayDimension) == '[') {
			arrayDimension++;
		}

		StringBuffer name = new StringBuffer();
		int parameterStart = genericTypeSignature.indexOf('<');

		if(parameterStart < 0) {
			name.append(genericTypeSignature.substring(arrayDimension + 1, genericTypeSignature.length() - 1).replace('/', '.'));
		} else {
			name.append(genericTypeSignature.substring(arrayDimension + 1, parameterStart).replace('/', '.'));
			name.append(Signature.toString(genericTypeSignature).substring(parameterStart - 1 - arrayDimension).replace('/', '.'));
		}
		for(int i = 0; i < arrayDimension; i++) {
			name.append("[]");
		}

		return getUserTypeName(name.toString());
	}

	static protected String getUserTypeName(String typeName) {
		String index = "";

		if(isArray(typeName)) {
			int bracket = typeName.indexOf('<');

			if(bracket != -1)
				typeName = getUserTypeName(unwrap(typeName.substring(BuiltinNative.ArrayQualifiedName.length() + 1, typeName.length() - 1)));
			else
				typeName = "value";

			index += "[]";
		} else if(isMap(typeName)) {
			int bracket = typeName.indexOf('<');

			if(bracket != -1) {
				typeName = typeName.substring(bracket + 1, typeName.length() - 1);
				int comma = typeName.indexOf(',');

				/*
				 * String key = unwrap(typeName.substring(0, comma));
				 * 
				 * IType type = Workspace.getInstance().lookupType(key);
				 * 
				 * if(type != null) { key = type.getUserName(); }
				 */
				index = "[key]";

				typeName = getUserTypeName(unwrap(typeName.substring(comma + 1)));
			} else {
				typeName = "value[key]";
			}
		}

		IType type = Workspace.getInstance().lookupType(typeName);

		if(type != null) {
			typeName = type.getUserName();
		}

		return typeName + index;
	}

	public String getGenericSignature() {
		return getReferenceType().genericSignature();
	}
}
