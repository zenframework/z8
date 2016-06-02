package org.zenframework.z8.server.base.xml;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

import org.zenframework.z8.server.base.file.FileInfo;
import org.zenframework.z8.server.engine.RmiIO;
import org.zenframework.z8.server.engine.RmiSerializable;
import org.zenframework.z8.server.types.encoding;
import org.zenframework.z8.server.utils.IOUtils;
import org.zenframework.z8.server.utils.NumericUtils;

public class GNode implements RmiSerializable, Serializable {
	private static final long serialVersionUID = 6229467644994428114L;

	private Map<String, String> attributes;
	private List<FileInfo> files;
	private byte[] content = null;

	public GNode() {
	}

	public GNode(String content) {
		try {
			this.content = content.getBytes(encoding.Default.toString());
		} catch(UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	public GNode(Map<String, String> attributes, List<FileInfo> files) {
		this.attributes = attributes;
		this.files = files;
	}

	public Map<String, String> getAttributes() {
		return attributes;
	}

	public byte[] getContent() {
		return content;
	}

	public List<FileInfo> getFiles() {
		return files;
	}

	private void writeObject(ObjectOutputStream out) throws IOException {
		serialize(out);
	}

	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		deserialize(in);
	}

	public void serialize(ObjectOutputStream out) throws IOException {
		out.writeLong(serialVersionUID);

		ByteArrayOutputStream bytes = new ByteArrayOutputStream(32 * NumericUtils.Kilobyte);
		ObjectOutputStream objects = new ObjectOutputStream(bytes);

		RmiIO.writeBytes(objects, content);

		objects.writeObject(attributes);

		objects.close();

		RmiIO.writeBytes(out, IOUtils.zip(bytes.toByteArray()));

		out.writeObject(files);
	}

	@SuppressWarnings("unchecked")
	public void deserialize(ObjectInputStream in) throws IOException, ClassNotFoundException {
		@SuppressWarnings("unused")
		long version = in.readLong();

		ByteArrayInputStream bytes = new ByteArrayInputStream(IOUtils.unzip(RmiIO.readBytes(in)));
		ObjectInputStream objects = new ObjectInputStream(bytes);

		content = RmiIO.readBytes(objects);

		attributes = (Map<String, String>)objects.readObject();

		objects.close();

		files = (List<FileInfo>)in.readObject();
	}
}
