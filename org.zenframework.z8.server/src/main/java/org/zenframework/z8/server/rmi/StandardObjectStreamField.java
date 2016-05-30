package org.zenframework.z8.server.rmi;

import java.lang.reflect.Field;

public class StandardObjectStreamField implements Comparable<Object> {
	private final String name;
	private final String signature;
	private final Class<?> type;
	private final boolean unshared;
	private final Field field;
	private int offset = 0;

	public StandardObjectStreamField(String name, Class<?> type) {
		this(name, type, false);
	}

	public StandardObjectStreamField(String name, Class<?> type, boolean unshared) {
		if(name == null) {
			throw new NullPointerException();
		}
		this.name = name;
		this.type = type;
		this.unshared = unshared;
		signature = getClassSignature(type).intern();
		field = null;
	}

	StandardObjectStreamField(String name, String signature, boolean unshared) {
		if(name == null) {
			throw new NullPointerException();
		}
		this.name = name;
		this.signature = signature.intern();
		this.unshared = unshared;
		field = null;

		switch(signature.charAt(0)) {
		case 'Z':
			type = Boolean.TYPE;
			break;
		case 'B':
			type = Byte.TYPE;
			break;
		case 'C':
			type = Character.TYPE;
			break;
		case 'S':
			type = Short.TYPE;
			break;
		case 'I':
			type = Integer.TYPE;
			break;
		case 'J':
			type = Long.TYPE;
			break;
		case 'F':
			type = Float.TYPE;
			break;
		case 'D':
			type = Double.TYPE;
			break;
		case 'L':
		case '[':
			type = Object.class;
			break;
		default:
			throw new IllegalArgumentException("illegal signature");
		}
	}

	StandardObjectStreamField(Field field, boolean unshared, boolean showType) {
		this.field = field;
		this.unshared = unshared;
		name = field.getName();
		Class<?> ftype = field.getType();
		type = (showType || ftype.isPrimitive()) ? ftype : Object.class;
		signature = getClassSignature(ftype).intern();
	}

	public String getName() {
		return name;
	}

	public Class<?> getType() {
		return type;
	}

	public char getTypeCode() {
		return signature.charAt(0);
	}

	public String getTypeString() {
		return isPrimitive() ? null : signature;
	}

	public int getOffset() {
		return offset;
	}

	protected void setOffset(int offset) {
		this.offset = offset;
	}

	public boolean isPrimitive() {
		char tcode = signature.charAt(0);
		return ((tcode != 'L') && (tcode != '['));
	}

	public boolean isUnshared() {
		return unshared;
	}

	public int compareTo(Object obj) {
		StandardObjectStreamField other = (StandardObjectStreamField)obj;
		boolean isPrim = isPrimitive();
		if(isPrim != other.isPrimitive()) {
			return isPrim ? -1 : 1;
		}
		return name.compareTo(other.name);
	}

	public String toString() {
		return signature + ' ' + name;
	}

	Field getField() {
		return field;
	}

	String getSignature() {
		return signature;
	}

	private static String getClassSignature(Class<?> cl) {
		StringBuilder sbuf = new StringBuilder();
		while(cl.isArray()) {
			sbuf.append('[');
			cl = cl.getComponentType();
		}
		if(cl.isPrimitive()) {
			if(cl == Integer.TYPE) {
				sbuf.append('I');
			} else if(cl == Byte.TYPE) {
				sbuf.append('B');
			} else if(cl == Long.TYPE) {
				sbuf.append('J');
			} else if(cl == Float.TYPE) {
				sbuf.append('F');
			} else if(cl == Double.TYPE) {
				sbuf.append('D');
			} else if(cl == Short.TYPE) {
				sbuf.append('S');
			} else if(cl == Character.TYPE) {
				sbuf.append('C');
			} else if(cl == Boolean.TYPE) {
				sbuf.append('Z');
			} else if(cl == Void.TYPE) {
				sbuf.append('V');
			} else {
				throw new InternalError();
			}
		} else {
			sbuf.append('L' + cl.getName().replace('.', '/') + ';');
		}
		return sbuf.toString();
	}
}
