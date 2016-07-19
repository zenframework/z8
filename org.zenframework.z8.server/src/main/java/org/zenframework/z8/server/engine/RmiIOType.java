package org.zenframework.z8.server.engine;

public class RmiIOType {
	public static final byte Null = 1;
	public static final byte Self = 2;

	public static final byte String = 10;
	public static final byte Integer = 11;
	public static final byte Long = 12;
	public static final byte Boolean = 13;
	public static final byte Byte = 14;
	public static final byte Char = 15;
	public static final byte Short = 16;
	public static final byte Float = 17;
	public static final byte Double = 18;

	public static final byte Primary = 30;
	public static final byte Date = 31;
	public static final byte Datespan = 32;
	public static final byte Datetime = 33;
	public static final byte Decimal = 34;
	public static final byte Guid = 35;
	public static final byte File = 36;
	public static final byte OBJECT = 37;

	public static final byte Array = 40;
	public static final byte Collection = 41;
	public static final byte Map = 42;
	public static final byte Enum = 43;

	public static final byte Proxy = 50;
	public static final byte Exception = 51;
	public static final byte ObjID = 52;
	public static final byte VMID = 53;
	public static final byte Lease = 54;
	public static final byte RemoteStub = 55;
}
