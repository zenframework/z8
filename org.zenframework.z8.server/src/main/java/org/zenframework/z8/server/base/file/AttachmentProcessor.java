package org.zenframework.z8.server.base.file;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.zenframework.z8.server.base.table.Table;
import org.zenframework.z8.server.base.table.system.SystemFiles;
import org.zenframework.z8.server.base.table.value.AttachmentField;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.engine.Z8Context;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.OBJECT;
import org.zenframework.z8.server.runtime.RCollection;
import org.zenframework.z8.server.types.datetime;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.integer;
import org.zenframework.z8.server.types.string;

public class AttachmentProcessor extends OBJECT {

	public static class CLASS<T extends AttachmentProcessor> extends OBJECT.CLASS<T> {
		public CLASS() {
			this(null);
		}

		public CLASS(IObject container) {
			super(container);
			setJavaClass(AttachmentProcessor.class);
			setAttribute(Native, AttachmentProcessor.class.getCanonicalName());
		}

		@Override
		public Object newObject(IObject container) {
			return new AttachmentProcessor(container);
		}
	}

	private Files files;
	private AttachmentField field;

	public AttachmentProcessor(IObject container) {
		super(container);
	}

	public AttachmentProcessor(AttachmentField field) {
		set(field);
	}

	public Table getTable() {
		return (Table) field.owner();
	}

	public void set(AttachmentField field) {
		this.field = field;
	}

	public AttachmentField getField() {
		return field;
	}

	public Collection<FileInfo> read(guid recordId) {
		if (getTable().readRecord(recordId, Arrays.<Field> asList(getField())))
	        return FileInfo.parseArray(getField().string().get());
		return new ArrayList<FileInfo>();
	}

	private void save(Collection<FileInfo> files, guid recordId) {
		getField().set(new string(FileInfo.toJson(files)));
		getTable().update(recordId);
	}

	public Collection<FileInfo> create(guid attachTo, Collection<FileInfo> files) {
		SystemFiles filesTable = SystemFiles.newInstance();

		for (FileInfo file : files) {
			boolean idIsNull = file.id == null || file.id.isNull();
			if (idIsNull || !filesTable.hasRecord(file.id)) {
				if (!idIsNull)
					filesTable.recordId.get().set(file.id);

				setPathIfEmpty(attachTo, file);
				file.instanceId = new string(Z8Context.getInstanceId());
				filesTable.addFile(file);
			}
		}

		return files;
	}

	public Collection<FileInfo> update(guid attachTo, Collection<FileInfo> files) {
		Collection<FileInfo> result = read(attachTo);

		files = create(attachTo, files);

		result.addAll(files);
		save(result, attachTo);

		return result;
	}

	public Collection<FileInfo> remove(guid target, Collection<FileInfo> files) {
		SystemFiles filesTable = SystemFiles.newInstance();
		Collection<FileInfo> result = read(target);

		for (FileInfo file : files)
			filesTable.destroy(file.id);

		result.removeAll(files);
		save(result, target);

		return result;
	}

	private void setPathIfEmpty(guid recordId, FileInfo fileInfo) {
		if (fileInfo.path.isEmpty()) {
			datetime time = new datetime();
			String path = FileUtils.getFile(Folders.Storage, time.format("yyyy.MM.dd"), getTable().classId(),
					recordId.toString(), field.name(), time.format("HH-mm-ss"), fileInfo.name.get()).toString();
			fileInfo.path = new string(path);
		}
	}

	public int getPageCount(guid recordId) {
		int result = 0;
		Collection<FileInfo> fileInfos = read(recordId);
		for (FileInfo fileInfo : fileInfos) {
			result += getFiles().getPageCount(fileInfo);
		}
		return result;
	}

	static public RCollection<? extends FileInfo.CLASS<? extends FileInfo>> z8_parse(string json) {
	    return toCollection(FileInfo.parseArray(json.get()));
	}

	public RCollection<? extends FileInfo.CLASS<? extends FileInfo>> z8_read(guid recordId) {
		return toCollection(read(recordId));
	}

	public RCollection<? extends FileInfo.CLASS<? extends FileInfo>> z8_create(guid target,
			RCollection<? extends FileInfo.CLASS<? extends FileInfo>> classes) {
		Collection<FileInfo> files = CLASS.asList(classes);
		return toCollection(create(target, files));
	}

	public RCollection<? extends FileInfo.CLASS<? extends FileInfo>> z8_update(guid target,
			RCollection<? extends FileInfo.CLASS<? extends FileInfo>> classes) {
		Collection<FileInfo> files = CLASS.asList(classes);
		return toCollection(update(target, files));
	}

	public integer z8_getPageCount(guid recordId) {
		return new integer(getPageCount(recordId));
	}

	static private RCollection<? extends FileInfo.CLASS<? extends FileInfo>> toCollection(Collection<FileInfo> files) {
		RCollection<FileInfo.CLASS<? extends FileInfo>> result = new RCollection<FileInfo.CLASS<? extends FileInfo>>();

		for (FileInfo file : files) {
			FileInfo.CLASS<FileInfo> cls = new FileInfo.CLASS<FileInfo>();
			cls.get().set(file);
			result.add(cls);
		}

		return result;
	}

	private Files getFiles() {
		if (files == null)
			files = Files.newInstance();
		return files;
	}

}
