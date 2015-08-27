package org.zenframework.z8.server.types;

import org.zenframework.z8.server.engine.ApplicationServer;
import org.zenframework.z8.server.utils.IOUtils;

import java.io.*;

public class file extends primary implements Serializable {
	public static final String EOL = "\r\n";

	private static final long serialVersionUID = 7150824619911286312L;

	public static final String FilesFolderName = "files";
	public static final String StorageFolderName = "storage";
    public static final String CacheFolderName = "pdf.cache";
    public static final String LuceneFolderName = "lucene";

	private File file;

	public static File BaseFolder = ApplicationServer.workingPath();
	public static File FilesFolder = new File(FilesFolderName);
	public static File StorageFolder = new File(StorageFolderName);
    public static File LuceneFolder = new File(BaseFolder, LuceneFolderName);

	public file() {
	}

	static class FileParts {
		String folder;
		String name;
		String extension;

		FileParts(File file) {
			folder = file.getParent();

			String name = file.getName().replace('/', '-').replace('\\', '-')
					.replace(':', '-').replace('\n', ' ');

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
		return object instanceof file && file != null && file.equals(((file)object).file);
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

		if (path.startsWith(BaseFolder.getPath())) {
			return path.substring(BaseFolder.getPath().length() + 1);
		}

		return path;
	}

	public void operatorAssign(file value) {
		file = value.file;
	}

	public void operatorAssign(string pathName) {
		file = new File(pathName.get());

		if (file.isAbsolute()) {
			throw new RuntimeException(
					"Only relative file path can be applied to objects of class 'file'. + \n'\\folder\\file.name' - correct\n'c:\\folder\\file.name' - incorrect");
		}

		
		file = new File(new File(BaseFolder, FilesFolderName), file.getPath());

		File parent = file.getParentFile();
		parent.mkdirs();
	}

	public string z8_getPath() {
		return new string(getRelativePath());
	}

	public string z8_read() {
		return z8_read(encoding.UTF8);
	}

	public string z8_read(encoding charset) {
		try {
			FileInputStream input = new FileInputStream(file);
			ByteArrayOutputStream output = new ByteArrayOutputStream();
            try {
                IOUtils.copy(input, output);
                return new string(output.toByteArray(), charset);
            } finally {
                input.close();
                output.close();
            }
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
				File folder = new File(BaseFolder, FilesFolder.getPath());
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
