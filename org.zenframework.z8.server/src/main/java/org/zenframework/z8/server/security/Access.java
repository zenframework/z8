package org.zenframework.z8.server.security;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.zenframework.z8.server.engine.RmiIO;

public class Access implements IAccess {
	private static final long serialVersionUID = -4405245083252310301L;

	private boolean read = false;
	private boolean write = false;
	private boolean create = false;
	private boolean copy = false;
	private boolean destroy = false;
	private boolean execute = false;

	static public IAccess administrator() {
		Access access = new Access();
		access.read = access.write = access.create = access.copy = access.destroy = access.execute = true;
		return access;
	}

	static public IAccess user() {
		Access access = new Access();
		access.read = true;
		access.write = access.create = access.copy = access.destroy = access.execute = false;
		return access;
	}

	static public IAccess guest() {
		Access access = new Access();
		access.read = access.write = access.create = access.copy = access.destroy = access.execute = false;
		return access;
	}

	public Access() {
	}

	public boolean read() {
		return read;
	}

	public void setRead(boolean read) {
		this.read = read;
	}

	public boolean write() {
		return write;
	}

	public void setWrite(boolean write) {
		this.write = write;
	}

	public boolean create() {
		return create;
	}

	public void setCreate(boolean create) {
		this.create = create;
	}

	public boolean copy() {
		return copy;
	}

	public void setCopy(boolean copy) {
		this.copy = copy;
	}

	public boolean destroy() {
		return destroy;
	}

	public void setDestroy(boolean destroy) {
		this.destroy = destroy;
	}

	public boolean execute() {
		return execute;
	}

	public void setExecute(boolean execute) {
		this.execute = execute;
	}

	public IAccess or(IAccess access) {
		Access result = new Access();
		result.read = read || access.read();
		result.write = write || access.write();
		result.create = create || access.create();
		result.copy = copy || access.copy();
		result.destroy = destroy || access.destroy();
		result.execute = execute || access.execute();
		return result;
	}

	public IAccess and(IAccess access) {
		Access result = new Access();
		result.read = read && access.read();
		result.write = write && access.write();
		result.create = create && access.create();
		result.copy = copy && access.copy();
		result.destroy = destroy && access.destroy();
		result.execute = execute && access.execute();
		return result;
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

		RmiIO.writeBoolean(out, read);
		RmiIO.writeBoolean(out, write);
		RmiIO.writeBoolean(out, create);
		RmiIO.writeBoolean(out, copy);
		RmiIO.writeBoolean(out, destroy);
		RmiIO.writeBoolean(out, execute);
	}

	@Override
	public void deserialize(ObjectInputStream in) throws IOException, ClassNotFoundException {
		@SuppressWarnings("unused")
		long version = RmiIO.readLong(in);

		read = RmiIO.readBoolean(in);
		write = RmiIO.readBoolean(in);
		create = RmiIO.readBoolean(in);
		copy = RmiIO.readBoolean(in);
		destroy = RmiIO.readBoolean(in);
		execute = RmiIO.readBoolean(in);
	}

	@Override
	public String toString() {
		String result = read ? "read" : "";
		result += write ? (result.isEmpty() ? "" : "/") + "write" : "";
		result += create ? (result.isEmpty() ? "" : "/") + "create": "";
		result += copy ? (result.isEmpty() ? "" : "/") + "copy" : "";
		result += destroy ? (result.isEmpty() ? "" : "/") + "destroy" : "";
		result += execute ? (result.isEmpty() ? "" : "/") + "execute" : "";
		return result;
	}
}
