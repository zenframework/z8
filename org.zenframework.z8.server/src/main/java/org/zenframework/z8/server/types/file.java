package org.zenframework.z8.server.types;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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
import org.zenframework.z8.server.base.file.FilesFactory;
import org.zenframework.z8.server.base.file.Folders;
import org.zenframework.z8.server.base.table.system.SystemFiles;
import org.zenframework.z8.server.engine.RmiIO;
import org.zenframework.z8.server.engine.RmiSerializable;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.parser.JsonArray;
import org.zenframework.z8.server.json.parser.JsonObject;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.runtime.RCollection;
import org.zenframework.z8.server.runtime.RLinkedHashMap;
import org.zenframework.z8.server.utils.IOUtils;

public class file extends primary implements RmiSerializable, Serializable {

	private static final long serialVersionUID = -2542688680678439014L;
	private static final int partSize = 512 * 1024;

	static public final String EOL = "\r\n";

	public string name = new string();
	public string path = new string();
	public datetime time = new datetime();
	public integer size = new integer();
	public guid id = new guid();
	public string instanceId = new string();

	public RLinkedHashMap<string, string> details = new RLinkedHashMap<string, string>();

	private FileItem value;

	private long offset;
	
	public Status status = Status.LOCAL;

	public JsonObject json;

	static public enum Status {

		LOCAL("Files.status.local", ""), REMOTE("Files.status.remote", "remote"), REQUEST_SENT("Files.status.requestSent", "requestSent");

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

		static public Status getStatus(String value) {
			for(Status status : values()) {
				if(status.value.equals(value))
					return status;
			}
			return LOCAL;
		}

	}

	public file() {
		super();
	}

	public file(guid id) {
		this(id, null, null, null);
	}

	public file(File file) {
		this(null, file.getName(), null, file.getPath(), file.length(), new datetime(file.lastModified()));
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

	public file(guid id, String name, String instanceId, String path, long size, datetime time) {
		super();
		this.id = new guid(id);
		this.instanceId = new string(instanceId);
		this.path = new string(getRelativePath(path));
		this.name = new string(name);
		this.size = new integer(size);
		this.time = new datetime(time);
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
		this.instanceId = file.instanceId;
		this.name = file.name;
		this.path = file.path;
		this.time = file.time;
		this.size = file.size;
		this.id = file.id;
		this.value = file.value;
		this.status = file.status;
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
		time = new datetime(json.has(Json.time) ? json.getString(Json.time) : "");
		size = new integer(json.has(Json.size) ? json.getString(Json.size) : "");
		id = new guid(json.has(Json.id) ? json.getString(Json.id) : "");
		instanceId = new string(json.has(Json.instanceId) ? json.getString(Json.instanceId) : "");

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
			json.put(Json.instanceId, instanceId);
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

		RmiIO.writeString(out, instanceId);
		RmiIO.writeString(out, name);
		RmiIO.writeString(out, path);
		RmiIO.writeDatetime(out, time);
		RmiIO.writeInteger(out, size);
		RmiIO.writeGuid(out, id);

		RmiIO.writeLong(out, offset);
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

		instanceId = new string(RmiIO.readString(in));
		name = new string(RmiIO.readString(in));
		path = new string(RmiIO.readString(in));
		time = RmiIO.readDatetime(in);
		size = RmiIO.readInteger(in);
		id = RmiIO.readGuid(in);

		offset = RmiIO.readLong(in);
		
		if(RmiIO.readBoolean(in)) {
			long size = RmiIO.readLong(in);

			value = FilesFactory.createFileItem(name.get());
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
		
		SystemFiles.get(this);

		if(offset == size.get())
			return null;
		
		byte[] bytes = new byte[partSize];

		InputStream input = getInputStream();
		input.skip(offset);
		int read = IOUtils.read(input, bytes);
		input.close();

		FileItem fileItem = new DiskFileItem(name.get(), null, false, name.get(), 1024 * 1024, null);
		OutputStream output = fileItem.getOutputStream();
		output.write(IOUtils.zip(bytes, 0, read));
		output.close();

		set(fileItem);

		offset += read;
		
		return this;
	}

	public boolean addPartTo(File target) throws IOException {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		IOUtils.copy(getInputStream(), bytes);

		byte[] unzipped = IOUtils.unzip(bytes.toByteArray());
		ByteArrayInputStream input = new ByteArrayInputStream(unzipped);

		target.getParentFile().mkdirs();

		RandomAccessFile output = new RandomAccessFile(target.getPath(), "rw");
		output.seek(offset);
		
		IOUtils.copy(input, output);
		
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

/*package org.zenframework.z8.server.types;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.io.FilenameUtils;
import org.zenframework.z8.server.base.file.Folders;
import org.zenframework.z8.server.runtime.RCollection;
import org.zenframework.z8.server.utils.IOUtils;

public class file extends primary {

	private static final long serialVersionUID = -5250938909367581442L;

	static public final String EOL = "\r\n";

	private File file;

	public file() {}

	static class FileParts {
		String folder;
		String name;
		String extension;

		FileParts(File file) {
			folder = file.getParent();

			String name = file.getName().replace('/', '-').replace('\\', '-').replace(':', '-').replace('\n', ' ');

			int index = name.lastIndexOf('.');
			this.name = index != -1 ? name.substring(0, index) : name;
			this.extension = index != -1 ? name.substring(index) : "";
		}
	}

	static public File getUniqueFileName(File path) {
		return getUniqueFileName(null, path);
	}

	static public File getUniqueFileName(File root, String path) {
		return getUniqueFileName(root, new File(path));
	}

	static public File getUniqueFileName(File root, File path) {
		FileParts parts = new FileParts(path);

		int index = 0;

		while (true) {
			String suffix = index != 0 ? (" (" + index + ")") : "";
			File file = new File(parts.folder, parts.name + suffix + parts.extension);
			File fileToCheck = root != null ? new File(root, file.getPath()) : file;

			if (!fileToCheck.exists())
				return file;

			index++;
		}
	}

	public file(file file) {
		this.file = file.file;
	}

	public file(String path) {
		this(new File(path));
	}

	public file(File path) {
		file = path;
	}

	public File get() {
		return file;
	}

	@Override
	public int hashCode() {
		return file != null ? file.hashCode() : 0;
	}

	@Override
	public boolean equals(Object object) {
		return object instanceof file && file != null && file.equals(((file) object).file);
	}

	public String getFullPath() {
		if (file != null) {
			return file.getPath();
		}
		return "";
	}

	public String getPath() {
		return file != null ? file.getPath() : "";
	}

	public String getRelativePath() {
		String path = getPath();

		if (path.startsWith(Folders.Base.getPath())) {
			return path.substring(Folders.Base.getPath().length() + 1);
		}

		return path;
	}

	public void operatorAssign(file value) {
		file = value.file;
	}

	public void operatorAssign(string pathName) {
		file = new File(pathName.get());

		if (!file.isAbsolute())
			file = new File(new File(Folders.Base, Folders.Files), file.getPath());

		if (!file.isDirectory())
			file.getParentFile().mkdirs();
	}

	public string z8_getPath() {
		return new string(getRelativePath());
	}

	public string z8_getName() {
		return new string(file.getName());
	}

	public string z8_getBaseName() {
		return new string(FilenameUtils.getBaseName(file.getName()));
	}

	public string z8_getExtension() {
		return new string(FilenameUtils.getExtension(file.getName()));
	}

	public bool z8_isDirectory() {
		return new bool(file.isDirectory());
	}

	public RCollection<string> z8_list() {
		String[] files = this.file.list();
		RCollection<string> z8files = new RCollection<string>(files.length, false);
		for (String file : files) {
			z8files.add(new string(file));
		}
		return z8files;
	}

	public RCollection<file> z8_listFiles() {
		File[] files = this.file.listFiles();
		RCollection<file> z8files = new RCollection<file>(files.length, false);
		for (File file : files) {
			z8files.add(new file(file));
		}
		return z8files;
	}

	public string z8_read() {
		return z8_read(encoding.UTF8);
	}

	public string z8_read(encoding charset) {
		try {
			FileInputStream input = new FileInputStream(file);
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			IOUtils.copy(input, output);
			return new string(output.toByteArray(), charset);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void write(String content) {
		write(content, encoding.Default);
	}

	public void write(String content, encoding charset) {
		try {
			if (file == null) {
				File folder = new File(Folders.Base, Folders.Files);
				folder.mkdirs();

				file = File.createTempFile("tmp", ".txt", folder);
				file.deleteOnExit();
			}

			FileOutputStream output = new FileOutputStream(file, true);
			output.write(content.getBytes(charset.toString()));
			output.close();
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void z8_write(string content) {
		write(content.get());
	}

	public void z8_write(string content, encoding charset) {
		write(content.get(), charset);
	}

}
*/