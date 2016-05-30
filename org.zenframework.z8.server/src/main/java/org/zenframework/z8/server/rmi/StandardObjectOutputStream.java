package org.zenframework.z8.server.rmi;

import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.Externalizable;
import java.io.IOException;
import java.io.NotActiveException;
import java.io.NotSerializableException;
import java.io.ObjectOutput;
import java.io.ObjectStreamConstants;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.UTFDataFormatException;
import java.util.Arrays;

public class StandardObjectOutputStream extends OutputStream implements ObjectOutput, ObjectStreamConstants {

	private final BlockDataOutputStream bout;
	private final HandleTable handles;
	private final ReplaceTable subs;
	private int protocol = PROTOCOL_VERSION_2;
	private int depth;

	private byte[] primVals;

	private final boolean enableOverride;
	private boolean enableReplace;

	private SerialCallbackContext curContext;
	private PutFieldImpl curPut;

	public StandardObjectOutputStream(OutputStream out) throws IOException {
		bout = new BlockDataOutputStream(out);
		handles = new HandleTable(10, (float)3.00);
		subs = new ReplaceTable(10, (float)3.00);
		enableOverride = false;
		writeStreamHeader();
		bout.setBlockDataMode(true);
	}

	protected StandardObjectOutputStream() throws IOException, SecurityException {
		bout = null;
		handles = null;
		subs = null;
		enableOverride = true;
	}

	public void useProtocolVersion(int version) throws IOException {
		if(handles.size() != 0) {
			throw new IllegalStateException("stream non-empty");
		}
		switch(version) {
		case PROTOCOL_VERSION_1:
		case PROTOCOL_VERSION_2:
			protocol = version;
			break;

		default:
			throw new IllegalArgumentException("unknown version: " + version);
		}
	}

	public final void writeObject(Object obj) throws IOException {
		if(enableOverride) {
			writeObjectOverride(obj);
			return;
		}
		try {
			writeObject0(obj, false);
		} catch(IOException ex) {
			if(depth == 0) {
				writeFatalException(ex);
			}
			throw ex;
		}
	}

	protected void writeObjectOverride(Object obj) throws IOException {
	}

	public void writeUnshared(Object obj) throws IOException {
		try {
			writeObject0(obj, true);
		} catch(IOException ex) {
			if(depth == 0) {
				writeFatalException(ex);
			}
			throw ex;
		}
	}

	public void defaultWriteObject() throws IOException {
		SerialCallbackContext ctx = curContext;
		if(ctx == null) {
			throw new NotActiveException("not in call to writeObject");
		}
		Object curObj = ctx.getObj();
		StandardObjectStreamClass curDesc = ctx.getDesc();
		bout.setBlockDataMode(false);
		defaultWriteFields(curObj, curDesc);
		bout.setBlockDataMode(true);
	}

	public StandardObjectOutputStream.PutField putFields() throws IOException {
		if(curPut == null) {
			SerialCallbackContext ctx = curContext;
			if(ctx == null) {
				throw new NotActiveException("not in call to writeObject");
			}
			StandardObjectStreamClass curDesc = ctx.getDesc();
			curPut = new PutFieldImpl(curDesc);
		}
		return curPut;
	}

	public void writeFields() throws IOException {
		if(curPut == null) {
			throw new NotActiveException("no current PutField object");
		}
		bout.setBlockDataMode(false);
		curPut.writeFields();
		bout.setBlockDataMode(true);
	}

	public void reset() throws IOException {
		if(depth != 0) {
			throw new IOException("stream active");
		}
		bout.setBlockDataMode(false);
		bout.writeByte(TC_RESET);
		clear();
		bout.setBlockDataMode(true);
	}

	protected void annotateClass(Class<?> cl) throws IOException {
	}

	protected void annotateProxyClass(Class<?> cl) throws IOException {
	}

	protected Object replaceObject(Object obj) throws IOException {
		return obj;
	}

	protected boolean enableReplaceObject(boolean enable) throws SecurityException {
		if(enable == enableReplace) {
			return enable;
		}
		if(enable) {
			SecurityManager sm = System.getSecurityManager();
			if(sm != null) {
				sm.checkPermission(SUBSTITUTION_PERMISSION);
			}
		}
		enableReplace = enable;
		return !enableReplace;
	}

	protected void writeStreamHeader() throws IOException {
		bout.writeShort(STREAM_MAGIC);
		bout.writeShort(STREAM_VERSION);
	}

	protected void writeClassDescriptor(StandardObjectStreamClass desc) throws IOException {
		desc.writeNonProxy(this);
	}

	public void write(int val) throws IOException {
		bout.write(val);
	}

	public void write(byte[] buf) throws IOException {
		bout.write(buf, 0, buf.length, false);
	}

	public void write(byte[] buf, int off, int len) throws IOException {
		if(buf == null) {
			throw new NullPointerException();
		}
		int endoff = off + len;
		if(off < 0 || len < 0 || endoff > buf.length || endoff < 0) {
			throw new IndexOutOfBoundsException();
		}
		bout.write(buf, off, len, false);
	}

	public void flush() throws IOException {
		bout.flush();
	}

	protected void drain() throws IOException {
		bout.drain();
	}

	public void close() throws IOException {
		flush();
		clear();
		bout.close();
	}

	public void writeBoolean(boolean val) throws IOException {
		bout.writeBoolean(val);
	}

	public void writeByte(int val) throws IOException {
		bout.writeByte(val);
	}

	public void writeShort(int val) throws IOException {
		bout.writeShort(val);
	}

	public void writeChar(int val) throws IOException {
		bout.writeChar(val);
	}

	public void writeInt(int val) throws IOException {
		bout.writeInt(val);
	}

	public void writeLong(long val) throws IOException {
		bout.writeLong(val);
	}

	public void writeFloat(float val) throws IOException {
		bout.writeFloat(val);
	}

	public void writeDouble(double val) throws IOException {
		bout.writeDouble(val);
	}

	public void writeBytes(String str) throws IOException {
		bout.writeBytes(str);
	}

	public void writeChars(String str) throws IOException {
		bout.writeChars(str);
	}

	public void writeUTF(String str) throws IOException {
		bout.writeUTF(str);
	}

	public static abstract class PutField {
		public abstract void put(String name, boolean val);
		public abstract void put(String name, byte val);
		public abstract void put(String name, char val);
		public abstract void put(String name, short val);
		public abstract void put(String name, int val);
		public abstract void put(String name, long val);
		public abstract void put(String name, float val);
		public abstract void put(String name, double val);
		public abstract void put(String name, Object val);

		@Deprecated
		public abstract void write(ObjectOutput out) throws IOException;
	}

	int getProtocolVersion() {
		return protocol;
	}

	void writeTypeString(String str) throws IOException {
		int handle;
		if(str == null) {
			writeNull();
		} else if((handle = handles.lookup(str)) != -1) {
			writeHandle(handle);
		} else {
			writeString(str, false);
		}
	}

	private void clear() {
		subs.clear();
		handles.clear();
	}

	private void writeObject0(Object obj, boolean unshared) throws IOException {
		boolean oldMode = bout.setBlockDataMode(false);
		depth++;
		try {
			int h;
			if((obj = subs.lookup(obj)) == null) {
				writeNull();
				return;
			} else if(!unshared && (h = handles.lookup(obj)) != -1) {
				writeHandle(h);
				return;
			} else if(obj instanceof Class) {
				writeClass((Class<?>)obj, unshared);
				return;
			} else if(obj instanceof StandardObjectStreamClass) {
				writeClassDesc((StandardObjectStreamClass)obj, unshared);
				return;
			}

			Object orig = obj;
			Class<?> cl = obj.getClass();
			StandardObjectStreamClass desc;
			for(;;) {
				Class<?> repCl;
				desc = StandardObjectStreamClass.lookup(cl, true);
				if(!desc.hasWriteReplaceMethod() || (obj = desc.invokeWriteReplace(obj)) == null || (repCl = obj.getClass()) == cl) {
					break;
				}
				cl = repCl;
			}
			if(enableReplace) {
				Object rep = replaceObject(obj);
				if(rep != obj && rep != null) {
					cl = rep.getClass();
					desc = StandardObjectStreamClass.lookup(cl, true);
				}
				obj = rep;
			}

			if(obj != orig) {
				subs.assign(orig, obj);
				if(obj == null) {
					writeNull();
					return;
				} else if(!unshared && (h = handles.lookup(obj)) != -1) {
					writeHandle(h);
					return;
				} else if(obj instanceof Class) {
					writeClass((Class<?>)obj, unshared);
					return;
				} else if(obj instanceof StandardObjectStreamClass) {
					writeClassDesc((StandardObjectStreamClass)obj, unshared);
					return;
				}
			}

			if(obj instanceof String) {
				writeString((String)obj, unshared);
			} else if(cl.isArray()) {
				writeArray(obj, desc, unshared);
			} else if(obj instanceof Enum) {
				writeEnum((Enum<?>)obj, desc, unshared);
			} else if(obj instanceof Serializable) {
				writeOrdinaryObject(obj, desc, unshared);
			} else
				throw new NotSerializableException(cl.getName());
		} finally {
			depth--;
			bout.setBlockDataMode(oldMode);
		}
	}

	private void writeNull() throws IOException {
		bout.writeByte(TC_NULL);
	}

	private void writeHandle(int handle) throws IOException {
		bout.writeByte(TC_REFERENCE);
		bout.writeInt(baseWireHandle + handle);
	}

	private void writeClass(Class<?> cl, boolean unshared) throws IOException {
		bout.writeByte(TC_CLASS);
		writeClassDesc(StandardObjectStreamClass.lookup(cl, true), false);
		handles.assign(unshared ? null : cl);
	}

	private void writeClassDesc(StandardObjectStreamClass desc, boolean unshared) throws IOException {
		int handle;
		if(desc == null) {
			writeNull();
		} else if(!unshared && (handle = handles.lookup(desc)) != -1) {
			writeHandle(handle);
		} else if(desc.isProxy()) {
			writeProxyDesc(desc, unshared);
		} else {
			writeNonProxyDesc(desc, unshared);
		}
	}

	private void writeProxyDesc(StandardObjectStreamClass desc, boolean unshared) throws IOException {
		bout.writeByte(TC_PROXYCLASSDESC);
		handles.assign(unshared ? null : desc);

		Class<?> cl = desc.forClass();
		Class<?>[] ifaces = cl.getInterfaces();
		bout.writeInt(ifaces.length);
		for(int i = 0; i < ifaces.length; i++) {
			bout.writeUTF(ifaces[i].getName());
		}

		bout.setBlockDataMode(true);

		annotateProxyClass(cl);
		bout.setBlockDataMode(false);
		bout.writeByte(TC_ENDBLOCKDATA);

		writeClassDesc(desc.getSuperDesc(), false);
	}

	private void writeNonProxyDesc(StandardObjectStreamClass desc, boolean unshared) throws IOException {
		bout.writeByte(TC_CLASSDESC);
		handles.assign(unshared ? null : desc);

		if(protocol == PROTOCOL_VERSION_1) {
			desc.writeNonProxy(this);
		} else {
			writeClassDescriptor(desc);
		}

		Class<?> cl = desc.forClass();
		bout.setBlockDataMode(true);

		annotateClass(cl);
		bout.setBlockDataMode(false);
		bout.writeByte(TC_ENDBLOCKDATA);

		writeClassDesc(desc.getSuperDesc(), false);
	}

	private void writeString(String str, boolean unshared) throws IOException {
		handles.assign(unshared ? null : str);
		long utflen = bout.getUTFLength(str);
		if(utflen <= 0xFFFF) {
			bout.writeByte(TC_STRING);
			bout.writeUTF(str, utflen);
		} else {
			bout.writeByte(TC_LONGSTRING);
			bout.writeLongUTF(str, utflen);
		}
	}

	private void writeArray(Object array, StandardObjectStreamClass desc, boolean unshared) throws IOException {
		bout.writeByte(TC_ARRAY);
		writeClassDesc(desc, false);
		handles.assign(unshared ? null : array);

		Class<?> ccl = desc.forClass().getComponentType();
		if(ccl.isPrimitive()) {
			if(ccl == Integer.TYPE) {
				int[] ia = (int[])array;
				bout.writeInt(ia.length);
				bout.writeInts(ia, 0, ia.length);
			} else if(ccl == Byte.TYPE) {
				byte[] ba = (byte[])array;
				bout.writeInt(ba.length);
				bout.write(ba, 0, ba.length, true);
			} else if(ccl == Long.TYPE) {
				long[] ja = (long[])array;
				bout.writeInt(ja.length);
				bout.writeLongs(ja, 0, ja.length);
			} else if(ccl == Float.TYPE) {
				float[] fa = (float[])array;
				bout.writeInt(fa.length);
				bout.writeFloats(fa, 0, fa.length);
			} else if(ccl == Double.TYPE) {
				double[] da = (double[])array;
				bout.writeInt(da.length);
				bout.writeDoubles(da, 0, da.length);
			} else if(ccl == Short.TYPE) {
				short[] sa = (short[])array;
				bout.writeInt(sa.length);
				bout.writeShorts(sa, 0, sa.length);
			} else if(ccl == Character.TYPE) {
				char[] ca = (char[])array;
				bout.writeInt(ca.length);
				bout.writeChars(ca, 0, ca.length);
			} else if(ccl == Boolean.TYPE) {
				boolean[] za = (boolean[])array;
				bout.writeInt(za.length);
				bout.writeBooleans(za, 0, za.length);
			} else {
				throw new InternalError();
			}
		} else {
			Object[] objs = (Object[])array;
			int len = objs.length;
			bout.writeInt(len);

			for(int i = 0; i < len; i++)
				writeObject0(objs[i], false);
		}
	}

	private void writeEnum(Enum<?> en, StandardObjectStreamClass desc, boolean unshared) throws IOException {
		bout.writeByte(TC_ENUM);
		StandardObjectStreamClass sdesc = desc.getSuperDesc();
		writeClassDesc((sdesc.forClass() == Enum.class) ? desc : sdesc, false);
		handles.assign(unshared ? null : en);
		writeString(en.name(), false);
	}

	private void writeOrdinaryObject(Object obj, StandardObjectStreamClass desc, boolean unshared) throws IOException {
		desc.checkSerialize();

		bout.writeByte(TC_OBJECT);
		writeClassDesc(desc, false);
		handles.assign(unshared ? null : obj);
		if(desc.isExternalizable() && !desc.isProxy())
			writeExternalData((Externalizable)obj);
		else
			writeSerialData(obj, desc);
	}

	private void writeExternalData(Externalizable obj) throws IOException {
		PutFieldImpl oldPut = curPut;
		curPut = null;

		SerialCallbackContext oldContext = curContext;
		try {
			curContext = null;
			if(protocol == PROTOCOL_VERSION_1) {
				obj.writeExternal(this);
			} else {
				bout.setBlockDataMode(true);
				obj.writeExternal(this);
				bout.setBlockDataMode(false);
				bout.writeByte(TC_ENDBLOCKDATA);
			}
		} finally {
			curContext = oldContext;
		}

		curPut = oldPut;
	}

	private void writeSerialData(Object obj, StandardObjectStreamClass desc) throws IOException {
		StandardObjectStreamClass.ClassDataSlot[] slots = desc.getClassDataLayout();
		for(int i = 0; i < slots.length; i++) {
			StandardObjectStreamClass slotDesc = slots[i].desc;
			if(slotDesc.hasWriteObjectMethod()) {
				PutFieldImpl oldPut = curPut;
				curPut = null;
				SerialCallbackContext oldContext = curContext;

				try {
					curContext = new SerialCallbackContext(obj, slotDesc);
					bout.setBlockDataMode(true);
					slotDesc.invokeWriteObject(obj, this);
					bout.setBlockDataMode(false);
					bout.writeByte(TC_ENDBLOCKDATA);
				} finally {
					curContext.setUsed();
					curContext = oldContext;
				}

				curPut = oldPut;
			} else {
				defaultWriteFields(obj, slotDesc);
			}
		}
	}

	private void defaultWriteFields(Object obj, StandardObjectStreamClass desc) throws IOException {
		Class<?> cl = desc.forClass();
		if(cl != null && obj != null && !cl.isInstance(obj)) {
			throw new ClassCastException();
		}

		desc.checkDefaultSerialize();

		int primDataSize = desc.getPrimDataSize();
		if(primVals == null || primVals.length < primDataSize) {
			primVals = new byte[primDataSize];
		}
		desc.getPrimFieldValues(obj, primVals);
		bout.write(primVals, 0, primDataSize, false);

		StandardObjectStreamField[] fields = desc.getFields(false);
		Object[] objVals = new Object[desc.getNumObjFields()];
		int numPrimFields = fields.length - objVals.length;
		desc.getObjFieldValues(obj, objVals);

		for(int i = 0; i < objVals.length; i++)
			writeObject0(objVals[i], fields[numPrimFields + i].isUnshared());
	}

	private void writeFatalException(IOException ex) throws IOException {
		clear();
		boolean oldMode = bout.setBlockDataMode(false);
		try {
			bout.writeByte(TC_EXCEPTION);
			writeObject0(ex, false);
			clear();
		} finally {
			bout.setBlockDataMode(oldMode);
		}
	}

	private class PutFieldImpl extends PutField {
		private final StandardObjectStreamClass desc;
		private final byte[] primVals;
		private final Object[] objVals;

		PutFieldImpl(StandardObjectStreamClass desc) {
			this.desc = desc;
			primVals = new byte[desc.getPrimDataSize()];
			objVals = new Object[desc.getNumObjFields()];
		}

		public void put(String name, boolean val) {
			Bits.putBoolean(primVals, getFieldOffset(name, Boolean.TYPE), val);
		}

		public void put(String name, byte val) {
			primVals[getFieldOffset(name, Byte.TYPE)] = val;
		}

		public void put(String name, char val) {
			Bits.putChar(primVals, getFieldOffset(name, Character.TYPE), val);
		}

		public void put(String name, short val) {
			Bits.putShort(primVals, getFieldOffset(name, Short.TYPE), val);
		}

		public void put(String name, int val) {
			Bits.putInt(primVals, getFieldOffset(name, Integer.TYPE), val);
		}

		public void put(String name, float val) {
			Bits.putFloat(primVals, getFieldOffset(name, Float.TYPE), val);
		}

		public void put(String name, long val) {
			Bits.putLong(primVals, getFieldOffset(name, Long.TYPE), val);
		}

		public void put(String name, double val) {
			Bits.putDouble(primVals, getFieldOffset(name, Double.TYPE), val);
		}

		public void put(String name, Object val) {
			objVals[getFieldOffset(name, Object.class)] = val;
		}

		public void write(ObjectOutput out) throws IOException {
			if(StandardObjectOutputStream.this != out) {
				throw new IllegalArgumentException("wrong stream");
			}
			out.write(primVals, 0, primVals.length);

			StandardObjectStreamField[] fields = desc.getFields(false);
			int numPrimFields = fields.length - objVals.length;
			for(int i = 0; i < objVals.length; i++) {
				if(fields[numPrimFields + i].isUnshared()) {
					throw new IOException("cannot write unshared object");
				}
				out.writeObject(objVals[i]);
			}
		}

		void writeFields() throws IOException {
			bout.write(primVals, 0, primVals.length, false);

			StandardObjectStreamField[] fields = desc.getFields(false);
			int numPrimFields = fields.length - objVals.length;
			for(int i = 0; i < objVals.length; i++) {
				writeObject0(objVals[i], fields[numPrimFields + i].isUnshared());
			}
		}

		private int getFieldOffset(String name, Class<?> type) {
			StandardObjectStreamField field = desc.getField(name, type);
			if(field == null) {
				throw new IllegalArgumentException("no such field " + name + " with type " + type);
			}
			return field.getOffset();
		}
	}

	private static class BlockDataOutputStream extends OutputStream implements DataOutput {
		private static final int MAX_BLOCK_SIZE = 1024;
		private static final int MAX_HEADER_SIZE = 5;
		private static final int CHAR_BUF_SIZE = 256;

		private final byte[] buf = new byte[MAX_BLOCK_SIZE];
		private final byte[] hbuf = new byte[MAX_HEADER_SIZE];
		private final char[] cbuf = new char[CHAR_BUF_SIZE];

		private boolean blkmode = false;
		private int pos = 0;

		private final OutputStream out;
		private final DataOutputStream dout;

		BlockDataOutputStream(OutputStream out) {
			this.out = out;
			dout = new DataOutputStream(this);
		}

		boolean setBlockDataMode(boolean mode) throws IOException {
			if(blkmode == mode) {
				return blkmode;
			}
			drain();
			blkmode = mode;
			return !blkmode;
		}

		public void write(int b) throws IOException {
			if(pos >= MAX_BLOCK_SIZE) {
				drain();
			}
			buf[pos++] = (byte)b;
		}

		public void write(byte[] b) throws IOException {
			write(b, 0, b.length, false);
		}

		public void write(byte[] b, int off, int len) throws IOException {
			write(b, off, len, false);
		}

		public void flush() throws IOException {
			drain();
			out.flush();
		}

		public void close() throws IOException {
			flush();
			out.close();
		}

		void write(byte[] b, int off, int len, boolean copy) throws IOException {
			if(!(copy || blkmode)) { // write directly
				drain();
				out.write(b, off, len);
				return;
			}

			while(len > 0) {
				if(pos >= MAX_BLOCK_SIZE) {
					drain();
				}
				if(len >= MAX_BLOCK_SIZE && !copy && pos == 0) {
					writeBlockHeader(MAX_BLOCK_SIZE);
					out.write(b, off, MAX_BLOCK_SIZE);
					off += MAX_BLOCK_SIZE;
					len -= MAX_BLOCK_SIZE;
				} else {
					int wlen = Math.min(len, MAX_BLOCK_SIZE - pos);
					System.arraycopy(b, off, buf, pos, wlen);
					pos += wlen;
					off += wlen;
					len -= wlen;
				}
			}
		}

		void drain() throws IOException {
			if(pos == 0) {
				return;
			}
			if(blkmode) {
				writeBlockHeader(pos);
			}
			out.write(buf, 0, pos);
			pos = 0;
		}

		private void writeBlockHeader(int len) throws IOException {
			if(len <= 0xFF) {
				hbuf[0] = TC_BLOCKDATA;
				hbuf[1] = (byte)len;
				out.write(hbuf, 0, 2);
			} else {
				hbuf[0] = TC_BLOCKDATALONG;
				Bits.putInt(hbuf, 1, len);
				out.write(hbuf, 0, 5);
			}
		}

		public void writeBoolean(boolean v) throws IOException {
			if(pos >= MAX_BLOCK_SIZE) {
				drain();
			}
			Bits.putBoolean(buf, pos++, v);
		}

		public void writeByte(int v) throws IOException {
			if(pos >= MAX_BLOCK_SIZE) {
				drain();
			}
			buf[pos++] = (byte)v;
		}

		public void writeChar(int v) throws IOException {
			if(pos + 2 <= MAX_BLOCK_SIZE) {
				Bits.putChar(buf, pos, (char)v);
				pos += 2;
			} else {
				dout.writeChar(v);
			}
		}

		public void writeShort(int v) throws IOException {
			if(pos + 2 <= MAX_BLOCK_SIZE) {
				Bits.putShort(buf, pos, (short)v);
				pos += 2;
			} else {
				dout.writeShort(v);
			}
		}

		public void writeInt(int v) throws IOException {
			if(pos + 4 <= MAX_BLOCK_SIZE) {
				Bits.putInt(buf, pos, v);
				pos += 4;
			} else {
				dout.writeInt(v);
			}
		}

		public void writeFloat(float v) throws IOException {
			if(pos + 4 <= MAX_BLOCK_SIZE) {
				Bits.putFloat(buf, pos, v);
				pos += 4;
			} else {
				dout.writeFloat(v);
			}
		}

		public void writeLong(long v) throws IOException {
			if(pos + 8 <= MAX_BLOCK_SIZE) {
				Bits.putLong(buf, pos, v);
				pos += 8;
			} else {
				dout.writeLong(v);
			}
		}

		public void writeDouble(double v) throws IOException {
			if(pos + 8 <= MAX_BLOCK_SIZE) {
				Bits.putDouble(buf, pos, v);
				pos += 8;
			} else {
				dout.writeDouble(v);
			}
		}

		public void writeBytes(String s) throws IOException {
			int endoff = s.length();
			int cpos = 0;
			int csize = 0;
			for(int off = 0; off < endoff;) {
				if(cpos >= csize) {
					cpos = 0;
					csize = Math.min(endoff - off, CHAR_BUF_SIZE);
					s.getChars(off, off + csize, cbuf, 0);
				}
				if(pos >= MAX_BLOCK_SIZE) {
					drain();
				}
				int n = Math.min(csize - cpos, MAX_BLOCK_SIZE - pos);
				int stop = pos + n;
				while(pos < stop) {
					buf[pos++] = (byte)cbuf[cpos++];
				}
				off += n;
			}
		}

		public void writeChars(String s) throws IOException {
			int endoff = s.length();
			for(int off = 0; off < endoff;) {
				int csize = Math.min(endoff - off, CHAR_BUF_SIZE);
				s.getChars(off, off + csize, cbuf, 0);
				writeChars(cbuf, 0, csize);
				off += csize;
			}
		}

		public void writeUTF(String s) throws IOException {
			writeUTF(s, getUTFLength(s));
		}

		void writeBooleans(boolean[] v, int off, int len) throws IOException {
			int endoff = off + len;
			while(off < endoff) {
				if(pos >= MAX_BLOCK_SIZE) {
					drain();
				}
				int stop = Math.min(endoff, off + (MAX_BLOCK_SIZE - pos));
				while(off < stop) {
					Bits.putBoolean(buf, pos++, v[off++]);
				}
			}
		}

		void writeChars(char[] v, int off, int len) throws IOException {
			int limit = MAX_BLOCK_SIZE - 2;
			int endoff = off + len;
			while(off < endoff) {
				if(pos <= limit) {
					int avail = (MAX_BLOCK_SIZE - pos) >> 1;
					int stop = Math.min(endoff, off + avail);
					while(off < stop) {
						Bits.putChar(buf, pos, v[off++]);
						pos += 2;
					}
				} else {
					dout.writeChar(v[off++]);
				}
			}
		}

		void writeShorts(short[] v, int off, int len) throws IOException {
			int limit = MAX_BLOCK_SIZE - 2;
			int endoff = off + len;
			while(off < endoff) {
				if(pos <= limit) {
					int avail = (MAX_BLOCK_SIZE - pos) >> 1;
					int stop = Math.min(endoff, off + avail);
					while(off < stop) {
						Bits.putShort(buf, pos, v[off++]);
						pos += 2;
					}
				} else {
					dout.writeShort(v[off++]);
				}
			}
		}

		void writeInts(int[] v, int off, int len) throws IOException {
			int limit = MAX_BLOCK_SIZE - 4;
			int endoff = off + len;
			while(off < endoff) {
				if(pos <= limit) {
					int avail = (MAX_BLOCK_SIZE - pos) >> 2;
					int stop = Math.min(endoff, off + avail);
					while(off < stop) {
						Bits.putInt(buf, pos, v[off++]);
						pos += 4;
					}
				} else {
					dout.writeInt(v[off++]);
				}
			}
		}

		void writeFloats(float[] v, int off, int len) throws IOException {
			throw new RuntimeException("native floatsToBytes");
/*
			int limit = MAX_BLOCK_SIZE - 4;
			int endoff = off + len;
			while(off < endoff) {
				if(pos <= limit) {
					int avail = (MAX_BLOCK_SIZE - pos) >> 2;
					int chunklen = Math.min(endoff - off, avail);
					floatsToBytes(v, off, buf, pos, chunklen);
					off += chunklen;
					pos += chunklen << 2;
				} else {
					dout.writeFloat(v[off++]);
				}
			}
*/
		}

		void writeLongs(long[] v, int off, int len) throws IOException {
			int limit = MAX_BLOCK_SIZE - 8;
			int endoff = off + len;
			while(off < endoff) {
				if(pos <= limit) {
					int avail = (MAX_BLOCK_SIZE - pos) >> 3;
					int stop = Math.min(endoff, off + avail);
					while(off < stop) {
						Bits.putLong(buf, pos, v[off++]);
						pos += 8;
					}
				} else {
					dout.writeLong(v[off++]);
				}
			}
		}

		void writeDoubles(double[] v, int off, int len) throws IOException {
			throw new RuntimeException("native doublesToBytes");
/*
			int limit = MAX_BLOCK_SIZE - 8;
			int endoff = off + len;
			while(off < endoff) {
				if(pos <= limit) {
					int avail = (MAX_BLOCK_SIZE - pos) >> 3;
					int chunklen = Math.min(endoff - off, avail);
					doublesToBytes(v, off, buf, pos, chunklen);
					off += chunklen;
					pos += chunklen << 3;
				} else {
					dout.writeDouble(v[off++]);
				}
			} 
*/
		}

		long getUTFLength(String s) {
			int len = s.length();
			long utflen = 0;
			for(int off = 0; off < len;) {
				int csize = Math.min(len - off, CHAR_BUF_SIZE);
				s.getChars(off, off + csize, cbuf, 0);
				for(int cpos = 0; cpos < csize; cpos++) {
					char c = cbuf[cpos];
					if(c >= 0x0001 && c <= 0x007F) {
						utflen++;
					} else if(c > 0x07FF) {
						utflen += 3;
					} else {
						utflen += 2;
					}
				}
				off += csize;
			}
			return utflen;
		}

		void writeUTF(String s, long utflen) throws IOException {
			if(utflen > 0xFFFFL) {
				throw new UTFDataFormatException();
			}
			writeShort((int)utflen);
			if(utflen == (long)s.length()) {
				writeBytes(s);
			} else {
				writeUTFBody(s);
			}
		}

		void writeLongUTF(String s, long utflen) throws IOException {
			writeLong(utflen);
			if(utflen == (long)s.length()) {
				writeBytes(s);
			} else {
				writeUTFBody(s);
			}
		}

		private void writeUTFBody(String s) throws IOException {
			int limit = MAX_BLOCK_SIZE - 3;
			int len = s.length();
			for(int off = 0; off < len;) {
				int csize = Math.min(len - off, CHAR_BUF_SIZE);
				s.getChars(off, off + csize, cbuf, 0);
				for(int cpos = 0; cpos < csize; cpos++) {
					char c = cbuf[cpos];
					if(pos <= limit) {
						if(c <= 0x007F && c != 0) {
							buf[pos++] = (byte)c;
						} else if(c > 0x07FF) {
							buf[pos + 2] = (byte)(0x80 | ((c >> 0) & 0x3F));
							buf[pos + 1] = (byte)(0x80 | ((c >> 6) & 0x3F));
							buf[pos + 0] = (byte)(0xE0 | ((c >> 12) & 0x0F));
							pos += 3;
						} else {
							buf[pos + 1] = (byte)(0x80 | ((c >> 0) & 0x3F));
							buf[pos + 0] = (byte)(0xC0 | ((c >> 6) & 0x1F));
							pos += 2;
						}
					} else { // write one byte at a time to normalize block
						if(c <= 0x007F && c != 0) {
							write(c);
						} else if(c > 0x07FF) {
							write(0xE0 | ((c >> 12) & 0x0F));
							write(0x80 | ((c >> 6) & 0x3F));
							write(0x80 | ((c >> 0) & 0x3F));
						} else {
							write(0xC0 | ((c >> 6) & 0x1F));
							write(0x80 | ((c >> 0) & 0x3F));
						}
					}
				}
				off += csize;
			}
		}
	}

	private static class HandleTable {
		private int size;
		private int threshold;
		private final float loadFactor;
		private int[] spine;
		private int[] next;
		private Object[] objs;

		HandleTable(int initialCapacity, float loadFactor) {
			this.loadFactor = loadFactor;
			spine = new int[initialCapacity];
			next = new int[initialCapacity];
			objs = new Object[initialCapacity];
			threshold = (int)(initialCapacity * loadFactor);
			clear();
		}

		int assign(Object obj) {
			if(size >= next.length) {
				growEntries();
			}
			if(size >= threshold) {
				growSpine();
			}
			insert(obj, size);
			return size++;
		}

		int lookup(Object obj) {
			if(size == 0) {
				return -1;
			}
			int index = hash(obj) % spine.length;
			for(int i = spine[index]; i >= 0; i = next[i]) {
				if(objs[i] == obj) {
					return i;
				}
			}
			return -1;
		}

		void clear() {
			Arrays.fill(spine, -1);
			Arrays.fill(objs, 0, size, null);
			size = 0;
		}

		int size() {
			return size;
		}

		private void insert(Object obj, int handle) {
			int index = hash(obj) % spine.length;
			objs[handle] = obj;
			next[handle] = spine[index];
			spine[index] = handle;
		}

		private void growSpine() {
			spine = new int[(spine.length << 1) + 1];
			threshold = (int)(spine.length * loadFactor);
			Arrays.fill(spine, -1);
			for(int i = 0; i < size; i++) {
				insert(objs[i], i);
			}
		}

		private void growEntries() {
			int newLength = (next.length << 1) + 1;
			int[] newNext = new int[newLength];
			System.arraycopy(next, 0, newNext, 0, size);
			next = newNext;

			Object[] newObjs = new Object[newLength];
			System.arraycopy(objs, 0, newObjs, 0, size);
			objs = newObjs;
		}

		private int hash(Object obj) {
			return System.identityHashCode(obj) & 0x7FFFFFFF;
		}
	}

	private static class ReplaceTable {
		private final HandleTable htab;
		private Object[] reps;

		ReplaceTable(int initialCapacity, float loadFactor) {
			htab = new HandleTable(initialCapacity, loadFactor);
			reps = new Object[initialCapacity];
		}

		void assign(Object obj, Object rep) {
			int index = htab.assign(obj);
			while(index >= reps.length) {
				grow();
			}
			reps[index] = rep;
		}

		Object lookup(Object obj) {
			int index = htab.lookup(obj);
			return (index >= 0) ? reps[index] : obj;
		}

		void clear() {
			Arrays.fill(reps, 0, htab.size(), null);
			htab.clear();
		}

		private void grow() {
			Object[] newReps = new Object[(reps.length << 1) + 1];
			System.arraycopy(reps, 0, newReps, 0, reps.length);
			reps = newReps;
		}
	}

	static class SerialCallbackContext {
		private final Object obj;
		private final StandardObjectStreamClass desc;

		private Thread thread;

		public SerialCallbackContext(Object obj, StandardObjectStreamClass desc) {
			this.obj = obj;
			this.desc = desc;
			this.thread = Thread.currentThread();
		}

		public Object getObj() throws NotActiveException {
			checkAndSetUsed();
			return obj;
		}

		public StandardObjectStreamClass getDesc() {
			return desc;
		}

		public void check() throws NotActiveException {
			if(thread != null && thread != Thread.currentThread()) {
				throw new NotActiveException("expected thread: " + thread + ", but got: " + Thread.currentThread());
			}
		}

		private void checkAndSetUsed() throws NotActiveException {
			if(thread != Thread.currentThread()) {
				throw new NotActiveException("not in readObject invocation or fields already read");
			}
			thread = null;
		}

		public void setUsed() {
			thread = null;
		}
	}

}
