package org.zenframework.z8.pde.build;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.core.runtime.IPath;

public class FileUtils {

	static public void copyFolder(File src, File dest, FileFilter filter) throws IOException {
		if(src.isDirectory()) {
			if(!dest.exists()) {
				dest.mkdir();
			}

			File files[] = src.listFiles(filter);
			for(File file : files) {
				File destFile = new File(dest, file.getName());

				copyFolder(file, destFile, filter);
			}
		} else {
			copyFile(src, dest, true);
		}
	}

	static public void copyFile(IPath srcFile, IPath destFile, boolean force_write) throws IOException {
		copyFile(srcFile.toFile(), destFile.toFile(), force_write);
	}

	static public void copyFile(File srcFile, File destFile, boolean force_write) throws IOException {

		InputStream in = new FileInputStream(srcFile);
		if(force_write && !destFile.canWrite())
			destFile.delete();
		OutputStream out = new FileOutputStream(destFile);

		byte[] buf = new byte[8192];
		int len;
		while((len = in.read(buf)) > 0) {
			out.write(buf, 0, len);
		}
		in.close();
		out.close();
	}

	static public void removeDir(IPath path) throws IOException {
		try {
			_RemoveDir(path.toString());
		} catch(Throwable e) {
			throw new IOException();
		}
	}

	static void _RemoveDir(String _path) throws IOException {
		File[] listFiles = (new File(_path)).listFiles();
		if(listFiles != null) // ���� ������ ���, �� ������ �� ���������
			for(File fd : listFiles) {
				if(fd.isDirectory())
					_RemoveDir(fd.getCanonicalPath());
				else {
					if(!fd.delete())
						throw new IOException();
				}
			}
		if(!(new File(_path)).delete())
			throw new IOException();
	}

}
