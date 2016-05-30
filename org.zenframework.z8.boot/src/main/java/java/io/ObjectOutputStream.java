package java.io;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.zenframework.z8.rmi.ObjectIO;

public class ObjectOutputStream extends DataOutputStream implements ObjectOutput {

	private static final Logger LOG = Logger.getLogger(ObjectOutputStream.class.getName());

	static {
		LOG.log(Level.INFO, "Z8 java.io.ObjectOutputStream is being used");
	}

	public ObjectOutputStream(OutputStream out) throws IOException {
		super(out);
	}

	public void writeObject(Object object) throws IOException {
		Object replacement = replaceObject(object);
		ObjectIO.write(this, replacement, object);
	}

    protected Object replaceObject(Object obj) throws IOException {
        return obj;
    }

    public void useProtocolVersion(int version) throws IOException {
	}

	protected boolean enableReplaceObject(boolean enable) throws SecurityException {
		return false;
	}
}
