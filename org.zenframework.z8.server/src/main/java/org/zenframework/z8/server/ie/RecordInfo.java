package org.zenframework.z8.server.ie;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

import org.zenframework.z8.server.engine.RmiIO;
import org.zenframework.z8.server.engine.RmiSerializable;
import org.zenframework.z8.server.types.guid;

public class RecordInfo implements RmiSerializable, Serializable {
	private static final long serialVersionUID = 8082764538622310343L;

	private guid id;
	private String table;
	private Collection<FieldInfo> fields = new ArrayList<FieldInfo>();
	
	public RecordInfo() {
	}
	
	public RecordInfo(guid id, String name) {
		this.id = id;
		this.table = name;
	}
	
	public guid id() {
		return id;
	}

	public String table() {
		return table;
	}

	public Collection<FieldInfo> fields() {
		return fields;
	}

	public void add(FieldInfo field) {
		fields.add(field);
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

		RmiIO.writeGuid(out, id);
		RmiIO.writeString(out, table);
		out.writeObject(fields);
	}

	@Override
	@SuppressWarnings("unchecked")
	public void deserialize(ObjectInputStream in) throws IOException, ClassNotFoundException {
		@SuppressWarnings("unused")
		long version = RmiIO.readLong(in);

		id = RmiIO.readGuid(in);
		table = RmiIO.readString(in);
		fields = (Collection<FieldInfo>)in.readObject();
	}
}

