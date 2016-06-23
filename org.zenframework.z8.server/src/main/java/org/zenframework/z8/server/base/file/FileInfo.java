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
import java.util.Map;

import org.apache.commons.fileupload.FileItem;
import org.zenframework.z8.server.engine.RmiIO;
import org.zenframework.z8.server.engine.RmiSerializable;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.parser.JsonArray;
import org.zenframework.z8.server.json.parser.JsonObject;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.OBJECT;
import org.zenframework.z8.server.runtime.RCollection;
import org.zenframework.z8.server.runtime.RLinkedHashMap;
import org.zenframework.z8.server.types.datetime;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.integer;
import org.zenframework.z8.server.types.string;
import org.zenframework.z8.server.utils.IOUtils;

public class FileInfo extends OBJECT implements RmiSerializable, Serializable {

	private static final long serialVersionUID = -2542688680678439014L;

	public string name = new string();
	public string path = new string();
	public datetime time = new datetime();
	public integer size = new integer();
	public guid id = new guid();
	public string instanceId = new string();

	public RLinkedHashMap<string, string> details = new RLinkedHashMap<string, string>();

	public FileItem file;
	public Status status = Status.LOCAL;

	public JsonObject json;

	public static enum Status {

		LOCAL("Files.status.local", ""), REMOTE("Files.status.remote", "remote"), REQUEST_SENT("Files.status.requestSent",
				"requestSent");

		private final String id;
		private final String value;

		private Status(String id, String value) {
			this.id = id;
			this.value = value;
		}

		public String getValue() {
			return value;
		}

		public String getText() {
			return Resources.get(id);
		}

		public static Status getStatus(String value) {
			for (Status status : values()) {
				if (status.value.equals(value))
					return status;
			}
			return LOCAL;
		}

	}

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
		this(file, null, null);
	}

	public FileInfo(FileItem file, String instanceId, String path) {
		super();
		this.instanceId = new string(instanceId);
		this.path = new string(path);
		this.name = new string(file.getName());
		this.file = file;
	}

	public FileInfo(guid id, String name, String instanceId, String path) {
		super();
		this.id = id;
		this.instanceId = new string(instanceId);
		this.path = new string(path);
		this.name = new string(name);
	}

	protected FileInfo(JsonObject json) {
		super();
		set(json);
	}

	public void set(FileInfo fileInfo) {
		this.instanceId = fileInfo.instanceId;
		this.name = fileInfo.name;
		this.path = fileInfo.path;
		this.time = fileInfo.time;
		this.size = fileInfo.size;
		this.id = fileInfo.id;
		this.file = fileInfo.file;
		this.status = fileInfo.status;
		this.details = fileInfo.details;
		this.json = fileInfo.json;
	}

	protected void set(JsonObject json) {
		path = new string(json.getString(json.has(Json.file) ? Json.file : Json.path));
		name = new string(json.has(Json.name) ? json.getString(Json.name) : "");
		time = new datetime(json.has(Json.time) ? json.getString(Json.time) : "");
		size = new integer(json.has(Json.size) ? json.getString(Json.size) : "");
		id = new guid(json.has(Json.id) ? json.getString(Json.id) : "");
		instanceId = new string(json.has(Json.instanceId) ? json.getString(Json.instanceId) : "");
		details.clear();
		if (json.has(Json.details)) {
			for (Map.Entry<String, Object> entry : json.getJsonObject(Json.details).entrySet()) {
				details.put(new string(entry.getKey()), new string(entry.getValue().toString()));
			}
		}

		this.json = json;
	}

	public static List<FileInfo> parseArray(String json) {
		return parse(new JsonArray(json));
	}

	public static List<FileInfo> parse(JsonArray array) {
		List<FileInfo> result = new ArrayList<FileInfo>();

		for (int i = 0; i < array.length(); i++)
			result.add(new FileInfo(array.getJsonObject(i)));

		return result;
	}

	public static String toJson(Collection<FileInfo> fileInfos) {
		JsonArray array = new JsonArray();

		for (FileInfo file : fileInfos)
			array.add(file.toJsonObject());

		return array.toString();
	}

	public JsonObject toJsonObject() {
		if (json == null) {
			json = new JsonObject();
			json.put(Json.name, name);
			json.put(Json.time, time);
			json.put(Json.size, size);
			json.put(Json.path, path);
			json.put(Json.id, id);
			json.put(Json.instanceId, instanceId);
			json.put(Json.details, details);
		}
		return json;
	}

	public static RCollection<FileInfo.CLASS<? extends FileInfo>> z8_parse(string json) {
		return z8_parse(org.zenframework.z8.server.base.json.parser.JsonArray.z8_parse(json));
	}

	public static RCollection<FileInfo.CLASS<? extends FileInfo>> z8_parse(
			org.zenframework.z8.server.base.json.parser.JsonArray.CLASS<? extends org.zenframework.z8.server.base.json.parser.JsonArray> array) {
		RCollection<FileInfo.CLASS<? extends FileInfo>> result = new RCollection<FileInfo.CLASS<? extends FileInfo>>();

		for (int index = 0; index < array.get().z8_length().get(); index++) {
			JsonObject object = array.get().getInternalArray().getJsonObject(index);

			FileInfo.CLASS<FileInfo> fileInfo = new FileInfo.CLASS<FileInfo>();
			fileInfo.get().set(object);

			result.add(fileInfo);
		}
		return result;
	}

	public static FileInfo.CLASS<? extends FileInfo> z8_parse(
			org.zenframework.z8.server.base.json.parser.JsonObject.CLASS<? extends org.zenframework.z8.server.base.json.parser.JsonObject> object) {
		FileInfo.CLASS<FileInfo> fileInfo = new FileInfo.CLASS<FileInfo>();
		fileInfo.get().set(object.get().getInternalObject());
		return fileInfo;
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

	@Override
	public String toString() {
		return toJsonObject().toString();
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
			return file == null ? null : file.getOutputStream();
		} catch (IOException e) {
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
		out.writeLong(serialVersionUID);

		RmiIO.writeString(out, instanceId);
		RmiIO.writeString(out, name);
		RmiIO.writeString(out, path);
		RmiIO.writeDatetime(out, time);
		RmiIO.writeInteger(out, size);
		RmiIO.writeGuid(out, id);
		RmiIO.write(out, null, details);

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

	@SuppressWarnings("unchecked")
	@Override
	public void deserialize(ObjectInputStream in) throws IOException, ClassNotFoundException {
		@SuppressWarnings("unused")
		long version = in.readLong();

		instanceId = new string(RmiIO.readString(in));
		name = new string(RmiIO.readString(in));
		path = new string(RmiIO.readString(in));
		size = RmiIO.readInteger(in);
		time = RmiIO.readDatetime(in);
		id = RmiIO.readGuid(in);
		details = (RLinkedHashMap<string, string>) RmiIO.read(in);

		if (in.readBoolean()) {
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

}
