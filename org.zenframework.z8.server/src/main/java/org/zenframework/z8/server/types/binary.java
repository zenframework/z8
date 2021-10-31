package org.zenframework.z8.server.types;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.FilenameUtils;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.types.sql.sql_binary;
import org.zenframework.z8.server.utils.IOUtils;

public class binary extends primary {
	private static final long serialVersionUID = -6993940737401994151L;

	private InputStream stream;

	public binary() {
		this(new byte[0]);
	}

	public binary(byte[] data) {
		set(data);
	}

	public binary(InputStream stream) {
		set(stream);
	}

	public binary(binary binary) {
		set(binary);
	}

	public binary(File file) {
		set(file);
	}

	public binary(string string) {
		set(string != null ? string.getBytes(encoding.Default) : new byte[0]);
	}

	public binary(String s) {
		this(new string(s));
	}

	private void set(byte[] bytes) {
		set(new ByteArrayInputStream(bytes != null ? bytes : new byte[0]));
	}

	private void set(binary binary) {
		this.set(binary.stream);
	}

	private void set(File file) {
		try {
			set(new FileInputStream(file));
		} catch(FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	private void set(InputStream stream) {
		if(this.stream == stream)
			return;

		close();
		this.stream = stream;
	}

	public InputStream get() {
		return stream;
	}

	public byte[] toByteArray() {
		try {
			return IOUtils.read(get());
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void close() {
		try {
			if(stream != null)
				stream.close();
		} catch(IOException e) {
			throw new exception(e);
		} finally {
			stream = null;
		}
	}

	@Override
	public FieldType type() {
		return FieldType.Binary;
	}

	@Override
	public String toDbConstant(DatabaseVendor dbtype) {
		switch(dbtype) {
		case Postgres:
		case Oracle:
			return "null";
		default:
			return "''";
		}
	}

	@Override
	public int compareTo(primary primary) {
		return 0;
	}

	@Override
	public String toString() {
		return "binary value";
	}

	public sql_binary sql_binary() {
		return new sql_binary(this);
	}

	public void unzip(File directory) {
		file.unzip(get(), directory.getAbsoluteFile());
	}

	public static binary zip(file fileOrDirectory) {
		return zip(fileOrDirectory.toFile());
	}

	public static binary zip(File fileOrDirectory) {
		file temp = file.createTempFile(FilenameUtils.getBaseName(fileOrDirectory.getName()), ".zip");
		temp.zip(fileOrDirectory);
		return temp.binary();
	}

	public int length() {
		try {
			return stream.available();
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}

	public integer z8_length() {
		return new integer(length());
	}

	public void z8_unzip(file directory) {
		file.unzip(get(), directory.getAbsolutePath());
	}

	public static binary z8_zip(file fileOrDirectory) {
		return zip(fileOrDirectory.toFile());
	}
}