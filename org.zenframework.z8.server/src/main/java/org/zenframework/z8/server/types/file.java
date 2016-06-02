package org.zenframework.z8.server.types;

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
    public static final String EOL = "\r\n";

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
            
        if(!file.isDirectory())
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
