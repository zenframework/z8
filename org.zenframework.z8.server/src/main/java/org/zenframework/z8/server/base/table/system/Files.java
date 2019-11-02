package org.zenframework.z8.server.base.table.system;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.zenframework.z8.server.base.file.Folders;
import org.zenframework.z8.server.base.file.InputOnlyFileItem;
import org.zenframework.z8.server.base.table.Table;
import org.zenframework.z8.server.base.table.value.BinaryField;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.base.table.value.StringField;
import org.zenframework.z8.server.db.ConnectionManager;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.types.file;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.integer;
import org.zenframework.z8.server.utils.IOUtils;

public class Files extends Table {
	public static final String TableName = "SystemFiles";

	static public class fieldNames {
		public final static String File = "File";
		public final static String Path = "Path";
	}

	static public class strings {
		public final static String Title = "Files.title";
		public final static String Name = "Files.name";
		public final static String Path = "Files.path";

	}

	static public class displayNames {
		public final static String Title = Resources.get(strings.Title);
		public final static String Name = Resources.get(strings.Name);
		public final static String Path = Resources.get(strings.Path);
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

	public final StringField.CLASS<StringField> path = new StringField.CLASS<StringField>(this);
	public final BinaryField.CLASS<BinaryField> data = new BinaryField.CLASS<BinaryField>(this);

	static public Files newInstance() {
		return new Files.CLASS<Files>().get();
	}

	public Files(IObject container) {
		super(container);
	}

	@Override
	public void initMembers() {
		super.initMembers();

		objects.add(data);
		objects.add(path);
	}

	@Override
	public void constructor2() {
		super.constructor2();

		name.setDisplayName(displayNames.Name);
		name.get().length = new integer(512);

		data.setName(fieldNames.File);
		data.setIndex("data");

		path.setName(fieldNames.Path);
		path.setIndex("path");
		path.setDisplayName(displayNames.Path);
		path.get().length = new integer(512);
	}

	public void add(file file) {
		change(file, true);
	}

	public void updateFile(file file) {
		change(file, false);
	}

	private void change(file file, boolean create) {
		InputStream input = file.getInputStream();

		try {
			name.get().set(file.name);
			data.get().set(input);
			path.get().set(file.path);

			if(create)
				create(file.id);
			else
				update(file.id);

			ConnectionManager.get().flush();
		} finally {
			IOUtils.closeQuietly(input);
		}
	}

	public static InputStream getInputStream(file file) throws IOException {
		return getInputStream(file.id);
	}

	public static InputStream getInputStream(guid fileId) throws IOException {
		Files table = newInstance();

		Field data = table.data.get();
		Collection<Field> fields = Arrays.asList(data);

		if(!fileId.isNull() && table.readRecord(fileId, fields))
			return data.binary().get();

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

	public static file get(file file) throws IOException {
		File path = new File(Folders.Base, file.path.get());

		if(!path.exists()) {
			InputStream inputStream = getInputStream(file);

			if(inputStream == null)
				return null;

			FileUtils.copyInputStreamToFile(inputStream, path);
		}

		file.set(new InputOnlyFileItem(path, file.name.get()));
		file.size = new integer(path.length());
		file.time = file.time;

		return file;
	}
}
