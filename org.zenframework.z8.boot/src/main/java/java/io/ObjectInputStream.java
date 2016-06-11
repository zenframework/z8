package java.io;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.zenframework.z8.rmi.ObjectIO;

public class ObjectInputStream extends DataInputStream implements ObjectInput {

	private static final Logger LOG = Logger.getLogger(ObjectInputStream.class.getName());

	static {
		LOG.log(Level.INFO, "Z8 java.io.ObjectInputStream is being used");
	}

	public ObjectInputStream(InputStream in) throws IOException {
		super(in);
	}

	@Override
	public Object readObject() throws IOException, ClassNotFoundException {
		return ObjectIO.read(this);
	}

}
