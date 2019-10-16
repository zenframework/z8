package org.zenframework.z8.server.file;

import java.io.IOException;

import org.zenframework.z8.server.base.table.system.Files;
import org.zenframework.z8.server.types.file;

public class DefaultFileHandler implements IFileHandler {

	@Override
	public boolean canHandleRequest(file file) {
		return true;
	}

	@Override
	public file getFile(file file) throws IOException {
		return Files.get(file);
	}

}
