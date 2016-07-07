package java.io;

import org.zenframework.z8.rmi.ObjectIO;

public class ObjectInputStream extends DataInputStream implements ObjectInput {

	public ObjectInputStream(InputStream in) throws IOException {
		super(in);
	}

	@Override
	public Object readObject() throws IOException, ClassNotFoundException {
		return ObjectIO.read(this);
	}

}
