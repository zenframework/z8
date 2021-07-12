package sun.rmi.server;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.rmi.MarshalException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.ServerError;
import java.rmi.ServerException;
import java.rmi.UnmarshalException;
import java.rmi.server.ExportException;
import java.rmi.server.RemoteCall;
import java.rmi.server.RemoteRef;
import java.rmi.server.RemoteStub;
import java.rmi.server.ServerNotActiveException;
import java.rmi.server.ServerRef;
import java.rmi.server.Skeleton;
import java.rmi.server.SkeletonNotFoundException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import sun.misc.ObjectInputFilter;
import sun.rmi.runtime.Log;
import sun.rmi.transport.LiveRef;
import sun.rmi.transport.Target;
import sun.rmi.transport.tcp.TCPTransport;
import sun.security.action.GetBooleanAction;

@SuppressWarnings("deprecation")
public class UnicastServerRef extends UnicastRef implements ServerRef, Dispatcher {
	public static final boolean logCalls = AccessController.doPrivileged(new GetBooleanAction("java.rmi.server.logCalls"));

	public static final Log callLog = Log.getLog("sun.rmi.server.call", "RMI", logCalls);

	private static final long serialVersionUID = -7384275867073752268L;

	private boolean forceStubUse = false;

	private static final boolean suppressStackTraces = AccessController.doPrivileged(new GetBooleanAction("sun.rmi.server.suppressStackTraces"));

	private transient Skeleton skel;
	private transient Map<Long, Method> hashToMethod_Map = null;

	private static final WeakClassHashMap<Map<Long, Method>> hashToMethod_Maps = new HashToMethod_Maps();
	private static final Map<Class<?>, ?> withoutSkeletons = Collections.synchronizedMap(new WeakHashMap<Class<?>, Void>());

	public UnicastServerRef(ObjectInputFilter filter) {
	}

	public UnicastServerRef(LiveRef ref, ObjectInputFilter filter) {
		super(ref);
	}

	public UnicastServerRef() {
	}

	public UnicastServerRef(LiveRef ref) {
		super(ref);
	}

	public UnicastServerRef(int port) {
		super(new LiveRef(port));
	}

	public UnicastServerRef(boolean forceStubUse) {
		this(0);
		this.forceStubUse = forceStubUse;
	}

	public RemoteStub exportObject(Remote impl, Object data) throws RemoteException {
		forceStubUse = true;
		return (RemoteStub)exportObject(impl, data, false);
	}

	public Remote exportObject(Remote impl, Object data, boolean permanent) throws RemoteException {
		Class<?> implClass = impl.getClass();
		Remote stub;

		try {
			stub = Util.createProxy(implClass, getClientRef(), forceStubUse);
		} catch(IllegalArgumentException e) {
			throw new ExportException("remote object implements illegal remote interface", e);
		}
		if(stub instanceof RemoteStub) {
			setSkeleton(impl);
		}

		Target target = new Target(impl, this, stub, ref.getObjID(), permanent);
		ref.exportObject(target);
		hashToMethod_Map = hashToMethod_Maps.get(implClass);
		return stub;
	}

	public String getClientHost() throws ServerNotActiveException {
		return TCPTransport.getClientHost();
	}

	public void setSkeleton(Remote impl) throws RemoteException {
		if(!withoutSkeletons.containsKey(impl.getClass())) {
			try {
				skel = Util.createSkeleton(impl);
			} catch(SkeletonNotFoundException e) {
				withoutSkeletons.put(impl.getClass(), null);
			}
		}
	}

	public void dispatch(Remote obj, RemoteCall call) throws IOException {
		int num;
		long op;

		try {
			ObjectInput in;
			try {
				in = call.getInputStream();
				num = in.readInt();
				if(num >= 0) {
					if(skel != null) {
						oldDispatch(obj, call, num);
						return;
					}

					throw new UnmarshalException("skeleton class not found but required " + "for client version");
				}
				op = in.readLong();
			} catch(Exception readEx) {
				throw new UnmarshalException("error unmarshalling call header", readEx);
			}

			MarshalInputStream marshalStream = (MarshalInputStream)in;
			marshalStream.skipDefaultResolveClass();

			Method method = hashToMethod_Map.get(op);
			if(method == null)
				throw new UnmarshalException("unrecognized method hash: " + "method not supported by remote object");

			Class<?>[] types = method.getParameterTypes();
			Object[] params = new Object[types.length];

			try {
				unmarshalCustomCallData(in);
				for(int i = 0; i < types.length; i++)
					params[i] = unmarshalValue(types[i], in);
			} catch(java.io.IOException e) {
				throw new UnmarshalException("error unmarshalling arguments", e);
			} catch(ClassNotFoundException e) {
				throw new UnmarshalException("error unmarshalling arguments", e);
			} finally {
				call.releaseInputStream();
			}

			Object result;
			try {
				result = method.invoke(obj, params);
			} catch(InvocationTargetException e) {
				throw e.getTargetException();
			}

			try {
				ObjectOutput out = call.getResultStream(true);
				Class<?> rtype = method.getReturnType();
				if(rtype != void.class)
					marshalValue(rtype, result, out);
			} catch(IOException ex) {
				throw new MarshalException("error marshalling return", ex);
			}
		} catch(Throwable e) {
			ObjectOutput out = call.getResultStream(false);
			if(e instanceof Error)
				e = new ServerError("Error occurred in server thread", (Error)e);
			else if(e instanceof RemoteException)
				e = new ServerException("RemoteException occurred in server thread", (Exception)e);

			if(suppressStackTraces)
				clearStackTraces(e);

			out.writeObject(e);
		} finally {
			call.releaseInputStream();
			call.releaseOutputStream();
		}
	}

	protected void unmarshalCustomCallData(ObjectInput in) throws IOException, ClassNotFoundException {
	}

	public void oldDispatch(Remote obj, RemoteCall call, int op) throws IOException {
		long hash;

		try {
			ObjectInput in;
			try {
				in = call.getInputStream();
				try {
					Class<?> clazz = Class.forName("sun.rmi.transport.DGCImpl_Skel");
					if(clazz.isAssignableFrom(skel.getClass()))
						((MarshalInputStream)in).useCodebaseOnly();
				} catch(ClassNotFoundException ignore) {
				}
				hash = in.readLong();
			} catch(Exception readEx) {
				throw new UnmarshalException("error unmarshalling call header", readEx);
			}

			unmarshalCustomCallData(in);
			skel.dispatch(obj, call, op, hash);

		} catch(Throwable e) {
			ObjectOutput out = call.getResultStream(false);
			if(e instanceof Error)
				e = new ServerError("Error occurred in server thread", (Error)e);
			else if(e instanceof RemoteException)
				e = new ServerException("RemoteException occurred in server thread", (Exception)e);

			if(suppressStackTraces)
				clearStackTraces(e);

			out.writeObject(e);
		} finally {
			call.releaseInputStream();
			call.releaseOutputStream();
		}
	}

	public static void clearStackTraces(Throwable t) {
		StackTraceElement[] empty = new StackTraceElement[0];
		while(t != null) {
			t.setStackTrace(empty);
			t = t.getCause();
		}
	}

	public String getRefClass(ObjectOutput out) {
		return "UnicastServerRef";
	}

	protected RemoteRef getClientRef() {
		return new UnicastRef(ref);
	}

	public void writeExternal(ObjectOutput out) throws IOException {
	}

	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		ref = null;
		skel = null;
	}

	private static class HashToMethod_Maps extends WeakClassHashMap<Map<Long, Method>> {
		HashToMethod_Maps() {
		}

		protected Map<Long, Method> computeValue(Class<?> remoteClass) {
			Map<Long, Method> map = new HashMap<>();
			for(Class<?> cl = remoteClass; cl != null; cl = cl.getSuperclass()) {
				for(Class<?> intf : cl.getInterfaces()) {
					if(Remote.class.isAssignableFrom(intf)) {
						for(Method method : intf.getMethods()) {
							final Method m = method;
							AccessController.doPrivileged(new PrivilegedAction<Void>() {
								public Void run() {
									m.setAccessible(true);
									return null;
								}
							});
							map.put(Util.computeMethodHash(m), m);
						}
					}
				}
			}
			return map;
		}
	}
}