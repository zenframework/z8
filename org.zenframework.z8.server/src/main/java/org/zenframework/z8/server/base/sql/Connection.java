package org.zenframework.z8.server.base.sql;

import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.OBJECT;

public class Connection extends OBJECT {
	public static class CLASS<T extends Connection> extends OBJECT.CLASS<T> {
		public CLASS() {
			this(null);
		}

		public CLASS(IObject container) {
			super(container);
			setJavaClass(Connection.class);
		}

		@Override
		public Object newObject(IObject container) {
			return new Connection(container);
		}
	}

	public org.zenframework.z8.server.db.Connection connection;

	public Connection(IObject container) {
		super(container);
	}

	public void z8_beginTransaction() {
		connection.beginTransaction();
	}

	public void z8_commit() {
		connection.commit();
	}

	public void z8_rollback() {
		connection.rollback();
	}

	public void z8_flush() {
		connection.flush();
	}
}
