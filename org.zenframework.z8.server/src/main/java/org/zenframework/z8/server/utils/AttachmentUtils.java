package org.zenframework.z8.server.utils;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import org.zenframework.z8.server.base.file.FileConverter;
import org.zenframework.z8.server.base.file.Folders;
import org.zenframework.z8.server.base.table.system.Files;
import org.zenframework.z8.server.config.ServerConfig;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.types.file;

public class AttachmentUtils {

	static public File getPreview(file file, Map<String, String> parameters) throws IOException {
		String ext = FileConverter.getExtension(file.baseName());
		if (!FileConverter.isConvertableToPdf(ext))
			return null;
		
		Files.get(file);
		
		File path = new File(ServerConfig.workingPath(), file.path.get());

		if (!path.exists())
			return null;

		if (FileConverter.isPdfExtension(ext))
			return path;

		File convertedFile = new File(Folders.Base, Folders.Cache + '/' + file.path.get());
		return FileConverter.convertToPdf(path, convertedFile, parameters);
	}

	static public int getPageCount(file file) {
		try {
			File preview = getPreview(file, Collections.<String, String>emptyMap());
			if (preview == null)
				return 0;
			return PdfUtils.getPageCount(preview);
		} catch (IOException e) {
			Trace.logError("Can't get pages count '" + file.path + "'", e);
			return 0;
		}
	}

}
