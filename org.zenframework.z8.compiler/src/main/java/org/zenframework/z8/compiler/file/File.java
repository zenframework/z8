package org.zenframework.z8.compiler.file;

import java.io.Closeable;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import org.eclipse.core.runtime.IPath;

public class File {

	private static enum Type {
		FILE, FOLDER, ARCHIVE, ARCHIVED_FILE, ARCHIVED_FOLDER
	}

	private static final String Charset = "UTF-8";

	private static final java.io.File TEMP_DIR = new java.io.File(new java.io.File(System.getProperty("java.io.tmpdir")), "z8-compiler");
	private static final Map<IPath, File> ARCHIVE_CACHE = new HashMap<IPath, File>();

	private final IPath path;
	private final java.io.File file;
	private final Type type;

	static public File fromPath(IPath path) throws FileException {
		IPath absolute = path.makeAbsolute();
		for (Map.Entry<IPath, File> entry : ARCHIVE_CACHE.entrySet()) {
			if (absolute.equals(entry.getKey()))
				return entry.getValue();
			else if (entry.getKey().isPrefixOf(absolute)) {
				java.io.File file = new java.io.File(entry.getValue().file, absolute.makeRelativeTo(entry.getKey()).toString());
				return new File(path, file, file.isDirectory() ? Type.ARCHIVED_FOLDER : Type.ARCHIVED_FILE);
			}
		}

		if (absolute.toFile().isDirectory())
			return new File(path, path.toFile(), Type.FOLDER);

		ZipFile zipfile = null;
		try {
			zipfile = new ZipFile(absolute.toString());
			File file = new File(path, new java.io.File(TEMP_DIR, path.lastSegment() + '@' + Integer.toString(absolute.hashCode(), 16)), Type.ARCHIVE);
			ARCHIVE_CACHE.put(absolute, file);
			return file;
		} catch (IOException e) {
			return new File(path, path.toFile(), Type.FILE);
		} finally {
			try {
				if (zipfile != null) {
					zipfile.close();
					zipfile = null;
				}
			} catch (IOException e) {}
		}
	}

	private File(IPath path, java.io.File file, Type type) {
		this.path = path;
		this.file = file;
		this.type = type;
	}

	public IPath getPath() {
		return path;
	}

	public long getTimeStamp() throws FileException {
		try {
			return file.lastModified();
		} catch(SecurityException e) {
			throw new FileException(path, e.getMessage());
		}
	}

	static public void rename(IPath oldPath, IPath newPath) throws FileException {
		try {
			oldPath.toFile().renameTo(newPath.toFile());
		} catch(SecurityException e) {
			throw new FileException(oldPath, e.getMessage());
		}
	}

	public char[] read() throws FileException, UnsupportedEncodingException {
		FileInputStream stream;

		try {
			stream = new FileInputStream(file);
		} catch(FileNotFoundException e) {
			throw new FileException(path, e.getMessage());
		}

		byte[] rawBytes;

		try {
			rawBytes = new byte[stream.available()];
			stream.read(rawBytes);
		} catch(IOException e) {
			throw new FileException(path, e.getMessage());
		} finally {
			closeQuietly(stream);
		}

		return new String(rawBytes, Charset).toCharArray();
	}

	private void write(String string, boolean append) throws FileException {
		FileOutputStream stream = null;

		try {
			if(file.exists() && !file.canWrite() && !append)
				file.delete();

			stream = new FileOutputStream(file, append);
			stream.write(string.getBytes(Charset));
			stream.close();
		} catch(Exception e) {
			closeQuietly(stream);
			throw new FileException(path, e.getMessage());
		}
	}

	public void write(String string) throws FileException {
		write(string, false);
	}

	public void append(String string) throws FileException {
		write(string, true);
	}

	public boolean exists() {
		return file.exists();
	}

	public boolean isContainer() throws FileException {
		return type == Type.FOLDER || type == Type.ARCHIVE || type == Type.ARCHIVED_FOLDER;
	}

	public File[] getFiles() throws FileException {
		boolean archived = false;
		java.io.File pathFile = this.path.toFile();
		try {
			if (type == Type.ARCHIVE) {
				unzip(new FileInputStream(pathFile), file, pathFile.lastModified() > file.lastModified());
				archived = true;
			}

			java.io.File[] files = file.listFiles();
			File[] result = new File[files.length];

			for (int i = 0; i < files.length; i++) {
				java.io.File child = new java.io.File(file, files[i].getName());
				result[i] = new File(path.append(files[i].getName()), child, getType(child.isDirectory(), archived));
			}

			return result;
		} catch(Exception e) {
			throw new FileException(path, e.getMessage());
		}
	}

	public boolean makeDirectories() throws FileException {
		try {
			return file.mkdirs();
		} catch(SecurityException e) {
			throw new FileException(path, e.getMessage());
		}
	}

	private static boolean delete(java.io.File path) {
		java.io.File[] files = path.listFiles();
		if (files != null) {
			for (java.io.File file : files)
				delete(file);
		}
		return path.delete();
	}

	private static void unzip(InputStream in, java.io.File destDir, boolean overwrite) {
		if (destDir.exists()) {
			if (overwrite) {
				delete(destDir);
			} else {
				String[] files = destDir.list();
				if (files != null && files.length > 0)
					return;
			}
		}
		byte[] buffer = new byte[8192];
		ZipInputStream zip = null;
		try {
			zip = new ZipInputStream(in);
			for (ZipEntry ze = zip.getNextEntry(); ze != null; ze = zip.getNextEntry()) {
				java.io.File newFile = new java.io.File(destDir, ze.getName().replace('\\', '/'));

				if (ze.isDirectory()) {
					newFile.mkdirs();
					zip.closeEntry();
					continue;
				}

				newFile.getParentFile().mkdirs();

				FileOutputStream out = new FileOutputStream(newFile);

				try {
					for (int n = zip.read(buffer); n >= 0; n = zip.read(buffer))
						out.write(buffer, 0, n);
				} finally {
					out.close();
				}

				zip.closeEntry();
			}
		} catch (Throwable e) {
			delete(destDir);
			throw new RuntimeException(e);
		} finally {
			closeQuietly(zip);
		}
	}

	private static void closeQuietly(Closeable closeable) {
		try {
			if (closeable != null)
				closeable.close();
		} catch (IOException e) {}
	}

	private static Type getType(boolean folder, boolean archived) {
		if (folder)
			return archived ? Type.ARCHIVED_FOLDER : Type.FOLDER;
		return archived ? Type.ARCHIVED_FILE : Type.FILE;
	}

}
