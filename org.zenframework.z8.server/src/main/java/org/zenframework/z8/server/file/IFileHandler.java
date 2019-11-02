package org.zenframework.z8.server.file;

import java.io.IOException;
import java.util.Map;

import org.zenframework.z8.server.types.file;

public interface IFileHandler {

	boolean canHandleRequest(file file);

	file getFile(file file, Map<String, String> parameters) throws IOException;

}
