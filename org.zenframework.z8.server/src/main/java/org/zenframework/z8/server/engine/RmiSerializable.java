package org.zenframework.z8.server.engine;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public interface RmiSerializable {
	public static final int Null = 100;
	public static final int Self = 500;

	public static final int String = 1000;
	public static final int Primary = 1001;
	public static final int OBJECT = 1002;

	public static final int Array = 1103;
	public static final int Collection = 1104;
	public static final int Map = 1105;

	public static final int Proxy = 2000;
	public static final int Exception = 2001;
	public static final int ObjID = 2002;
	public static final int VMID = 2003;
	public static final int Lease = 2004;
	public static final int RemoteStub = 2005;
	
	public void serialize(ObjectOutputStream stream) throws IOException;
	public void deserialize(ObjectInputStream stream) throws IOException, ClassNotFoundException;
}
