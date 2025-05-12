package org.zenframework.z8.webserver.resource;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.zenframework.z8.server.utils.IOUtils;

public class WebResource {
	private final String path;
	private final File resourceBase;

	private File file;
	private InputStream in;

	public WebResource(String path, File resourceBase) throws IOException {
		this.path = path;
		this.resourceBase = resourceBase;
	}

	public String getPath() {
		return path;
	}

	public String getName() {
		return file.getName();
	}

	public boolean isFolder() {
		return file.isDirectory();
	}

	public InputStream getInputStream() {
		return in;
	}

	public boolean open() throws IOException {
		file = new File(resourceBase, path);

		if(!file.exists())
			return false;

		in = !file.isDirectory() ? new FileInputStream(file) : null;

		return true;
	}

	public void evaluate(IProcessor processor, Map<String, Object> bindings) throws IOException {
		in = new ByteArrayInputStream(processor.evaluate(IOUtils.readText(in), bindings).getBytes());
	}
}
