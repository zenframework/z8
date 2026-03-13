package org.zenframework.z8.server.types;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.runtime.RCollection;
import org.zenframework.z8.server.types.sql.sql_binary;
import org.zenframework.z8.server.utils.IOUtils;

public class binary extends primary {
	static private final long serialVersionUID = -6993940737401994151L;

	public string name = new string();

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

	public String getName() {
		return name.get();
	}

	public String getBaseName() {
		return file.getBaseName(name.get());
	}

	public String getExtension() {
		return file.getExtension(name.get());
	}

	public binary setName(String name) {
		this.name = new string(name);
		return this;
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
			throw new RuntimeException(e);
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
		return getName();
	}

	public string z8_getName() {
		return name;
	}

	public binary z8_setName(string name) {
		this.name = name;
		return this;
	}

	public string z8_getBaseName() {
		return new string(getBaseName());
	}

	public string z8_getExtension() {
		return new string(getExtension());
	}

	public sql_binary sql_binary() {
		return new sql_binary(this);
	}

	static public binary zip(file file) {
		return zip(file.toFile());
	}

	static public binary zip(File fileOrDirectory) {
		return file.createTempFile().zip(fileOrDirectory).binary();
	}

	static public binary zip(binary[] binaries) {
		return file.createTempFile().zip(binaries).binary();
	}

	public void unzip(File directory) {
		file.unzip(get(), directory.getAbsoluteFile());
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

	static public binary z8_zip(file file) {
		return zip(file.toFile());
	}

	static public binary z8_zip(RCollection<binary> binaries) {
		return zip(binaries.toArray(new binary[0]));
	}

	public void z8_unzip(file directory) {
		file.unzip(get(), directory.getAbsolutePath());
	}
}