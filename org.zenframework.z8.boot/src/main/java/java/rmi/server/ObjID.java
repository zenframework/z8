package java.rmi.server;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;

public final class ObjID implements Serializable {
	private static final long serialVersionUID = -2041779290797310925L;

	private long objNum = 0;

	public ObjID() {
		this(1000000);
	}

	public ObjID(int objNum) {
		this((long)objNum);
	}

	private ObjID(long objNum) {
		this.objNum = objNum;
	}

	public void write(ObjectOutput out) throws IOException {
		out.writeLong(objNum);
	}

	public static ObjID read(ObjectInput in) throws IOException {
		return new ObjID(in.readLong());
	}

	@Override
	public int hashCode() {
		return (int)objNum;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ObjID) {
			ObjID id = (ObjID) obj;
			return objNum == id.objNum;
		}

		return false;
	}

	@Override
	public String toString() {
		return "[" + objNum + "]";
	}
}
