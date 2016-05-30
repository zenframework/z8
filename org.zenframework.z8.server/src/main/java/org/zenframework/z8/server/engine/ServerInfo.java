package org.zenframework.z8.server.engine;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class ServerInfo implements RmiSerializable, Serializable {

	private static final long serialVersionUID = 5011706173964296365L;

	private IServer server;
	private String id;
	private String url;

	public ServerInfo() {
	}
	
	public ServerInfo(IServer server, String id, String url) {
		this.server = server;
		this.id = id;
		this.url = url;
	}

	public IServer getServer() {
		return server;
	}

	public IApplicationServer getApplicationServer() {
		return (IApplicationServer) server;
	}

	public String getId() {
		return id;
	}

	public String getUrl() {
		return url;
	}

    private void writeObject(ObjectOutputStream out)  throws IOException {
    	serialize(out);
    }
    
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
    	deserialize(in);
    }

	@Override
	public void serialize(ObjectOutputStream out) throws IOException {
		out.writeLong(serialVersionUID);
		
		RmiIO.writeString(out, id);
		RmiIO.writeString(out, url);
	}
	
	@Override
	public void deserialize(ObjectInputStream in) throws IOException, ClassNotFoundException {	
		@SuppressWarnings("unused")
		long version = in.readLong();
		
		id = RmiIO.readString(in);
		url = RmiIO.readString(in);
	}
}
