package org.zenframework.z8.server.base.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemHeaders;

public class InputOnlyFileItem implements FileItem {
	private static final long serialVersionUID = -2878883966271701862L;

	private File file;
	private InputStream stream;
	private String name;

	public InputOnlyFileItem(File file, String name) {
		this.file = file;
		this.name = name;
	}

	public InputOnlyFileItem(InputStream stream, String name) {
		this.stream = stream;
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return stream != null ? stream : new FileInputStream(file);
	}

	@Override
	public FileItemHeaders getHeaders() {
		return null;
	}

	@Override
	public void setHeaders(FileItemHeaders headers) {
	}

	@Override
	public String getContentType() {
		return null;
	}

	@Override
	public boolean isInMemory() {
		return false;
	}

	@Override
	public long getSize() {
		try {
			return stream != null ? stream.available() : file.length();
		} catch(Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public byte[] get() {
		return null;
	}

	@Override
	public String getString(String encoding) throws UnsupportedEncodingException {
		return null;
	}

	@Override
	public String getString() {
		return null;
	}

	@Override
	public void write(File file) throws Exception {
	}

	@Override
	public void delete() {
	}

	@Override
	public String getFieldName() {
		return null;
	}

	@Override
	public void setFieldName(String name) {
	}

	@Override
	public boolean isFormField() {
		return false;
	}

	@Override
	public void setFormField(boolean state) {
	}

	@Override
	public OutputStream getOutputStream() throws IOException {
		return null;
	}
}
