package org.zenframework.z8.server.base.file;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;

import org.zenframework.z8.server.base.table.Table;
import org.zenframework.z8.server.base.table.system.Files;
import org.zenframework.z8.server.base.table.value.AttachmentField;
import org.zenframework.z8.server.json.JsonWriter;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.OBJECT;
import org.zenframework.z8.server.runtime.RCollection;
import org.zenframework.z8.server.types.file;
import org.zenframework.z8.server.types.guid;
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

    private Table table;
    private AttachmentField field;

    public AttachmentProcessor(IObject container) {
        super(container);
    }

    public AttachmentProcessor(Table table, AttachmentField field) {
        set(table, field);
    }

    public Table getTable() {
        return table;
    }

    public void set(Table table, AttachmentField field) {
        this.table = table;
        this.field = field;
    }

    public AttachmentField getField() {
        return field;
    }

    private List<FileInfo> getExisitingFiles(guid recordId) {
        if(getTable().readRecord(recordId)) 
            return FileInfo.parseArray(getField().string().get());
        return new ArrayList<FileInfo>();
    }

    private void save(Collection<FileInfo> files, guid recordId) {
        JsonWriter writer = new JsonWriter();

        writer.startArray();
        for (FileInfo file : files)
            writer.write(file);
        writer.finishArray();

        getField().set(new string(writer.toString()));
        getTable().update(recordId);
    }

    public Collection<FileInfo> create(guid target, Collection<FileInfo> files) {
        Files filesTable = new Files.CLASS<Files>().get();

        for (FileInfo file : files) {
            boolean idIsNull = file.id == null || file.id.isNull();
            if (idIsNull || !filesTable.hasRecord(file.id)) {
                if (!idIsNull) {
                    filesTable.recordId.get().set(file.id);
                }
                setPathIfEmpty(target, file);
                filesTable.name.get().set(file.name);
                filesTable.file.get().set(file.getInputStream());
                filesTable.path.get().set(file.path);
                filesTable.target.get().set(target);
                file.id = filesTable.create();
            }
        }

        return files;
    }

    public Collection<FileInfo> update(guid target, Collection<FileInfo> files) {
        Collection<FileInfo> result = getExisitingFiles(target);

        files = create(target, files);

        result.addAll(files);
        save(result, target);

        return result;
    }

    public Collection<FileInfo> read(guid recordId) {
        return getExisitingFiles(recordId);
    }

    public Collection<FileInfo> remove(guid target, Collection<FileInfo> files) {
        Files filesTable = new Files.CLASS<Files>().get();
        Collection<FileInfo> result = getExisitingFiles(target);

        for(FileInfo file: files){
            filesTable.destroy(file.id);
        }

        result.removeAll(files);
        save(result, target);

        return result;

    }

    private void setPathIfEmpty(guid recordId, FileInfo fileInfo) {
        if (fileInfo.path.isEmpty()) {
            String path = FileUtils.getFile(file.StorageFolder, getTable().classId(), recordId.toString(), fileInfo.name.get())
                    .toString();
            fileInfo.path = new string(path);
        }
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

    private RCollection<? extends FileInfo.CLASS<? extends FileInfo>> toCollection(Collection<FileInfo> files) {
        RCollection<FileInfo.CLASS<? extends FileInfo>> result = new RCollection<FileInfo.CLASS<? extends FileInfo>>();

        for (FileInfo file : files) {
            FileInfo.CLASS<FileInfo> cls = new FileInfo.CLASS<FileInfo>();
            cls.get().set(file);
            result.add(cls);
        }

        return result;
    }
}
