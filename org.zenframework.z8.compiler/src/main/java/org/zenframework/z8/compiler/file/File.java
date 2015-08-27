package org.zenframework.z8.compiler.file;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

public class File {
    private IPath path;

    private final String Charset = "UTF-8";
    
    static public File fromPath(IPath path) throws FileException {
        return new File(path);
    }

    public File(String path) throws FileException {
        this.path = new Path(path);
    }

    public File(IPath path) throws FileException {
        this.path = path;
    }

    public IPath getPath() {
        return path;
    }

    public long getTimeStamp() throws FileException {
        try {
            return path.toFile().lastModified();
        }
        catch(SecurityException e) {
            throw new FileException(path, e.getMessage());
        }
    }

    static public void rename(IPath oldPath, IPath newPath) throws FileException {
        try {
            oldPath.toFile().renameTo(newPath.toFile());
        }
        catch(SecurityException e) {
            throw new FileException(oldPath, e.getMessage());
        }
    }

    public char[] read() throws FileException, UnsupportedEncodingException {
        FileInputStream stream;

        try {
            stream = new FileInputStream(path.toString());
        }
        catch(FileNotFoundException e) {
            throw new FileException(path, e.getMessage());
        }

        byte[] rawBytes;

        try {
            rawBytes = new byte[stream.available()];
            stream.read(rawBytes);
        }
        catch(IOException e) {
            throw new FileException(path, e.getMessage());
        }
        finally {
            try {
                stream.close();
            }
            catch(IOException e) {}
        }

        return new String(rawBytes, Charset).toCharArray();
    }

    private void write(String string, boolean append) throws FileException {
        FileOutputStream stream = null;

        try {
            String path = getPath().toString();

            java.io.File file = new java.io.File(path);

            if(file.exists() && !file.canWrite() && !append) {
                file.delete();
            }

            stream = new FileOutputStream(getPath().toString(), append);
            stream.write(string.getBytes(Charset));
        }
        catch(java.lang.SecurityException e) {
            throw new FileException(path, e.getMessage());
        }
        catch(java.io.IOException e) {
            throw new FileException(path, e.getMessage());
        }
        finally {
            try {
                if(stream != null) {
                    stream.close();
                }
            }
            catch(java.io.IOException e) {
                throw new FileException(path, e.getMessage());
            }
        }
    }

    public void write(String string) throws FileException {
        write(string, false);
    }

    public void append(String string) throws FileException {
        write(string, true);
    }

    public boolean exists() throws FileException {
        try {
            return new java.io.File(path.toString()).exists();
        }
        catch(SecurityException e) {
            throw new FileException(path, e.getMessage());
        }
    }

    public boolean isDirectory() throws FileException {
        try {
            return new java.io.File(path.toString()).isDirectory();
        }
        catch(SecurityException e) {
            throw new FileException(path, e.getMessage());
        }
    }

    public IPath[] getFiles() throws FileException {
        try {
            String[] fileNames = path.toFile().list();
            IPath[] result = new IPath[fileNames.length];

            for(int i = 0; i < fileNames.length; i++) {
                result[i] = path.append(fileNames[i]);
            }

            return result;
        }
        catch(SecurityException e) {
            throw new FileException(path, e.getMessage());
        }
    }

    public boolean makeDirectories() throws FileException {
        try {
            return path.toFile().mkdirs();
        }
        catch(SecurityException e) {
            throw new FileException(path, e.getMessage());
        }
    }
}
