package org.zenframework.z8.server.file;

import java.io.IOException;

import org.zenframework.z8.server.types.file;

public interface IFileHandler {

	boolean canHandleRequest(file file);

	file getFile(file file) throws IOException;

}
