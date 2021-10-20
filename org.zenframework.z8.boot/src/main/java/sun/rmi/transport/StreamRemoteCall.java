package sun.rmi.transport;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.StreamCorruptedException;
import java.rmi.MarshalException;
import java.rmi.RemoteException;
import java.rmi.UnmarshalException;
import java.rmi.server.ObjID;
import java.rmi.server.RemoteCall;

import sun.misc.ObjectInputFilter;

@SuppressWarnings("deprecation")
public class StreamRemoteCall implements RemoteCall {
	private ConnectionInputStream in = null;
	private ConnectionOutputStream out = null;
	private Connection conn;
	private boolean resultStarted = false;
	private Exception serverException = null;

	public void setObjectInputFilter(ObjectInputFilter filter) {
	}

	public StreamRemoteCall(Connection c) {
		conn = c;
	}

	public StreamRemoteCall(Connection c, ObjID id, int op, long hash) throws RemoteException {
		try {
			conn = c;
			conn.getOutputStream().write(TransportConstants.Call);
			getOutputStream();
			id.write(out);
			out.writeInt(op);
			out.writeLong(hash);
		} catch(IOException e) {
			throw new MarshalException("Error marshaling call header", e);
		}
	}

	public Connection getConnection() {
		return conn;
	}

	public ObjectOutput getOutputStream() throws IOException {
		return getOutputStream(false);
	}

	private ObjectOutput getOutputStream(boolean resultStream) throws IOException {
		return out == null ? out = new ConnectionOutputStream(conn, resultStream) : out;
	}

	public void releaseOutputStream() throws IOException {
		try {
			if(out != null) {
				try {
					out.flush();
				} finally {
					out.done();
				}
			}
			conn.releaseOutputStream();
		} finally {
			out = null;
		}
	}

	public ObjectInput getInputStream() throws IOException {
		return in == null? in = new ConnectionInputStream(conn.getInputStream()) : in;
	}

	public void releaseInputStream() throws IOException {
		try {
			if(in != null) {
				try {
					in.done();
				} catch(RuntimeException e) {
				}

				in.registerRefs();
				in.done(conn);
			}
			conn.releaseInputStream();
		} finally {
			in = null;
		}
	}

	public ObjectOutput getResultStream(boolean success) throws IOException {
		if(resultStarted)
			throw new StreamCorruptedException("result already in progress");
		else
			resultStarted = true;

		DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
		wr.writeByte(TransportConstants.Return);
		getOutputStream(true);

		if(success)
			out.writeByte(TransportConstants.NormalReturn);
		else
			out.writeByte(TransportConstants.ExceptionalReturn);
		out.writeID();
		return out;
	}

	@SuppressWarnings("fallthrough")
	public void executeCall() throws Exception {
		byte returnType;

		DGCAckHandler ackHandler = null;
		try {
			if(out != null)
				ackHandler = out.getDGCAckHandler();

			releaseOutputStream();
			DataInputStream rd = new DataInputStream(conn.getInputStream());
			byte op = rd.readByte();

			if(op != TransportConstants.Return)
				throw new UnmarshalException("Transport return code invalid");

			getInputStream();
			returnType = in.readByte();
			in.readID();
		} catch(UnmarshalException e) {
			throw e;
		} catch(IOException e) {
			throw new UnmarshalException("Error unmarshaling return header", e);
		} finally {
			if(ackHandler != null)
				ackHandler.release();
		}

		switch(returnType) {
		case TransportConstants.NormalReturn:
			break;

		case TransportConstants.ExceptionalReturn:
			Object ex;
			try {
				ex = in.readObject();
			} catch(Exception e) {
				throw new UnmarshalException("Error unmarshaling return", e);
			}

			if(ex instanceof Exception)
				exceptionReceivedFromServer((Exception)ex);
			else
				throw new UnmarshalException("Return type not Exception");
		default:
			throw new UnmarshalException("Return code invalid");
		}
	}

	protected void exceptionReceivedFromServer(Exception ex) throws Exception {
		serverException = ex;

		StackTraceElement[] serverTrace = ex.getStackTrace();
		StackTraceElement[] clientTrace = (new Throwable()).getStackTrace();
		StackTraceElement[] combinedTrace = new StackTraceElement[serverTrace.length + clientTrace.length];
		System.arraycopy(serverTrace, 0, combinedTrace, 0, serverTrace.length);
		System.arraycopy(clientTrace, 0, combinedTrace, serverTrace.length, clientTrace.length);
		ex.setStackTrace(combinedTrace);
		throw ex;
	}

	public Exception getServerException() {
		return serverException;
	}

	public void done() throws IOException {
		releaseInputStream();
	}
}