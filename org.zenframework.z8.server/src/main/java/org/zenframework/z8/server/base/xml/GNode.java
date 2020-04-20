package org.zenframework.z8.server.base.xml;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.apache.commons.fileupload.FileItem;
import org.zenframework.z8.server.engine.RmiIO;
import org.zenframework.z8.server.engine.RmiSerializable;
import org.zenframework.z8.server.request.ContentType;
import org.zenframework.z8.server.types.file;
import org.zenframework.z8.server.utils.IOUtils;
import org.zenframework.z8.server.utils.NumericUtils;

public class GNode implements RmiSerializable, Serializable {
	private static final long serialVersionUID = 6229467644994428114L;

	private Map<String, String> attributes;
	private List<file> files;
	private InputStream in;
	private ContentType contentType;

	public GNode() {
		this.contentType = ContentType.Text;
	}

	public GNode(InputStream in, ContentType contentType) {
		this.in = in;
		this.contentType = contentType;
	}

	public GNode(Map<String, String> attributes, List<file> files) {
		this.attributes = attributes;
		this.files = files;
		this.contentType = ContentType.Text;
	}

	public Map<String, String> getAttributes() {
		return attributes;
	}

	public InputStream getInputStream() {
		return in;
	}

	public List<file> getFiles() {
		return files;
	}

	public ContentType getContentType() {
		return contentType;
	}

	private void writeObject(ObjectOutputStream out) throws IOException {
		serialize(out);
	}

	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		deserialize(in);
	}

	@Override
	public void serialize(ObjectOutputStream out) throws IOException {
		RmiIO.writeLong(out, serialVersionUID);

		ByteArrayOutputStream bytes = new ByteArrayOutputStream(32 * NumericUtils.Kilobyte);
		ObjectOutputStream objects = new ObjectOutputStream(bytes);

		RmiIO.writeString(objects, contentType.toString());
		RmiIO.writeBoolean(objects, in != null);

		if(in != null) {
			long size = in.available();
			RmiIO.writeLong(objects, size);

			try {
				IOUtils.copyLarge(in, objects, size, false);
			} finally {
				IOUtils.closeQuietly(in);
			}
		}

		objects.writeObject(attributes);
		objects.close();

		RmiIO.writeBytes(out, IOUtils.zip(bytes.toByteArray()));
		out.writeObject(files);
	}

	@Override
	@SuppressWarnings("unchecked")
	public void deserialize(ObjectInputStream in) throws IOException, ClassNotFoundException {
		@SuppressWarnings("unused")
		long version = RmiIO.readLong(in);

		ByteArrayInputStream bytes = new ByteArrayInputStream(IOUtils.unzip(RmiIO.readBytes(in)));
		ObjectInputStream objects = new ObjectInputStream(bytes);

		contentType = ContentType.fromString(RmiIO.readString(objects));

		if(RmiIO.readBoolean(objects)) {
			long size = RmiIO.readLong(objects);

			FileItem fileItem = file.createFileItem();
			OutputStream out = fileItem.getOutputStream();

			try {
				IOUtils.copyLarge(objects, out, size, false);
			} finally {
				IOUtils.closeQuietly(objects);
			}

			this.in = fileItem.getInputStream();
		}

		attributes = (Map<String, String>)objects.readObject();

		objects.close();

		files = (List<file>)in.readObject();
	}
}
