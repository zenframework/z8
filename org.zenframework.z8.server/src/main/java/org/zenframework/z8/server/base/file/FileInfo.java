package org.zenframework.z8.server.base.file;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.fileupload.FileItem;
import org.zenframework.z8.server.engine.RmiSerializable;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.parser.JsonArray;
import org.zenframework.z8.server.json.parser.JsonObject;
import org.zenframework.z8.server.rmi.RmiIO;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.OBJECT;
import org.zenframework.z8.server.runtime.RCollection;
import org.zenframework.z8.server.types.datetime;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.string;
import org.zenframework.z8.server.utils.IOUtils;

public class FileInfo extends OBJECT implements RmiSerializable, Serializable {
	private static final long serialVersionUID = -2542688680678439014L;

	private static final String PROP_DEFAULT_READ = "z8.serialization.FileInfo.defaultReadObject";
	private static final String PROP_DEFAULT_WRITE = "z8.serialization.FileInfo.defaultWriteObject";

	private static final boolean DEFAULT_READ = Boolean.parseBoolean(System.getProperty(PROP_DEFAULT_READ));
	private static final boolean DEFAULT_WRITE = Boolean.parseBoolean(System.getProperty(PROP_DEFAULT_WRITE));

	public static boolean isDefaultRead() {
		return DEFAULT_READ;
	}

	public static boolean isDefaultWrite() {
		return DEFAULT_WRITE;
	}

	public string name = new string();
	public string path = new string();
	public string type = new string();
	public datetime time = new datetime();
	public guid id = new guid();

	public transient JsonObject json;

	public transient FileItem file;

	public static class CLASS<T extends FileInfo> extends OBJECT.CLASS<T> {
		public CLASS() {
			this(null);
		}

		public CLASS(IObject container) {
			super(container);
			setJavaClass(FileInfo.class);
			setAttribute(Native, FileInfo.class.getCanonicalName());
		}

		@Override
		public Object newObject(IObject container) {
			return new FileInfo(container);
		}
	}

	public FileInfo() {
		super();
	}

	public FileInfo(String id) {
		super();
		this.id = new guid(id);
	}

	public FileInfo(IObject container) {
		super(container);
	}

	public FileInfo(FileItem file) throws IOException {
		this(file, null);
	}

	public FileInfo(FileItem file, String path) throws IOException {
		super();
		this.path = new string(path);
		this.name = new string(file.getName());
		this.file = file;
	}

	protected FileInfo(JsonObject json) {
		super();
		set(json);
	}

	public void set(FileInfo fileInfo) {
		this.path = fileInfo.path;
		this.name = fileInfo.name;
		this.time = fileInfo.time;
		this.type = fileInfo.type;
		this.id = fileInfo.id;

		this.file = fileInfo.file;
	}

	protected void set(JsonObject json) {
		path = new string(json.getString(json.has(Json.file) ? Json.file : Json.path));
		name = new string(json.has(Json.name) ? json.getString(Json.name) : "");
		time = new datetime(json.has(Json.time) ? json.getString(Json.time) : "");
		type = new string(json.has(Json.type) ? json.getString(Json.type) : "");
		id = new guid(json.has(Json.id) ? json.getString(Json.id) : "");

		this.json = json;
	}

	public static List<FileInfo> parseArray(String json) {
		List<FileInfo> result = new ArrayList<FileInfo>();

		if (!json.isEmpty()) {
			JsonArray array = new JsonArray(json);

			for (int i = 0; i < array.length(); i++)
				result.add(parse(array.getJsonObject(i)));
		}
		return result;
	}

	public static String toJson(Collection<FileInfo> fileInfos) {
		JsonArray array = new JsonArray();

		for (FileInfo file : fileInfos)
			array.add(file.toJsonObject());

		return array.toString();
	}

	public static FileInfo parse(JsonObject json) {
		return new FileInfo(json);
	}

	public JsonObject toJsonObject() {
		if (json == null) {
			json = new JsonObject();
			json.put(Json.name, name);
			// json.put(Json.time, time);
			json.put(Json.type, type);
			json.put(Json.path, path);
			json.put(Json.id, id);
		}
		return json;
	}

	public static RCollection<FileInfo.CLASS<? extends FileInfo>> z8_parse(string json) {
		RCollection<FileInfo.CLASS<? extends FileInfo>> result = new RCollection<FileInfo.CLASS<? extends FileInfo>>();

		JsonArray array = new JsonArray(json.get());

		for (int index = 0; index < array.length(); index++) {
			JsonObject object = array.getJsonObject(index);

			FileInfo.CLASS<FileInfo> fileInfo = new FileInfo.CLASS<FileInfo>();
			fileInfo.get().set(object);

			result.add(fileInfo);
		}
		return result;
	}

	static public string z8_toJson(RCollection<FileInfo.CLASS<? extends FileInfo>> classes) {
		return new string(toJson(CLASS.asList(classes)));
	}

	@Override
	public int hashCode() {
		return id != null ? id.hashCode() : 0;
	}

	@Override
	public boolean equals(Object object) {
		return object instanceof FileInfo && id != null && id.equals(((FileInfo) object).id);
	}

	public InputStream getInputStream() {
		try {
			return file == null ? null : file.getInputStream();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public OutputStream getOutputStream() {
		try {
			return file.getOutputStream();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

/*	private void writeObject(ObjectOutputStream outputStream) throws IOException {
		outputStream.defaultWriteObject();
		if (DEFAULT_WRITE)
			return;

		outputStream.writeByte(FILE_INFO_VERSION);
		outputStream.writeBoolean(file != null);

		if (file != null) {
			InputStream inputStream = file.getInputStream();

			try {
				IOUtils.copy(inputStream, outputStream, false);
			} finally {
				IOUtils.closeQuietly(inputStream);
			}
		}
	}
*/
	
    private void writeObject(ObjectOutputStream out)  throws IOException {
    	serialize(out);
    }
    
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
    	deserialize(in);
    }

    public void serialize(ObjectOutputStream out) throws IOException {
		out.writeLong(serialVersionUID);
	
		RmiIO.writeString(out, name);
		RmiIO.writeString(out, path);
		RmiIO.writeString(out, type);
		RmiIO.writeDatetime(out, time);
		RmiIO.writeGuid(out, id);

		out.writeBoolean(file != null);
	
		if (file != null) {
			InputStream in = file.getInputStream();
	
			long size = in.available();
			out.writeLong(size);

			try {
				IOUtils.copyLarge(in, out, size, false);
			} finally {
				IOUtils.closeQuietly(in);
			}
		}
	}

	public void deserialize(ObjectInputStream in) throws IOException, ClassNotFoundException {
		@SuppressWarnings("unused")
		long version = in.readLong();
		
		name = new string(RmiIO.readString(in));
		path = new string(RmiIO.readString(in));
		type = new string(RmiIO.readString(in));
		time = RmiIO.readDatetime(in);
		id = RmiIO.readGuid(in);

		if(in.readBoolean()) {
			long size = in.readLong();
			
			file = FilesFactory.createFileItem(name.get());
			OutputStream out = file.getOutputStream();
	
			try {
				IOUtils.copyLarge(in, out, size, false);
			} finally {
				IOUtils.closeQuietly(out);
			}
		}
	}

/*	private void readObject(ObjectInputStream inputStream) throws IOException, ClassNotFoundException {
		inputStream.defaultReadObject();
		if (DEFAULT_READ)
			return;

		// Read FileInfo version - for future use
		@SuppressWarnings("unused")
		byte version = inputStream.readByte();
		
		if (inputStream.readBoolean()) {
			file = FilesFactory.createFileItem(name.get());

			OutputStream outputStream = file.getOutputStream();

			try {
				IOUtils.copy(inputStream, outputStream, false);
			} finally {
				IOUtils.closeQuietly(outputStream);
			}
		}
	}
*/
}
