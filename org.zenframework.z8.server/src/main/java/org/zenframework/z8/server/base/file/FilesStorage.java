package org.zenframework.z8.server.base.file;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.zenframework.z8.server.types.file;
import org.zenframework.z8.server.types.string;
import org.zenframework.z8.server.utils.IOUtils;

public class FilesStorage {

    private File root;

    public FilesStorage(File storageRoot) {
        initialize(storageRoot);
    }

    private void initialize(File storageRoot) {
        this.root = storageRoot;
    }

    public File save(FileInfo file, String fileName) throws IOException {
        File f = save(file.getInputStream(), fileName);
        file.path = new string(f.getPath());
        return f;
    }

    public File save(byte[] data, String fileName) throws IOException {
        return save(new ByteArrayInputStream(data), fileName);
    }

    public File save(InputStream inputStream, String fileName) throws IOException {
        File result = file.getUniqueFileName(root, fileName);

        File file = getFile(result.toString());
        file.getParentFile().mkdirs();

        IOUtils.copy(inputStream, new FileOutputStream(file));

        return result;
    }

    public File getFile(String fileName) {
        return root != null ? new File(root, fileName) : new File(fileName);
    }
    
    public String getRootPath(){
        return root.getAbsolutePath();
    }

    public file z8_getFile(string fileName) {
        return new file(getFile(fileName.get()));
    }
}
