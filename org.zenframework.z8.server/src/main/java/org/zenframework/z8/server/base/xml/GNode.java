package org.zenframework.z8.server.base.xml;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import org.zenframework.z8.server.base.file.FileInfo;
import org.zenframework.z8.server.engine.RmiSerializable;
import org.zenframework.z8.server.rmi.RmiIO;
import org.zenframework.z8.server.types.encoding;

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

	private byte[] deflate(byte[] bytes) {
		if(bytes == null)
			return null;
		
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		Deflater deflater = new Deflater();
		deflater.setInput(bytes);
		deflater.finish();

		byte[] buffer = new byte[32768];
		while(!deflater.finished()) {
			int count = deflater.deflate(buffer);
			outputStream.write(buffer, 0, count);
		}

		return outputStream.toByteArray();
	}

	private byte[] inflate(byte[] bytes) {
		if(bytes == null)
			return null;
		
		Inflater inflater = new Inflater();
		inflater.setInput(bytes);

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		
		byte[] buffer = new byte[32768];

		while(!inflater.finished()) {
			try {
				int count = inflater.inflate(buffer);
				outputStream.write(buffer, 0, count);
			} catch(DataFormatException e) {
				throw new RuntimeException(e);
			}
		}

		return outputStream.toByteArray();
	}
	
    private void writeObject(ObjectOutputStream out)  throws IOException {
    	serialize(out);
    }
    
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
    	deserialize(in);
    }

    public void serialize(ObjectOutputStream out) throws IOException {
		out.writeLong(serialVersionUID);
		
		RmiIO.writeBytes(out, deflate(content));

		out.writeInt(files != null ? files.size() : -1);
		if(files != null) {
			for(FileInfo file : files) 
				out.writeObject(file);
		}

		out.writeInt(attributes != null ? attributes.size() : -1);
		if(attributes != null) {
			for(String key : attributes.keySet()) {
				RmiIO.writeString(out, key);
				RmiIO.writeString(out, attributes.get(key));
			}
		}
	}
	
	public void deserialize(ObjectInputStream in) throws IOException, ClassNotFoundException {
		@SuppressWarnings("unused")
		long version = in.readLong();
		
		content = inflate(RmiIO.readBytes(in));

		int count = in.readInt();
		if(count != -1) {
			files = new ArrayList<FileInfo>();
			for(int i = 0; i < count; i++)
				files.add((FileInfo)in.readObject());
		}
		
		count = in.readInt();
		if(count != -1) {
			attributes = new HashMap<String, String>();
			for(int i = 0; i < count; i++) {
				String key = RmiIO.readString(in);
				String value = RmiIO.readString(in);
				attributes.put(key, value);
			}
		}
	}
}
