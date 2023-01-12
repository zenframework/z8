package org.zenframework.z8.server.base.file;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.zenframework.z8.server.base.table.Table;
import org.zenframework.z8.server.base.table.system.Files;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.base.table.value.FileField;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.engine.ApplicationServer;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.OBJECT;
import org.zenframework.z8.server.runtime.RCollection;
import org.zenframework.z8.server.security.IUser;
import org.zenframework.z8.server.types.date;
import org.zenframework.z8.server.types.file;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.integer;
import org.zenframework.z8.server.types.string;
import org.zenframework.z8.server.utils.AttachmentUtils;
import org.zenframework.z8.server.utils.StringUtils;

public class AttachmentProcessor extends OBJECT {

	public static class CLASS<T extends AttachmentProcessor> extends OBJECT.CLASS<T> {
		public CLASS() {
			this(null);
		}

		public CLASS(IObject container) {
			super(container);
			setJavaClass(AttachmentProcessor.class);
		}

		@Override
		public Object newObject(IObject container) {
			return new AttachmentProcessor(container);
		}
	}

	private FileField field;

	public AttachmentProcessor(IObject container) {
		super(container);
	}

	public AttachmentProcessor(FileField field) {
		set(field);
	}

	public Table getTable() {
		return (Table)field.owner();
	}

	public void set(FileField field) {
		this.field = field;
	}

	public FileField getField() {
		return field;
	}

	public Collection<file> read(guid recordId) {
		if(getField().type() == FieldType.File)
			return new ArrayList<file>();

		Table table = getTable();

		table.saveState();

		try {
			if(getTable().readRecord(recordId, Arrays.<Field>asList(getField())))
				return file.parse(getField().string().get());
			return new ArrayList<file>();
		} finally {
			table.restoreState();
		}
	}

	public void save(guid recordId, Collection<file> files) {
		getField().set(new string(file.toJson(files)));
		getTable().update(recordId);
	}

	public Collection<file> create(guid recordId, Collection<file> files) {
		Files filesTable = Files.newInstance();

		for(file file : files) {
			if (file.name.get().length() > org.zenframework.z8.server.types.file.DISK_MAX_FILENAME_LENGTH)
				throw new RuntimeException("The file name is too long: '" + file.name.get() + "'");
		}

		for(file file : files) {
			if(file.id.isNull()) {
				file.id = guid.create();
				setUser(file);
				setPathIfEmpty(recordId, file);
				filesTable.add(file);
			}
		}

		return files;
	}

	public Collection<file> update(guid recordId, Collection<file> files) {
		Collection<file> result = read(recordId);

		files = create(recordId, files);

		result.addAll(files);
		save(recordId, result);

		return result;
	}

	public Collection<file> remove(guid recordId, Collection<file> files) {
		Files filesTable = Files.newInstance();
		Collection<file> result = read(recordId);

		for(file file : files)
			filesTable.destroy(file.id);

		result.removeAll(files);
		save(recordId, result);

		return result;
	}

	private void setPathIfEmpty(guid recordId, file f) {
		String path = f.path.get();

		if(path.isEmpty() || !path.startsWith(Files.Storage)) {
			date time = new date();
			path = Files.Storage + StringUtils.concat(file.separator, time.format("yyyy.MM.dd"), getTable().name(), recordId.toString(), field.name(), time.format("HH-mm-ss"), f.name.get());
			f.path = new string(path);
		}
	}

	private void setUser(file file) {
		IUser user = ApplicationServer.getUser();
		String name = user.name();
		file.author = new string(name + (name.isEmpty() ? "" : " - ") + user.login());
		file.user = user.id();
	}

	public int getTotalPageCount(guid recordId) {
		int result = 0;

		Collection<file> files = read(recordId);

		for(file file : files)
			result += AttachmentUtils.getPageCount(file);

		return result;
	}

	static public RCollection<file> z8_parse(string json) {
		return new RCollection<file>(file.parse(json.get()));
	}

	public RCollection<file> z8_read(guid recordId) {
		return new RCollection<file>(read(recordId));
	}

	public RCollection<file> z8_create(guid target, RCollection<file> files) {
		return new RCollection<file>(create(target, files));
	}

	public RCollection<file> z8_update(guid target, RCollection<file> files) {
		return new RCollection<file>(update(target, files));
	}

	public RCollection<file> z8_remove(guid target, RCollection<file> files) {
		return new RCollection<file>(remove(target, files));
	}

	public integer z8_getTotalPageCount(guid recordId) {
		return new integer(getTotalPageCount(recordId));
	}
}
