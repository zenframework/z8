package org.zenframework.z8.server.base.table.system;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;

import org.zenframework.z8.server.base.file.FileInfo;
import org.zenframework.z8.server.base.file.FilesFactory;
import org.zenframework.z8.server.base.file.Folders;
import org.zenframework.z8.server.base.file.InputOnlyFileItem;
import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.base.table.Table;
import org.zenframework.z8.server.base.table.value.BinaryField;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.base.table.value.StringField;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.db.sql.expressions.Operation;
import org.zenframework.z8.server.db.sql.expressions.Rel;
import org.zenframework.z8.server.request.Loader;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.utils.IOUtils;

public class Files extends Table {

	private static final String TABLE_PREFIX = "table";

	public static final String TableName = "SystemFiles";

	static public class names {
		public final static String File = "File";
		public final static String Target = "Target";
		public final static String Table = "Table";
		public final static String Path = "Path";
	}

	static public class strings {
		public final static String Title = "Files.title";
		public final static String Name = "Files.name";
		public final static String File = "Files.file";
		public final static String Target = "Files.target";
		public final static String Table = "Files.table";
		public final static String Path = "Files.path";
	}

	public static class CLASS<T extends Files> extends Table.CLASS<T> {
		public CLASS() {
			this(null);
		}

		public CLASS(IObject container) {
			super(container);
			setJavaClass(Files.class);
			setName(TableName);
			setDisplayName(Resources.get(Files.strings.Title));
		}

		@Override
		public Object newObject(IObject container) {
			return new Files(container);
		}
	}

	public StringField.CLASS<StringField> path = new StringField.CLASS<StringField>(this);
	public BinaryField.CLASS<BinaryField> file = new BinaryField.CLASS<BinaryField>(this);

	public Files() {
		this(null);
	}

	public Files(IObject container) {
		super(container);
	}

	@Override
	public void constructor2() {
		super.constructor2();

		name.setDisplayName(Resources.get(Files.strings.Name));
		name.get().length.set(512);

		file.setName(names.File);
		file.setIndex("file");
		file.setDisplayName(Resources.get(strings.File));

		path.setName(names.Path);
		path.setIndex("path");
		path.setDisplayName(Resources.get(strings.Path));
		path.get().length.set(512);

		registerDataField(file);
		registerDataField(path);
	}

	public static Files instance() {
		return new Files.CLASS<Files>().get();
	}

	public static InputStream getInputStream(FileInfo fileInfo) throws IOException {
		Files table = new Files.CLASS<Files>().get();

		SqlToken where = new Rel(table.path.get(), Operation.Eq, fileInfo.path.sql_string());

		guid recordId = fileInfo.id;

		Field file = table.file.get();
		Collection<Field> fields = Arrays.asList(file);

		if(recordId != null && !recordId.isNull() && table.readRecord(recordId, fields) || table.readFirst(fields, where))
			return file.binary().get();

		return null;
	}

	public static FileInfo getFile(FileInfo fileInfo) throws IOException {
		if(fileInfo.path.get().startsWith(TABLE_PREFIX))
			return getFileFromTable(fileInfo);
		else
			return getFileFromStorage(fileInfo);
	}

	private static FileInfo getFileFromStorage(FileInfo fileInfo) throws IOException {
		File path = new File(Folders.Base, fileInfo.path.get());
		if(FileInfo.isDefaultWrite()) {
			InputStream inputStream = !path.exists() ? getInputStream(fileInfo) : new FileInputStream(path);
			if(inputStream == null)
				return null;
			fileInfo.file = FilesFactory.createFileItem(fileInfo.name.get());
			IOUtils.copy(inputStream, fileInfo.getOutputStream());
			return fileInfo;
		} else {
			if(!path.exists()) {
				InputStream inputStream = getInputStream(fileInfo);
				if(inputStream == null)
					return null;
				IOUtils.copy(inputStream, path);
			}
			fileInfo.file = new InputOnlyFileItem(path, fileInfo.name.get());
			return fileInfo;
		}
	}

	private static FileInfo getFileFromTable(FileInfo fileInfo) throws IOException {
		File path = new File(Folders.Base, fileInfo.path.get());
		if(FileInfo.isDefaultWrite()) {
			InputStream inputStream = !path.exists() ? getTableFieldInputStream(fileInfo) : new FileInputStream(path);
			if(inputStream == null)
				return null;
			fileInfo.file = FilesFactory.createFileItem(fileInfo.name.get());
			IOUtils.copy(inputStream, fileInfo.getOutputStream());
			return fileInfo;
		} else {
			if(!path.exists()) {
				InputStream inputStream = getTableFieldInputStream(fileInfo);
				if(inputStream == null)
					return null;
				IOUtils.copy(inputStream, path);
			}
			fileInfo.file = new InputOnlyFileItem(path, fileInfo.name.get());
			return fileInfo;
		}

	}

	private static InputStream getTableFieldInputStream(FileInfo fileInfo) throws IOException {
		File field = new File(fileInfo.path.get());
		File recordId = field.getParentFile();
		File table = recordId.getParentFile();
		Query query = (Query)Loader.getInstance(table.getName());
		Field dataField = query.getFieldByName(field.getName());
		if(query.readRecord(new guid(recordId.getName()), Arrays.asList(dataField))) {
			return new ByteArrayInputStream(dataField.get().toString().getBytes());
		} else {
			throw new IOException("Incorrect path '" + fileInfo.path + "'");
		}
	}

}
