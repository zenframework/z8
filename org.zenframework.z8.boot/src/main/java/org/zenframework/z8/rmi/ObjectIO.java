package org.zenframework.z8.rmi;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class ObjectIO {

	private static final Logger LOG = Logger.getLogger(ObjectIO.class.getName());

	static private ObjectIO instance = null;

	static public void initialize(ObjectIO instance) {
		if (ObjectIO.instance == null) {
			ObjectIO.instance = instance;
			LOG.log(Level.INFO, "Z8 ObjectIO initialized");
		}
	}

	static public void write(ObjectOutputStream out, Object replacement, Object object) throws IOException {
		instance.writeObject(out, replacement, object);
	}

	static public Object read(ObjectInputStream in) throws IOException, ClassNotFoundException {
		return instance.readObject(in);
	}

	abstract protected void writeObject(ObjectOutputStream out, Object replacement, Object object) throws IOException;
	abstract protected Object readObject(ObjectInputStream in) throws IOException, ClassNotFoundException;

}
