package org.zenframework.z8.server.base.table.system;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Collection;

import org.zenframework.z8.server.base.file.Folders;
import org.zenframework.z8.server.base.file.InputOnlyFileItem;
import org.zenframework.z8.server.base.file.InputStreamFileItem;
import org.zenframework.z8.server.base.table.Table;
import org.zenframework.z8.server.base.table.value.BinaryField;
import org.zenframework.z8.server.base.table.value.DatetimeField;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.base.table.value.IntegerField;
import org.zenframework.z8.server.base.table.value.StringField;
import org.zenframework.z8.server.config.ServerConfig;
import org.zenframework.z8.server.db.ConnectionManager;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.types.binary;
import org.zenframework.z8.server.types.date;
import org.zenframework.z8.server.types.file;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.integer;
import org.zenframework.z8.server.types.string;
import org.zenframework.z8.server.utils.IOUtils;

public class Files extends Table {
	public static final String TableName = "SystemFiles";
	public static final String Storage = "storage/";

	public static final long Location_DB = file.DB.get();
	public static final long Location_Storage = file.Storage.get();

	static public class fieldNames {
		public final static String Name = "Name";
		public final static String File = "File";
		public final static String Path = "Path";
		public final static String Size = "Size";
		public final static String Time = "Time";
		public final static String LastModified = "Last modified";
		public final static String Location = "Location";
}

	static public class strings {
		public final static String Title = "Files.title";
		public final static String Name = "Files.name";
		public final static String Path = "Files.path";
		public final static String Size = "Files.size";
		public final static String Time = "Files.time";
		public final static String LastModified = "Files.lastModified";
		public final static String Location = "Files.location";
	}

	static public class displayNames {
		public final static String Title = Resources.get(strings.Title);
		public final static String Name = Resources.get(strings.Name);
		public final static String Path = Resources.get(strings.Path);
		public final static String Size = Resources.get(strings.Size);
		public final static String Time = Resources.get(strings.Time);
		public final static String LastModified = Resources.get(strings.LastModified);
		public final static String Location = Resources.get(strings.Location);
	}

	public static class CLASS<T extends Files> extends Table.CLASS<T> {
		public CLASS() {
			this(null);
		}

		public CLASS(IObject container) {
			super(container);
			setJavaClass(Files.class);
			setName(TableName);
			setDisplayName(displayNames.Title);
		}

		@Override
		public Object newObject(IObject container) {
			return new Files(container);
		}
	}

	public final StringField.CLASS<? extends StringField> name = new StringField.CLASS<StringField>(this);
	public final StringField.CLASS<StringField> path = new StringField.CLASS<StringField>(this);
	public final BinaryField.CLASS<BinaryField> data = new BinaryField.CLASS<BinaryField>(this);
	public final IntegerField.CLASS<IntegerField> size = new IntegerField.CLASS<IntegerField>(this);
	public final DatetimeField.CLASS<DatetimeField> time = new DatetimeField.CLASS<DatetimeField>(this);
	public final DatetimeField.CLASS<DatetimeField> lastModified = new DatetimeField.CLASS<DatetimeField>(this);
	public final IntegerField.CLASS<IntegerField> location = new IntegerField.CLASS<IntegerField>(this);

	static public Files newInstance() {
		return new Files.CLASS<Files>().get();
	}

	public Files(IObject container) {
		super(container);
	}

	@Override
	public void initMembers() {
		super.initMembers();

		objects.add(name);
		objects.add(data);
		objects.add(path);
		objects.add(size);
		objects.add(time);
		objects.add(lastModified);
		objects.add(location);
	}

	@Override
	public void constructor2() {
		super.constructor2();

		name.setName(fieldNames.Name);
		name.setIndex("name");
		name.setDisplayName(displayNames.Name);
		name.get().length = new integer(512);

		data.setName(fieldNames.File);
		data.setIndex("data");

		path.setName(fieldNames.Path);
		path.setIndex("path");
		path.setDisplayName(displayNames.Path);
		path.get().length = new integer(512);

		size.setName(fieldNames.Size);
		size.setIndex("size");

		time.setName(fieldNames.Time);
		time.setIndex("time");

		lastModified.setName(fieldNames.LastModified);
		lastModified.setIndex("lastModified");

		location.setName(fieldNames.Location);
		location.setIndex("location");
	}

	@Override
	public void onNew() {
		super.onNew();

		date now = new date();
		time.get().set(now);
		lastModified.get().set(now);
	}

	public file add(file file) {
		return change(file, true);
	}
	
	public file add(guid id, string path, string name, integer size, binary data) {
		String pathStr = file.getNormalizedPath(path.get());
		if(!pathStr.startsWith(Storage)) {
			//throw new RuntimeException("Path \"" + pathStr + "\" doesn't belong to storage");
			if(pathStr.startsWith("/"))
				pathStr = pathStr.substring(1);
			pathStr = Storage + pathStr;
		}
		
		file forAdd = new file();
		forAdd.id = id;
		forAdd.name = name;
		forAdd.path = path;
		forAdd.size = size;
		forAdd.set(new InputStreamFileItem(data.get(), name.get()));

		return change(forAdd, true);
	}

	public file updateFile(file file) {
		return change(file, false);
	}

	private file change(file file, boolean create) {
		InputStream input = file.getInputStream();

		try {
			name.get().set(file.name);
			path.get().set(file.path);
			size.get().set(file.size);
			time.get().set(file.time);
			lastModified.get().set(file.time);

			if(input != null) {
				if (ServerConfig.filesSaveOnDisk()) {
					location.get().set(Location_Storage);
					putOnDisk(file, input);
				} else {
					data.get().set(input);
				}
			} else if(create) {
				throw new RuntimeException("File \"" + file.path + "\" has no data and can't be created");
			}

			if(create) {
				if(file.id == null || file.id.equals(guid.Null))
					file.id = guid.create();
				create(file.id);
			} else {
				if(file.id == null || file.id.equals(guid.Null))
					throw new RuntimeException("File \"" + file.path + "\" has no id");
				update(file.id);
			}

			ConnectionManager.get().flush();
			return file;
		} finally {
			IOUtils.closeQuietly(input);
		}
	}

	private void putOnDisk(file file, InputStream input) {
		try {
			File path = getFullStoragePath(file);
			path.getParentFile().mkdirs();
			java.nio.file.Files.copy(input, path.toPath(), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	private void setSize(guid id, long newSize) {
		size.get().set(newSize);
		update(id);
	}

	private static file readFile(file f) throws IOException {
		guid fileId = f.id;
		
		Files table = newInstance();
		
		Field location = table.location.get();
		Field size = table.size.get();
		Collection<Field> fields = Arrays.asList(location, size);
		
		if(fileId != null && !fileId.isNull() && table.readRecord(fileId, fields)) {
			f.size = new integer(size.integer().get());
			f.location = location.integer(); 
		} else {
			f.location = file.Storage; 
		}
		
		return f;
	}
	
	public static InputStream getInputStream(file file) throws IOException {
		guid fileId = file.id;

		Files table = newInstance();

		Field data = table.data.get();
		Field size = table.size.get();
		Collection<Field> fields = Arrays.asList(data, size);

		if(!fileId.isNull() && table.readRecord(fileId, fields)) {
			file.size = new integer(size.integer().get());
			return data.binary().get();
		}

		return null;
	}

	public static file get(guid fileId) {
		Files table = newInstance();

		Field path = table.path.get();
		Collection<Field> fields = Arrays.asList(path);

		if(!fileId.isNull() && table.readRecord(fileId, fields))
			return new file(fileId, path.string().get());

		return null;
	}
	
	public static file get(file f) throws IOException {
		return get(f, true);
	}

	public static file get(file f, boolean strictMode) throws IOException {
		File value = getFullStoragePath(f);
		boolean exists = value.exists();

		readFile(f);

		if(f.location.get() == Location_Storage) {
			if(!exists) {
				if(!strictMode)
					return f;
				throw new RuntimeException("Files.java:get(file file) file location is storage and file doesn't exist, path: " + value.getAbsolutePath());
			}
			return f.set(new InputOnlyFileItem(value, f.name.get()));
		}

		// location == Location_DB

		long fileSize = f.size.get();

		if(exists) {
			if(fileSize == value.length() || !strictMode)
				return f.set(new InputOnlyFileItem(value, f.name.get()));
			value.delete();
		}

		//location == Location_DB && !exists
		InputStream inputStream = getInputStream(f);

		if(inputStream == null)
			throw new RuntimeException("Files.java:get(file file) inputStream == null, path: " + value.getAbsolutePath());

		value.getParentFile().mkdirs();
		long copiedSize = IOUtils.copyLarge(inputStream, new FileOutputStream(value));

		if(strictMode) {
			if(fileSize == 0) {
				f.size = new integer(copiedSize);
				newInstance().setSize(f.id, copiedSize);
			} else if(copiedSize != fileSize) {
				value.delete();
				throw new RuntimeException("Files.java:get(file file) file broken (size value doesn't match data size), fileId: " + f.id.get() +  ", path: " + value.getAbsolutePath());
			}
		}

		return f.set(new InputOnlyFileItem(value, f.name.get()));
	}

	public static File getFullStoragePath(file file) {
		String pathStr = file.getPath();
		return pathStr.startsWith(Storage) ? new File(ServerConfig.storagePath(), pathStr.substring(Storage.length()))
				: new File(Folders.Base, pathStr);
	}
	
	public static file z8_get(guid fileId) {
		return get(fileId);
	}

	public static file z8_get(file file) {
		try {
			return get(file);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static file z8_add(guid id, string path, string name, integer size, binary data) {
		return newInstance().add(id, path, name, size, data);
	}
	
	public static file z8_add(string path, string name, integer size, binary data) {
		return z8_add(null, path, name, size, data);
	}
	
	public static file z8_add(guid id, string path, string name, file f) {
		return newInstance().add(id, path, name, f.size, f.binary());
	}
	
	public static file z8_add(string path, string name, file f) {
		return z8_add(null, path, name, f.size, f.binary());
	}
}
