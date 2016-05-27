package org.zenframework.z8.server.base.file;

import org.zenframework.z8.server.resources.Resources;

public class FileInfoNotFoundException extends Exception {

	private static final long serialVersionUID = 7648359491931705877L;

	private final boolean retryLater;

	public FileInfoNotFoundException(FileInfo fileInfo, boolean retryLater) {
		super(Resources.format(retryLater ? "Files.retryLater" : "Files.notFound", fileInfo));
		this.retryLater = retryLater;
	}

	public boolean isRetryLater() {
		return retryLater;
	}

}
