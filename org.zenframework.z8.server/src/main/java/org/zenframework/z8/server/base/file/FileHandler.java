package org.zenframework.z8.server.base.file;

import java.io.IOException;
import java.util.Map;

import org.zenframework.z8.server.file.IFileHandler;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.OBJECT;
import org.zenframework.z8.server.runtime.RLinkedHashMap;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.file;
import org.zenframework.z8.server.types.string;

public class FileHandler extends OBJECT implements IFileHandler {

	public static class CLASS<T extends FileHandler> extends OBJECT.CLASS<T> {
		public CLASS() {
			this(null);
		}

		public CLASS(IObject container) {
			super(container);
			setJavaClass(FileHandler.class);
		}

		@Override
		public Object newObject(IObject container) {
			return new FileHandler(container);
		}
	}

	public FileHandler() {}

	public FileHandler(IObject container) {
		super(container);
	}

	@Override
	public boolean canHandleRequest(file file) {
		return z8_canHandleRequest(file).get();
	}

	@Override
	public file getFile(file file, Map<String, String> parameters) throws IOException {
		return z8_getFile(file, toRLinkedHashMap(parameters));
	}

	public bool z8_canHandleRequest(file file) {
		return bool.False;
	}

	@SuppressWarnings("rawtypes")
	public file z8_getFile(file file, RLinkedHashMap parameters) {
		return file;
	}

	private static RLinkedHashMap<string, string> toRLinkedHashMap(Map<String, String> parameters) {
		RLinkedHashMap<string, string> result = new RLinkedHashMap<string, string>();
		for (Map.Entry<String, String> entry : parameters.entrySet())
			result.put(new string(entry.getKey()), new string(entry.getValue()));
		return result;
	}
}
