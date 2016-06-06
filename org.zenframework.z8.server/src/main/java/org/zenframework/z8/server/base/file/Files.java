package org.zenframework.z8.server.base.file;

import java.io.File;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.zenframework.z8.server.base.table.system.SystemFiles;
import org.zenframework.z8.server.engine.Z8Context;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.OBJECT;
import org.zenframework.z8.server.utils.PdfUtils;

public class Files extends OBJECT {

	private static final Log LOG = LogFactory.getLog(Files.class);

	public static class CLASS<T extends Files> extends OBJECT.CLASS<T> {
		public CLASS() {
			this(null);
		}

		public CLASS(IObject container) {
			super(container);
			setJavaClass(Files.class);
		}

		@Override
		public Object newObject(IObject container) {
			return new Files(container);
		}
	}

	private final SystemFiles systemFiles = SystemFiles.newInstance();
	private FileConverter converter;

	private Files(IObject container) {
		super(container);
	}

	public static Files newInstance() {
		return new Files.CLASS<Files>().get();
	}

	public void addFile(FileInfo fileInfo) {
		systemFiles.addFile(fileInfo);
	}

	public FileInfo getFile(FileInfo fileInfo) throws IOException {
		return systemFiles.getFile(fileInfo);
	}

	public File getPreview(FileInfo fileInfo) throws IOException {
		if (!FileConverter.isConvertableToPdf(fileInfo.path.get()))
			return null;
		getFile(fileInfo);
		File path = new File(Z8Context.getWorkingPath(), fileInfo.path.get());
		if (!path.exists())
			return null;
		return getConverter().getConvertedPdf(fileInfo.path.get(), path);
	}

	public int getPageCount(FileInfo fileInfo) {
		try {
			File preview = getPreview(fileInfo);
			if (preview == null)
				return 0;
			return PdfUtils.getPageCount(preview);
		} catch (IOException e) {
			LOG.warn("Can't get pages count '" + fileInfo.path + "'", e);
			return 0;
		}
	}

	private FileConverter getConverter() {
		if (converter == null)
			converter = new FileConverter(new File(Z8Context.getWorkingPath(), Folders.Cache));
		return converter;
	}

}
