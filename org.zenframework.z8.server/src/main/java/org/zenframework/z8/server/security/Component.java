package org.zenframework.z8.server.security;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.zenframework.z8.server.engine.RmiIO;
import org.zenframework.z8.server.engine.RmiSerializable;

public class Component implements RmiSerializable {
    private static final long serialVersionUID = 3995824646818479932L;

    private String id;
    private String className;
    private String title;

    public Component() {
    }
    
    public Component(String id, String className, String title) {
        this.id = id;
        this.className = className;
        this.title = title;
    }

    public String id() {
        return id;
    }

    public String className() {
        return className;
    }

    public String title() {
        return title;
    }

    @Override
    public boolean equals(Object object) {
        if(object == null)
            return false;
        if(object == this)
            return true;
        if(this.getClass() != object.getClass())
            return false;

        Component component = (Component)object;
        if(this.hashCode() == component.hashCode())
            return true;

        return false;
    }

    @Override
    public int hashCode() {
        StringBuffer buffer = new StringBuffer();
        buffer.append(this.className);
        return buffer.toString().hashCode();
    }
    
    @Override
	public void serialize(ObjectOutputStream out) throws IOException {
    	RmiIO.writeLong(out, serialVersionUID);
		
		RmiIO.writeString(out, id);
		RmiIO.writeString(out, className);
		RmiIO.writeString(out, title);
	}
	
    @Override
	public void deserialize(ObjectInputStream in) throws IOException, ClassNotFoundException {	
		@SuppressWarnings("unused")
		long version = RmiIO.readLong(in);
		
		id = RmiIO.readString(in);
		className = RmiIO.readString(in);
		title = RmiIO.readString(in);
	}
}
