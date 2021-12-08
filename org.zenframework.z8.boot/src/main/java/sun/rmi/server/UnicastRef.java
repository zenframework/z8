package sun.rmi.server;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.Method;
import java.rmi.MarshalException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.UnmarshalException;
import java.rmi.server.Operation;
import java.rmi.server.RemoteCall;
import java.rmi.server.RemoteObject;
import java.rmi.server.RemoteRef;
import java.security.AccessController;

import sun.rmi.runtime.Log;
import sun.rmi.transport.Connection;
import sun.rmi.transport.LiveRef;
import sun.rmi.transport.StreamRemoteCall;
import sun.security.action.GetBooleanAction;

@SuppressWarnings("deprecation")
public class UnicastRef implements RemoteRef {
	public static final Log clientRefLog = Log.getLog("sun.rmi.client.ref", "transport", Util.logLevel);

	public static final Log clientCallLog = Log.getLog("sun.rmi.client.call", "RMI", AccessController.doPrivileged(new GetBooleanAction("sun.rmi.client.logCalls")));
	private static final long serialVersionUID = 8258372400816541186L;

	protected LiveRef ref;

	public UnicastRef() {
	}

	public UnicastRef(LiveRef liveRef) {
		ref = liveRef;
	}

	public LiveRef getLiveRef() {
		return ref;
	}

	public Object invoke(Remote obj, Method method, Object[] params, long opnum) throws Exception {
		Connection conn = ref.getChannel().newConnection();
		RemoteCall call = null;
		boolean reuse = true;

		boolean alreadyFreed = false;

		try {
			call = new StreamRemoteCall(conn, ref.getObjID(), -1, opnum);

			try {
				ObjectOutput out = call.getOutputStream();
				marshalCustomCallData(out);
				Class<?>[] types = method.getParameterTypes();
				for(int i = 0; i < types.length; i++)
					marshalValue(types[i], params[i], out);
			} catch(IOException e) {
				clientRefLog.log(Log.BRIEF, "IOException marshalling arguments: ", e);
				throw new MarshalException("error marshalling arguments", e);
			}

			call.executeCall();

			try {
				Class<?> rtype = method.getReturnType();
				if(rtype == void.class)
					return null;

				ObjectInput in = call.getInputStream();
				Object returnValue = unmarshalValue(rtype, in);

				alreadyFreed = true;

				ref.getChannel().free(conn, true);

				return returnValue;
			} catch(IOException e) {
				throw new UnmarshalException("error unmarshalling return", e);
			} catch(ClassNotFoundException e) {
				throw new UnmarshalException("error unmarshalling return", e);
			} finally {
				try {
					call.done();
				} catch(IOException e) {
					reuse = false;
				}
			}
		} catch(RuntimeException e) {
			if((call == null) || (((StreamRemoteCall)call).getServerException() != e))
				reuse = false;
			throw e;
		} catch(RemoteException e) {
			reuse = false;
			throw e;
		} catch(Error e) {
			reuse = false;
			throw e;
		} finally {
			if(!alreadyFreed)
				ref.getChannel().free(conn, reuse);
		}
	}

	protected void marshalCustomCallData(ObjectOutput out) throws IOException {
	}

	protected static void marshalValue(Class<?> type, Object value, ObjectOutput out) throws IOException {
		if(type.isPrimitive()) {
			if(type == int.class)
				out.writeInt(((Integer)value).intValue());
			else if(type == boolean.class)
				out.writeBoolean(((Boolean)value).booleanValue());
			else if(type == byte.class)
				out.writeByte(((Byte)value).byteValue());
			else if(type == char.class)
				out.writeChar(((Character)value).charValue());
			else if(type == short.class)
				out.writeShort(((Short)value).shortValue());
			else if(type == long.class)
				out.writeLong(((Long)value).longValue());
			else if(type == float.class)
				out.writeFloat(((Float)value).floatValue());
			else if(type == double.class)
				out.writeDouble(((Double)value).doubleValue());
			else
				throw new Error("Unrecognized primitive type: " + type);
		} else
			out.writeObject(value);
	}

	protected static Object unmarshalValue(Class<?> type, ObjectInput in) throws IOException, ClassNotFoundException {
		if(type.isPrimitive()) {
			if(type == int.class)
				return Integer.valueOf(in.readInt());
			else if(type == boolean.class)
				return Boolean.valueOf(in.readBoolean());
			else if(type == byte.class)
				return Byte.valueOf(in.readByte());
			else if(type == char.class)
				return Character.valueOf(in.readChar());
			else if(type == short.class)
				return Short.valueOf(in.readShort());
			else if(type == long.class)
				return Long.valueOf(in.readLong());
			else if(type == float.class)
				return Float.valueOf(in.readFloat());
			else if(type == double.class)
				return Double.valueOf(in.readDouble());
			else
				throw new Error("Unrecognized primitive type: " + type);
		} else
			return in.readObject();
	}

	public RemoteCall newCall(RemoteObject obj, Operation[] ops, int opnum, long hash) throws RemoteException {
		Connection conn = ref.getChannel().newConnection();
		try {
			RemoteCall call = new StreamRemoteCall(conn, ref.getObjID(), opnum, hash);
			try {
				marshalCustomCallData(call.getOutputStream());
			} catch(IOException e) {
				throw new MarshalException("error marshaling " + "custom call data");
			}
			return call;
		} catch(RemoteException e) {
			ref.getChannel().free(conn, false);
			throw e;
		}
	}

	public void invoke(RemoteCall call) throws Exception {
		try {
			call.executeCall();
		} catch(RemoteException e) {
			free(call, false);
			throw e;
		} catch(Error e) {
			free(call, false);
			throw e;
		} catch(RuntimeException e) {
			free(call, false);
			throw e;
		} catch(Exception e) {
			free(call, true);
			throw e;
		}
	}

	private void free(RemoteCall call, boolean reuse) throws RemoteException {
		Connection conn = ((StreamRemoteCall)call).getConnection();
		ref.getChannel().free(conn, reuse);
	}

	public void done(RemoteCall call) throws RemoteException {
		free(call, true);

		try {
			call.done();
		} catch(IOException e) {
		}
	}

	public String getRefClass(ObjectOutput out) {
		return "UnicastRef";
	}

	public void writeExternal(ObjectOutput out) throws IOException {
		ref.write(out, false);
	}

	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		ref = LiveRef.read(in, false);
	}

	public String remoteToString() {
		return Util.getUnqualifiedName(getClass()) + " [liveRef: " + ref + "]";
	}

	public int remoteHashCode() {
		return ref.hashCode();
	}

	public boolean remoteEquals(RemoteRef sub) {
		if(sub instanceof UnicastRef)
			return ref.remoteEquals(((UnicastRef)sub).ref);
		return false;
	}
}