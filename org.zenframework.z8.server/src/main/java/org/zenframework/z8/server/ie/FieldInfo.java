package org.zenframework.z8.server.ie;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.engine.RmiIO;
import org.zenframework.z8.server.engine.RmiSerializable;
import org.zenframework.z8.server.types.primary;

public class FieldInfo implements RmiSerializable, Serializable {
	private static final long serialVersionUID = -5993039287020295974L;

	private String name;
	private primary value;
	
	public FieldInfo() {
	}
	
	public FieldInfo(Field field) {
		this.name = field.name();
		this.value = field.get();
	}

	public String name() {
		return name;
	}

	public primary value() {
		return value;
	}

	private void writeObject(ObjectOutputStream out) throws IOException {
		serialize(out);
	}

	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		deserialize(in);
	}

	@Override
	public void serialize(ObjectOutputStream out) throws IOException {
		RmiIO.writeLong(out, serialVersionUID);

		RmiIO.writeString(out, name);
		RmiIO.writePrimary(out, value);
	}

	@Override
	public void deserialize(ObjectInputStream in) throws IOException, ClassNotFoundException {
		@SuppressWarnings("unused")
		long version = RmiIO.readLong(in);

		name = RmiIO.readString(in);
		value = RmiIO.readPrimary(in);
	}
}

