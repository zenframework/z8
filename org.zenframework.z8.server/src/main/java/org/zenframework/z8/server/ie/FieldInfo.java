package org.zenframework.z8.server.ie;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.engine.RmiIO;
import org.zenframework.z8.server.engine.RmiSerializable;
import org.zenframework.z8.server.types.binary;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.date;
import org.zenframework.z8.server.types.datespan;
import org.zenframework.z8.server.types.decimal;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.integer;
import org.zenframework.z8.server.types.primary;
import org.zenframework.z8.server.types.string;

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
	
	public int size() {
		int size = 0;
		size += name.length();
		
		if(value instanceof binary)
			size += 0;//((binary)value).get()
		else if(value instanceof bool)
			size += 9;
		else if(value instanceof date)
			size += 432;
		else if(value instanceof guid)
			size += 40;
		else if(value instanceof integer || value instanceof datespan || value instanceof decimal)
			size += 16;
		else if(value instanceof string)
			size += 40 + ((string)value).z8_length().get()*2;
		
		
		return size;
	}
}
