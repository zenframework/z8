package org.zenframework.z8.pde;

import org.eclipse.swt.dnd.ByteArrayTransfer;

public class Z8Transfer extends ByteArrayTransfer {

	private static Z8Transfer instance = new Z8Transfer();

	private Object object;

	private static final String TYPE_NAME = "Local Z8 Transfer"//$NON-NLS-1$
			+ System.currentTimeMillis() + ":" + instance.hashCode();//$NON-NLS-1$
	private static final int TYPEID = registerType(TYPE_NAME);

	private Z8Transfer() {
	}

	public static Z8Transfer getInstance() {
		return instance;
	}

	@Override
	protected int[] getTypeIds() {
		return new int[] { TYPEID };
	}

	@Override
	protected String[] getTypeNames() {
		return new String[] { TYPE_NAME };
	}

	public Object getObject() {
		return object;
	}

	public void setObject(Object obj) {
		object = obj;
	}

}
