package org.zenframework.z8.compiler.util;

import java.io.Closeable;

// The same org.zenframework.z8.server.utils.IOUtils
public class IOUtil {

	static public void closeQuietly(Closeable closable) {
		try {
			if(closable != null)
				closable.close();
		} catch(Throwable e) {
		}
	}
}
