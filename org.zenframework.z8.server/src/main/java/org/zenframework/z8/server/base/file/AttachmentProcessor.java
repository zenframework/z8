package org.zenframework.z8.server.base.file;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.io.FileUtils;
import org.zenframework.z8.server.base.table.Table;
import org.zenframework.z8.server.base.table.system.Files;
import org.zenframework.z8.server.base.table.value.AttachmentField;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.OBJECT;
import org.zenframework.z8.server.runtime.RCollection;
import org.zenframework.z8.server.types.date;
import org.zenframework.z8.server.types.file;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.integer;
import org.zenframework.z8.server.types.string;
import org.zenframework.z8.server.utils.PdfUtils;

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
		if(getTable().readRecord(recordId, Arrays.<Field> asList(getField())))
			return get();
		return new ArrayList<FileInfo>();
	}

	public Collection<FileInfo> read(guid recordId, String type) {
		return filterByType(read(recordId), type);
	}

	private void save(Collection<FileInfo> files, guid recordId) {
		getField().set(new string(FileInfo.toJson(files)));
		getTable().update(recordId);
	}

	public Collection<FileInfo> create(guid target, Collection<FileInfo> files, String type) {
		Files filesTable = new Files.CLASS<Files>().get();

		for(FileInfo file : files) {
			boolean idIsNull = file.id == null || file.id.isNull();
			if(idIsNull || !filesTable.hasRecord(file.id)) {
				if(!idIsNull) {
					filesTable.recordId.get().set(file.id);
				}
				setPathIfEmpty(target, file);
				filesTable.name.get().set(file.name);
				filesTable.file.get().set(file.getInputStream());
				filesTable.path.get().set(file.path);
				file.type = new string(type);
				file.id = filesTable.create();
			}
		}

		return files;
	}

	public Collection<FileInfo> update(guid target, Collection<FileInfo> files, String type) {
		Collection<FileInfo> result = read(target);

		files = create(target, files, type);

		result.addAll(files);
		save(result, target);

		return result;
	}

	public Collection<FileInfo> remove(guid target, Collection<FileInfo> files) {
		Files filesTable = new Files.CLASS<Files>().get();
		Collection<FileInfo> result = read(target);

		for(FileInfo file : files) {
			filesTable.destroy(file.id);
		}

		result.removeAll(files);
		save(result, target);

		return result;

	}

	private void setPathIfEmpty(guid recordId, FileInfo fileInfo) {
		if(fileInfo.path.isEmpty()) {
			String path = FileUtils.getFile(file.StorageFolder, new date().format("yyyy.MM.dd"), getTable().classId(), recordId.toString(), field.name(), fileInfo.name.get()).toString();
			fileInfo.path = new string(path);
		}
	}

	public Collection<FileInfo> get() {
		return FileInfo.parseArray(getField().string().get());
	}

	private Collection<FileInfo> filterByType(Collection<FileInfo> files, String type) {
		if(type != null) {
			Iterator<FileInfo> i = files.iterator();
			while(i.hasNext()) {
				if(!type.equals(i.next().type.get()))
					i.remove();
			}
		}
		return files;
	}

	public int getPageCount(guid recordId) {
		int result = 0;

		Collection<FileInfo> fileInfos = read(recordId);

		for(FileInfo fileInfo : fileInfos) {
			try {
				result += getPageCount(fileInfo);
			} catch(IOException e) {
				Trace.logEvent("AttachmentProcessor.getPageCount('" + fileInfo.path + "'): '" + e.getMessage());
				result += 1;
			}
		}

		return result;
	}

	private int getPageCount(FileInfo fileInfo) throws IOException {
		String relativePath = fileInfo.path.get();
		File absolutePath = new File(file.BaseFolder, relativePath);

		if(!FileConverter.isConvertableToPdf(absolutePath))
			return 1;

		if(!absolutePath.exists())
			FileUtils.copyInputStreamToFile(Files.getInputStream(fileInfo), absolutePath);

		FileConverter fileConverter = new FileConverter(new File(file.BaseFolder, file.CacheFolderName));
		File pdfFile = fileConverter.getConvertedPdf(relativePath, absolutePath);

		return PdfUtils.getPageCount(pdfFile);
	}

	public RCollection<? extends FileInfo.CLASS<? extends FileInfo>> z8_get() {
		return toCollection(get());
	}

	public RCollection<? extends FileInfo.CLASS<? extends FileInfo>> z8_get(string type) {
		return toCollection(filterByType(get(), type.get()));
	}

	public RCollection<? extends FileInfo.CLASS<? extends FileInfo>> z8_read(guid recordId) {
		return toCollection(read(recordId));
	}

	public RCollection<? extends FileInfo.CLASS<? extends FileInfo>> z8_read(guid recordId, string type) {
		return toCollection(read(recordId, type.get()));
	}

	public RCollection<? extends FileInfo.CLASS<? extends FileInfo>> z8_create(guid target, RCollection<? extends FileInfo.CLASS<? extends FileInfo>> classes) {
		return z8_create(target, classes, new string());
	}

	public RCollection<? extends FileInfo.CLASS<? extends FileInfo>> z8_create(guid target, RCollection<? extends FileInfo.CLASS<? extends FileInfo>> classes, string type) {
		Collection<FileInfo> files = CLASS.asList(classes);
		return toCollection(create(target, files, type.get()));
	}

	public RCollection<? extends FileInfo.CLASS<? extends FileInfo>> z8_update(guid target, RCollection<? extends FileInfo.CLASS<? extends FileInfo>> classes) {
		return z8_update(target, classes, new string());
	}

	public RCollection<? extends FileInfo.CLASS<? extends FileInfo>> z8_update(guid target, RCollection<? extends FileInfo.CLASS<? extends FileInfo>> classes, string type) {
		Collection<FileInfo> files = CLASS.asList(classes);
		return toCollection(update(target, files, type.get()));
	}

	private RCollection<? extends FileInfo.CLASS<? extends FileInfo>> toCollection(Collection<FileInfo> files) {
		RCollection<FileInfo.CLASS<? extends FileInfo>> result = new RCollection<FileInfo.CLASS<? extends FileInfo>>();

		for(FileInfo file : files) {
			FileInfo.CLASS<FileInfo> cls = new FileInfo.CLASS<FileInfo>();
			cls.get().set(file);
			result.add(cls);
		}

		return result;
	}

	public integer z8_getPageCount(guid recordId) {
		return new integer(getPageCount(recordId));
	}
}
