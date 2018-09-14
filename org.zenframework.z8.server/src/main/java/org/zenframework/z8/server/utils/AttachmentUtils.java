package org.zenframework.z8.server.utils;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FilenameUtils;
import org.zenframework.z8.server.base.file.FileConverter;
import org.zenframework.z8.server.base.file.Folders;
import org.zenframework.z8.server.base.table.system.Files;
import org.zenframework.z8.server.config.ServerConfig;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.types.file;

public class AttachmentUtils {
	static public File getPreview(file file) throws IOException {
		if (!FileConverter.isConvertableToPdf(file.path.get()))
			return null;
		
		String systemPath = FilenameUtils.separatorsToSystem(file.path.get());

		Files.get(file);
		
		File path = new File(ServerConfig.workingPath(), systemPath);

		if (!path.exists())
			return null;
		
		return new FileConverter(new File(Folders.Base, Folders.Cache)).getConvertedPdf(systemPath, path);
	}

	static public int getPageCount(file file) {
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
}
