package java.rmi.server;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.util.concurrent.atomic.AtomicLong;

public final class ObjID implements Serializable {
	private static final long serialVersionUID = -2041779290797310925L;

	private static final AtomicLong nextObjNum = new AtomicLong(1000000);

	private long objNum = 0;

	public ObjID() {
		this(nextObjNum.getAndIncrement());
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

	public int hashCode() {
		return (int)objNum;
	}

	public boolean equals(Object obj) {
        if (obj instanceof ObjID) {
            ObjID id = (ObjID) obj;
            return objNum == id.objNum;
        }

        return false;
	}

	public String toString() {
		return "[" + objNum + "]";
	}
}
