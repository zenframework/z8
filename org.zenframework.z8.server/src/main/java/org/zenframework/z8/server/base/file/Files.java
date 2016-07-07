package org.zenframework.z8.server.base.file;

import java.io.File;
import java.io.IOException;

import org.zenframework.z8.server.base.table.system.SystemFiles;
import org.zenframework.z8.server.engine.Z8Context;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.types.file;
import org.zenframework.z8.server.utils.PdfUtils;

public class Files {
	private final SystemFiles systemFiles = SystemFiles.newInstance();
	private FileConverter converter;

	public void addFile(file file) {
		systemFiles.addFile(file);
	}

	public file getFile(file file) throws IOException {
		return systemFiles.getFile(file);
	}

	public File getPreview(file file) throws IOException {
		if (!FileConverter.isConvertableToPdf(file.path.get()))
			return null;
		getFile(file);
		File path = new File(Z8Context.getWorkingPath(), file.path.get());
		if (!path.exists())
			return null;
		return getConverter().getConvertedPdf(file.path.get(), path);
	}

	public int getPageCount(file file) {
		try {
			File preview = getPreview(file);
			if (preview == null)
				return 0;
			return PdfUtils.getPageCount(preview);
		} catch (IOException e) {
			Trace.logError("Can't get pages count '" + file.path + "'", e);
			return 0;
		}
	}

	private FileConverter getConverter() {
		if (converter == null)
			converter = new FileConverter(new File(Z8Context.getWorkingPath(), Folders.Cache));
		return converter;
	}

}
