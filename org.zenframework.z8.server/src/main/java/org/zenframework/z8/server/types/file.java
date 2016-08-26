package org.zenframework.z8.server.types;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.io.FilenameUtils;
import org.zenframework.z8.server.base.file.Folders;
import org.zenframework.z8.server.base.table.system.Files;
import org.zenframework.z8.server.engine.RmiIO;
import org.zenframework.z8.server.engine.RmiSerializable;
import org.zenframework.z8.server.exceptions.ThreadInterruptedException;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.parser.JsonArray;
import org.zenframework.z8.server.json.parser.JsonObject;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.runtime.RCollection;
import org.zenframework.z8.server.runtime.RLinkedHashMap;
import org.zenframework.z8.server.utils.IOUtils;
import org.zenframework.z8.server.utils.NumericUtils;

public class file extends primary implements RmiSerializable, Serializable {

	private static final long serialVersionUID = -2542688680678439014L;
	static public final String EOL = "\r\n";

	public string name = new string();
	public string path = new string();
	public date time = new date();
	public integer size = new integer();
	public guid id = new guid();
	
	public RLinkedHashMap<string, string> details = new RLinkedHashMap<string, string>();

	private FileItem value;

	private long offset = 0;
	private int partLength = 0;
	
	public JsonObject json;

	public static FileItem createFileItem(string name) {
		return createFileItem(name.get());
	}

	public static FileItem createFileItem(String name) {
		return new DiskFileItem(null, null, false, name, NumericUtils.Megabyte, null);
	}
	
	public file() {
		super();
	}

	public file(guid id) {
		this(id, null, null, null);
	}

	public file(File file) {
		this(null, file.getName(), null, file.getPath(), file.length(), new date(file.lastModified()));
	}

	public file(FileItem file) throws IOException {
		this(file, null, null);
	}

	public file(FileItem value, String instanceId, String path) {
		this(null, value.getName(), instanceId, path, value.getSize(), null);
		this.value = value;
	}

	public file(guid id, String name, String instanceId, String path) {
		this(id, name, instanceId, path, 0, null);
	}

	public file(guid id, String name, String instanceId, String path, long size, date time) {
		super();
		this.id = new guid(id);
		this.path = new string(getRelativePath(path));
		this.name = new string(name);
		this.size = new integer(size);
		this.time = new date(time);
	}

	public file(file file) {
		super();
		set(file);
	}

	protected file(JsonObject json) {
		super();
		set(json);
	}

	public void set(file file) {
		this.name = file.name;
		this.path = file.path;
		this.time = file.time;
		this.size = file.size;
		this.id = file.id;
		this.value = file.value;
		this.details = file.details;
		this.json = file.json;
	}

	public FileItem get() {
		return value;
	}
	
	public void set(FileItem value) {
		this.value = value;
	}

	protected void set(JsonObject json) {
		path = new string(json.getString(json.has(Json.file) ? Json.file : Json.path));
		name = new string(json.has(Json.name) ? json.getString(Json.name) : "");
		size = new integer(json.has(Json.size) ? json.getString(Json.size) : "");
		id = new guid(json.has(Json.id) ? json.getString(Json.id) : "");

		String jsonTime = json.has(Json.time) ? json.getString(Json.time) : "";
		try {
			time = jsonTime.indexOf('/') == -1 ? new date(jsonTime) : new date(jsonTime, "D/M/y H:m:s");
		} catch(NumberFormatException e) {
			time = new date(); 
		}

		this.json = json;
	}

	static public Collection<file> parse(String json) {
		List<file> result = new ArrayList<file>();

		JsonArray array = new JsonArray(json);

		for(int i = 0; i < array.length(); i++)
			result.add(new file(array.getJsonObject(i)));

		return result;
	}

	static public String toJson(Collection<file> files) {
		JsonArray array = new JsonArray();

		for(file file : files)
			array.add(file.toJsonObject());

		return array.toString();
	}

	public long offset() {
		return offset;
	}
	
	public int partLength() {
		return partLength;
	}

	public void setOffset(long offset) {
		this.offset = offset;
	}

	public JsonObject toJsonObject() {
		if(json == null) {
			json = new JsonObject();
			json.put(Json.name, name);
			json.put(Json.time, time);
			json.put(Json.size, size);
			json.put(Json.path, path);
			json.put(Json.id, id);
			json.put(Json.details, details);
		}
		return json;
	}

	@Override
	public int hashCode() {
		return id != null ? id.hashCode() : 0;
	}

	@Override
	public boolean equals(Object object) {
		return object instanceof file && id != null && id.equals(((file)object).id);
	}

	@Override
	public String toString() {
		return toJsonObject().toString();
	}

	public InputStream getInputStream() {
		try {
			return value == null ? null : value.getInputStream();
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}

	public OutputStream getOutputStream() {
		try {
			return value == null ? null  : value.getOutputStream();
		} catch(IOException e) {
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

		RmiIO.writeString(out, name);
		RmiIO.writeString(out, path);
		RmiIO.writeDate(out, time);
		RmiIO.writeInteger(out, size);
		RmiIO.writeGuid(out, id);

		RmiIO.writeLong(out, offset);
		RmiIO.writeInt(out, partLength);
		RmiIO.writeBoolean(out, value != null);

		if(value != null) {
			InputStream in = value.getInputStream();

			long size = in.available();
			RmiIO.writeLong(out, size);

			try {
				IOUtils.copyLarge(in, out, size, false);
			} finally {
				IOUtils.closeQuietly(in);
			}
		}
	}

	@Override
	public void deserialize(ObjectInputStream in) throws IOException, ClassNotFoundException {
		@SuppressWarnings("unused")
		long version = RmiIO.readLong(in);

		name = new string(RmiIO.readString(in));
		path = new string(RmiIO.readString(in));
		time = RmiIO.readDate(in);
		size = RmiIO.readInteger(in);
		id = RmiIO.readGuid(in);

		offset = RmiIO.readLong(in);
		partLength = RmiIO.readInt(in);
		
		if(RmiIO.readBoolean(in)) {
			long size = RmiIO.readLong(in);

			value = createFileItem(name);
			OutputStream out = value.getOutputStream();

			try {
				IOUtils.copyLarge(in, out, size, false);
			} finally {
				IOUtils.closeQuietly(out);
			}
		}
	}

	public void write(String content) {
		write(content, encoding.Default);
	}

	private File getTempFile() throws IOException {
		File folder = new File(Folders.Base, Folders.Files);
		folder.mkdirs();

		return File.createTempFile("tmp", ".txt", folder);
	}

	public void write(String content, encoding charset) {
		try {
			File file = new File(path.get());
			
			if(path.isEmpty()) {
				file = getTempFile();
				path.set(getRelativePath(file.getPath()));
			} else if(!file.isAbsolute())
				file = new File(Folders.Base, path.get());

			OutputStream output = new FileOutputStream(file, true);
			output.write(content.getBytes(charset.toString()));
			IOUtils.closeQuietly(output);
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}

	static public String getRelativePath(File file) {
		return getRelativePath(file.getPath());
	}

	static public String getRelativePath(String path) {
		if (path != null && path.startsWith(Folders.Base.getPath()))
			return path.substring(Folders.Base.getPath().length() + 1);

		return path;
	}

	public file nextPart() throws IOException {
		if(Thread.interrupted())
			throw new ThreadInterruptedException();
		
		Files.get(this);

		offset += partLength;
		
		if(offset == size.get())
			return null;
		
		byte[] bytes = new byte[512 * NumericUtils.Kilobyte];
		partLength = read(offset, bytes);

		InputStream input = new ByteArrayInputStream(bytes, 0, partLength);
		
		set(createFileItem(name));
		
		IOUtils.zip(input, getOutputStream());

		return this;
	}

	private int read(long offset, byte[] bytes) throws IOException {
		InputStream input = null;
		try {
			input = getInputStream();
			input.skip(offset);
			return IOUtils.read(input, bytes);
		} finally {
			IOUtils.closeQuietly(input);
		}
	}
	
	public boolean addPartTo(File target) throws IOException {
		RandomAccessFile output = new RandomAccessFile(target.getPath(), "rw");
		output.seek(offset);
		
		IOUtils.unzip(getInputStream(), output);
		
		return target.length() == size.get();
	}
	
	public void operatorAssign(string path) {
		File file = new File(path.get());

		if(!file.isAbsolute())
			file = new File(new File(Folders.Base, Folders.Files), file.getPath());

		if(!file.isDirectory())
			file.getParentFile().mkdirs();

		this.path.set(file.getPath());
	}

	static public RCollection<file> z8_parse(string json) {
		return new RCollection<file>(parse(json.get()));
	}

	static public string z8_toJson(RCollection<file> classes) {
		return new string(toJson(classes));
	}

	public void z8_write(string content) {
		write(content.get());
	}

	public void z8_write(string content, encoding charset) {
		write(content.get(), charset);
	}
	
	static public string z8_name(string name) {
		return new string(FilenameUtils.getName(name.get()));
	}

	static public string z8_baseName(string name) {
		return new string(FilenameUtils.getBaseName(name.get()));
	}

	static public string z8_extension(string name) {
		return new string(FilenameUtils.getExtension(name.get()));
	}
	
	public bool z8_isDirectory() {
		return z8_isDirectory(path);
	}

	static public bool z8_isDirectory(string path) {
		return new bool(new File(path.get()).isDirectory());
	}

	public RCollection<file> z8_listFiles() {
		return z8_listFiles(path);
	}
	
	static public RCollection<file> z8_listFiles(string path) {
		File[] files = new File(path.get()).listFiles();
		
		RCollection<file> result = new RCollection<file>();
		
		for (File file : files)
			result.add(new file(file));

		return result;
	}
}
