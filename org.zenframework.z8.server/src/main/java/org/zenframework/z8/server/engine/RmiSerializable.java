package org.zenframework.z8.server.engine;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public interface RmiSerializable {
	public void serialize(ObjectOutputStream stream) throws IOException;
	public void deserialize(ObjectInputStream stream) throws IOException, ClassNotFoundException;
}
