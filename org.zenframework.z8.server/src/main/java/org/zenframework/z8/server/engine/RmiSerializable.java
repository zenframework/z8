package org.zenframework.z8.server.engine;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public interface RmiSerializable {
	public static final int Self = 500;
	public static final int String = 1000;
	public static final int Primary = 1001;
	public static final int Array = 1002;
	public static final int Proxy = 1003;
	public static final int Exception = 1004;
	public static final int ObjID = 1005;
	public static final int Lease = 1006;
	
	public void serialize(ObjectOutputStream stream) throws IOException;
	public void deserialize(ObjectInputStream stream) throws IOException, ClassNotFoundException;
}
