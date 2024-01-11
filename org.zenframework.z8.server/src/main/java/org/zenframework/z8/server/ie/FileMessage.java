package org.zenframework.z8.server.ie;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.apache.commons.io.FileUtils;
import org.zenframework.z8.server.base.file.Folders;
import org.zenframework.z8.server.base.file.InputOnlyFileItem;
import org.zenframework.z8.server.base.table.system.Files;
import org.zenframework.z8.server.base.table.system.TransportQueue;
import org.zenframework.z8.server.engine.ApplicationServer;
import org.zenframework.z8.server.engine.RmiIO;
import org.zenframework.z8.server.engine.Session;
import org.zenframework.z8.server.request.Request;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.types.file;
import org.zenframework.z8.server.types.string;

public class FileMessage extends Message {

	static private final long serialVersionUID = 3103056307172568573L;

	static public final string RecordId = new string("message.recordId");

	static public class CLASS<T extends FileMessage> extends Message.CLASS<T> {
		public CLASS() {
			this(null);
		}

		public CLASS(IObject container) {
			super(container);
			setJavaClass(FileMessage.class);
		}

		@Override
		public Object newObject(IObject container) {
			return new FileMessage(container);
		}
	}

	private file file;

	static public FileMessage newInstance() {
		return new FileMessage.CLASS<FileMessage>(null).get();
	}

	public FileMessage(IObject container) {
		super(container);
	}

	public file getFile() {
		return file;
	}

	public void setFile(file file) {
		this.file = file;
	}

	@Override
	public void setBytesTransferred(long bytesTransferred) {
		file.setOffset(bytesTransferred);
	}

	@Override
	protected void write(ObjectOutputStream out) throws IOException {
		RmiIO.writeLong(out, serialVersionUID);
		out.writeObject(file);
	}

	@Override
	protected void read(ObjectInputStream in) throws IOException, ClassNotFoundException {
		@SuppressWarnings("unused")
		long version = RmiIO.readLong(in);

		file = (file)in.readObject();
	}

	@Override
	public void prepare() {
		if (isExportToFile()) {
			try {
				exportToFile(Files.get(file));
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		} else {
			TransportQueue.newInstance().add(this);
		}
	}

	@Override
	protected boolean transactive() {
		return false;
	}

	@Override
	protected boolean apply() {
		File target = FileUtils.getFile(Folders.Temp, getAddress(), file.getPath());

		long offset = file.offset();

		if(offset == 0) {
			target.getParentFile().mkdirs();
			target.delete();
		} else if(!target.exists() || (offset + file.partLength()) < target.length())
			return false;

		ApplicationServer.setRequest(new Request(new Session(ApplicationServer.getSchema())));

		try {
			if(!file.addPartTo(target))
				return true;
		} catch(IOException e) {
			ApplicationServer.setRequest(null);
			throw new RuntimeException(e);
		}

		try {
			Files files = Files.newInstance();
			file.set(new InputOnlyFileItem(target, file.name.get()));

			if(!files.hasRecord(file.id))
				files.add(file);
			else
				files.updateFile(file);

			return true;
		} finally {
			target.delete();
			ApplicationServer.setRequest(null);
		}
	}

	@Override
	protected void initDescription() {
		setName(file.name.get());
		setDescription(file.json.toString());
	}

	public file z8_getFile() {
		return getFile();
	}

	public void z8_setFile(file file) {
		setFile(file);
	}
}
