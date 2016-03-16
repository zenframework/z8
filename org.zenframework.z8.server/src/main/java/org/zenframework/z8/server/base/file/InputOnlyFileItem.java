package org.zenframework.z8.server.base.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemHeaders;

public class InputOnlyFileItem implements FileItem
{
	private static final long serialVersionUID = -2878883966271701862L;

	private File file;
	private String name;
	
	public InputOnlyFileItem(File file, String name) {
		this.file = file;
		this.name = name;
	}
	
	public String getName() {
		return name;
	}

	public InputStream getInputStream() throws IOException {
		return new FileInputStream(file);
	}

	public FileItemHeaders getHeaders() {
		return null;
	}

	public void setHeaders(FileItemHeaders headers) {
	}

	public String getContentType() {
		return null;
	}

	public boolean isInMemory() {
		return false;
	}

	public long getSize() {
		return file.length();
	}

	public byte[] get() {
		return null;
	}

	public String getString(String encoding) throws UnsupportedEncodingException {
		return null;
	}

	public String getString() {
		return null;
	}

	public void write(File file) throws Exception {
	}

	public void delete() {
	}

	public String getFieldName() {
		return null;
	}

	public void setFieldName(String name) {
	}

	public boolean isFormField() {
		return false;
	}

	public void setFormField(boolean state) {
	}

	public OutputStream getOutputStream() throws IOException {
		return null;
	}
}
