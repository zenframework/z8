package org.zenframework.z8.server.base.file;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.zenframework.z8.server.base.table.Table;
import org.zenframework.z8.server.base.table.system.Files;
import org.zenframework.z8.server.base.table.value.AttachmentField;
import org.zenframework.z8.server.base.table.value.Field;
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
import org.zenframework.z8.server.utils.IOUtils;

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

	private AttachmentField field;

	public AttachmentProcessor(IObject container) {
		super(container);
	}

	public AttachmentProcessor(AttachmentField field) {
		set(field);
	}

	public Table getTable() {
		return (Table)field.owner();
	}

	public void set(AttachmentField field) {
		this.field = field;
	}

	public AttachmentField getField() {
		return field;
	}

	public Collection<file> read(guid recordId) {
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

	private void save(Collection<file> files, guid recordId) {
		getField().set(new string(file.toJson(files)));
		getTable().update(recordId);
	}

	public Collection<file> create(guid attachTo, Collection<file> files) {
		Files filesTable = Files.newInstance();

		for(file file : files) {
			if(file.id.isNull()) {
				file.id = guid.create();
				setUser(file);
				setPathIfEmpty(attachTo, file);
				putToCache(file);
				filesTable.add(file);
			}
		}

		return files;
	}

	private void putToCache(file file) {
		try {
			IOUtils.copy(file.getInputStream(), new File(Folders.Base, file.path.get()));
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}

	public Collection<file> update(guid attachTo, Collection<file> files) {
		Collection<file> result = read(attachTo);

		files = create(attachTo, files);

		result.addAll(files);
		save(result, attachTo);

		return result;
	}

	public Collection<file> remove(guid target, Collection<file> files) {
		Files filesTable = Files.newInstance();
		Collection<file> result = read(target);

		for(file file : files)
			filesTable.destroy(file.id);

		result.removeAll(files);
		save(result, target);

		return result;
	}

	private void setPathIfEmpty(guid recordId, file file) {
		if(file.path.isEmpty()) {
			date time = new date();
			String path = FileUtils.getFile(Folders.Storage, time.format("yyyy.MM.dd"), getTable().name(), recordId.toString(), field.name(), time.format("HH-mm-ss"), file.name.get()).toString();
			file.path = new string(path);
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
