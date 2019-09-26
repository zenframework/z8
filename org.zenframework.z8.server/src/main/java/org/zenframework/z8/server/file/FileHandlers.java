package org.zenframework.z8.server.file;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Collection;
import java.util.Enumeration;
import java.util.LinkedList;

import org.zenframework.z8.server.base.file.FileHandler;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.request.Loader;
import org.zenframework.z8.server.types.file;

public class FileHandlers {

	static private final String FILE_HANDLER = "META-INF/z8.filehandler";

	static private final Collection<IFileHandler> fileHandlers = new LinkedList<IFileHandler>();

	static {
		try {
			Enumeration<URL> resources = FileHandlers.class.getClassLoader().getResources(FILE_HANDLER);
			while (resources.hasMoreElements()) {
				BufferedReader in = new BufferedReader(new InputStreamReader(resources.nextElement().openStream()));
				for (String line = in.readLine(); line != null; line = in.readLine()) {
					String className = line.trim();
					try {
						fileHandlers.add(loadFileHandler(className));
						Trace.log().info("File handler loaded: " + className);
					} catch (Throwable e) {
						Trace.log().error("Couldn't load class '" + className + "'", e);
					}
				}
			}
		} catch (IOException e) {
			Trace.log().error("Couldn't read resources '" + FILE_HANDLER + "'", e);
		} finally {
			fileHandlers.add(new DefaultFileHandler());
		}
	}

	static public IFileHandler getFileHandler(file file) {
		for (IFileHandler handler : fileHandlers)
			if (handler.canHandleRequest(file))
				return handler;
		throw new RuntimeException("Can't handler file " + file);
	}

	static private IFileHandler loadFileHandler(String className) {
		try {
			return (IFileHandler) Class.forName(className).newInstance();
		} catch (Throwable e) {
			return (FileHandler) Loader.getInstance(className);
		}
	}

}
