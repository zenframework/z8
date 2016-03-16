package org.zenframework.z8.server.base.file;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.zenframework.z8.server.types.file;
import org.zenframework.z8.server.types.string;
import org.zenframework.z8.server.utils.IOUtils;

public class FileStorage {

	private File path;

	static public FileStorage cache() {
		return new FileStorage(new File(Folders.Base, Folders.Cache));
	}

	public FileStorage(File path) {
		this.path = path;
	}

	public File save(FileInfo file, String fileName) throws IOException {
		File f = save(file.getInputStream(), fileName);
		file.path = new string(f.getPath());
		return f;
	}

	private File save(InputStream inputStream, String fileName) throws IOException {
		File result = file.getUniqueFileName(path, fileName);

		File file = getFile(result.toString());
		file.getParentFile().mkdirs();

		IOUtils.copy(inputStream, new FileOutputStream(file));

		return result;
	}

	public File getFile(String fileName) {
		return path != null ? new File(path, fileName) : new File(fileName);
	}

	public String getRootPath() {
		return path.getAbsolutePath();
	}
}
