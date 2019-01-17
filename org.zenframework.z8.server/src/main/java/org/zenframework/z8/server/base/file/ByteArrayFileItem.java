package org.zenframework.z8.server.base.file;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemHeaders;

public class ByteArrayFileItem implements FileItem {
	private static final long serialVersionUID = 3295278729911145232L;

	private final byte[] content;
	private final String name;

	public ByteArrayFileItem(byte[] content, String name) {
		this.content = content;
		this.name = name;
	}

	@Override
	public FileItemHeaders getHeaders() {
		return null;
	}

	@Override
	public void setHeaders(FileItemHeaders headers) {}

	@Override
	public InputStream getInputStream() throws IOException {
		return new ByteArrayInputStream(content);
	}

	@Override
	public String getContentType() {
		return null;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean isInMemory() {
		return true;
	}

	@Override
	public long getSize() {
		return content.length;
	}

	@Override
	public byte[] get() {
		return content;
	}

	@Override
	public String getString(String encoding) throws UnsupportedEncodingException {
		return new String(content, encoding);
	}

	@Override
	public String getString() {
		return new String(content);
	}

	@Override
	public void write(File file) throws Exception {}

	@Override
	public void delete() {}

	@Override
	public String getFieldName() {
		return null;
	}

	@Override
	public void setFieldName(String name) {}

	@Override
	public boolean isFormField() {
		return false;
	}

	@Override
	public void setFormField(boolean state) {}

	@Override
	public OutputStream getOutputStream() throws IOException {
		return null;
	}

}
