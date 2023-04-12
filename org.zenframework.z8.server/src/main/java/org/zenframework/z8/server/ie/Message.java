package org.zenframework.z8.server.ie;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.zenframework.z8.rmi.ObjectIO;
import org.zenframework.z8.server.base.table.system.Domains;
import org.zenframework.z8.server.base.table.system.MessageQueue;
import org.zenframework.z8.server.config.ServerConfig;
import org.zenframework.z8.server.db.Connection;
import org.zenframework.z8.server.db.ConnectionManager;
import org.zenframework.z8.server.engine.ApplicationServer;
import org.zenframework.z8.server.engine.Database;
import org.zenframework.z8.server.engine.RmiIO;
import org.zenframework.z8.server.engine.RmiSerializable;
import org.zenframework.z8.server.engine.Session;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.request.IRequest;
import org.zenframework.z8.server.request.Request;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.OBJECT;
import org.zenframework.z8.server.security.Domain;
import org.zenframework.z8.server.security.IUser;
import org.zenframework.z8.server.security.User;
import org.zenframework.z8.server.types.binary;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.string;
import org.zenframework.z8.server.utils.IOUtils;
import org.zenframework.z8.server.utils.NumericUtils;

abstract public class Message extends OBJECT implements RmiSerializable, Serializable {

	static private final long serialVersionUID = 3103056587172568573L;

	static private final int MB = 1024 * 1024;
	static private final int MessageSizeThreshold = 10 * MB; // 10MB

	static protected final String FileUrlPrefix = "file:";

	static public class CLASS<T extends Message> extends OBJECT.CLASS<T> {
		public CLASS() {
			this(null);
		}

		public CLASS(IObject container) {
			super(container);
			setJavaClass(Message.class);
		}

		@Override
		public Object newObject(IObject container) {
			return null;
		}
	}

	private guid id = guid.Null;
	private guid sourceId = guid.Null;
	private String name;
	private String description;
	private String sender;
	private String address;

	abstract public void setBytesTransferred(long bytesTransferred);

	abstract protected void write(ObjectOutputStream out) throws IOException;

	abstract protected void read(ObjectInputStream in) throws IOException, ClassNotFoundException;

	abstract public void prepare();

	abstract protected boolean transactive();

	abstract protected boolean apply();

	abstract protected void initDescription();
	
	public Message(IObject container) {
		super(container);
	}

	public guid getId() {
		return id;
	}

	public void setId(guid id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}
	
	@Override
	public void setName(String name) {
		this.name = name;
	}
	
	public String getDescription() {
		return description;
	}
	
	@Override
	public void setDescription(String description) {
		this.description = description;
	}
	
	public guid getSourceId() {
		return sourceId;
	}

	public void setSourceId(guid sourceId) {
		this.sourceId = sourceId;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getSender() {
		return sender;
	}

	public void setSender(String sender) {
		this.sender = sender;
	}

	public boolean isExportToFile() {
		return address.startsWith(FileUrlPrefix);
	}

	protected void beforeImport() {
		z8_beforeImport();
	}

	protected void afterImport() {
		ConnectionManager.get().flush();
		z8_afterImport();
	}

	public void beforeExport() {
		z8_beforeExport();
	}

	public void afterExport() {
		z8_afterExport();
	}

	public binary toBinary() {
		try {
			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(bytes);

			serialize(out);

			IOUtils.closeQuietly(out);
			IOUtils.closeQuietly(bytes);

			if (bytes.size() > MessageSizeThreshold)
				Trace.logEvent("Warning: huge message " + (bytes.size() / MB) + "MB serialized");

			return new binary(bytes.toByteArray());
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void fromBinary(binary binary) {
		try {
			InputStream binaryIn = binary.get();
			ObjectInputStream in = new ObjectInputStream(binaryIn);

			deserialize(in);

			IOUtils.closeQuietly(in);
			IOUtils.closeQuietly(binaryIn);
		} catch(Throwable e) {
			throw new RuntimeException(e);
		}
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

		ByteArrayOutputStream bytes = new ByteArrayOutputStream(256 * NumericUtils.Kilobyte);
		ObjectOutputStream objects = new ObjectOutputStream(bytes);

		RmiIO.writeGuid(objects, id);
		RmiIO.writeGuid(objects, sourceId);
		RmiIO.writeString(objects, sender);
		RmiIO.writeString(objects, address);

		write(objects);

		objects.close();

		RmiIO.writeBytes(out, IOUtils.zip(bytes.toByteArray()));
	}

	@Override
	public void deserialize(ObjectInputStream in) throws IOException, ClassNotFoundException {
		@SuppressWarnings("unused")
		long version = RmiIO.readLong(in);

		ByteArrayInputStream bytes = new ByteArrayInputStream(IOUtils.unzip(RmiIO.readBytes(in)));
		ObjectInputStream objects = new ObjectInputStream(bytes);

		id = RmiIO.readGuid(objects);
		sourceId = RmiIO.readGuid(objects);
		sender = RmiIO.readString(objects);
		address = RmiIO.readString(objects);

		read(objects);

		objects.close();
	}

	public void send() {
		if(address == null || address.isEmpty())
			throw new RuntimeException("Export address is not set");

		if(sender == null || sender.isEmpty())
			setSender(Domains.DefaultDomain.get());
		initDescription();

		ConnectionManager.get().flush();

		if(Domains.newInstance().isOwner(address))
			accept(true);
		else
			MessageQueue.newInstance().add(this);
	}

	public boolean accept() {
		return accept(false);
	}

	protected void exportToFile(Object o) {
		File file = getExportFile();
		file.getParentFile().mkdirs();
		ObjectOutputStream out = null;
		try {
			out = new ObjectOutputStream(new FileOutputStream(file));
			ObjectIO.write(out, o, o);
			MessageQueue messageQueue = MessageQueue.newInstance();
			messageQueue.endProcessing(getSourceId());
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			IOUtils.closeQuietly(out);
		}
	}

	protected File getExportFile() {
		String fileName = Long.toString(java.lang.System.currentTimeMillis(), Character.MAX_RADIX) + '-' + getId() + '.' + getClass().getSimpleName().toLowerCase();
		File file = new File(getAddress().substring(Message.FileUrlPrefix.length()) + File.separator + fileName);
		if (!file.isAbsolute())
			file = new File(ServerConfig.exchangeFolderOut(), file.getPath());
		return file;
	}

	private boolean accept(boolean localSend) {
		if (!localSend)
			ApplicationServer.setRequest(new Request(new Session(ApplicationServer.getSchema())));

		Domains domains = Domains.newInstance();
		Domain acceptorDomain = domains.getDomain(address);

		IUser user = acceptorDomain != null ? acceptorDomain.getSystemUser() : User.system(Database.get(null));

		IRequest currentRequest = ApplicationServer.getRequest();

		IRequest request = new Request(new Session("", user));
		request.setMonitor(currentRequest.getMonitor());
		ApplicationServer.setRequest(request);

		Connection connection = transactive() ? ConnectionManager.get() : null;

		try {
			if(connection != null)
				connection.beginTransaction();

			boolean result = callImport(localSend);

			if(connection != null)
				connection.commit();

			return result;
		} catch(Throwable e) {
			if(connection != null)
				connection.rollback();

			Trace.logError(e);
			throw new RuntimeException(e);
		} finally {
			ApplicationServer.setRequest(currentRequest);
			ConnectionManager.release();
		}
	}

	private boolean callImport(boolean localSend) {
		if(localSend) {
			beforeExport();
			afterExport();
		}

		beforeImport();

		if(!localSend) {
			if(!apply())
				return false;
		}

		afterImport();
		return true;
	}

	public string z8_getAddress() {
		return new string(address);
	}

	public void z8_setAddress(string address) {
		setAddress(address.get());
	}

	public string z8_getSender() {
		return new string(sender);
	}

	public void z8_setSender(string sender) {
		setSender(sender.get());
	}

	public void z8_send() {
		send();
	}

	public void z8_beforeImport() {
	}

	public void z8_afterImport() {
	}

	public void z8_beforeExport() {
	}

	public void z8_afterExport() {
	}

}
