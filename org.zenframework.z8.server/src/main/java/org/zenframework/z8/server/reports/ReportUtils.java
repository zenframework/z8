package org.zenframework.z8.server.reports;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.zenframework.z8.server.types.date;
import org.zenframework.z8.server.types.guid;

public class ReportUtils {
	public static File getUniqueFileName(File folder, String name, String extension) {
		name = name.replace('/', '-').replace('\\', '-').replace(':', '-').replace('\n', ' ');

		if(name.endsWith("."))
			name = name.substring(0, name.length() - 1);

		date time = new date();
		File file = FileUtils.getFile(folder, time.format("yyyy.MM.dd"), guid.create().toString(), name + "." + extension);
		file.getParentFile().mkdirs();
		return file;
	}
}
