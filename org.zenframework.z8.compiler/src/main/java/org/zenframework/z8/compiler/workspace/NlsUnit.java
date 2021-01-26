package org.zenframework.z8.compiler.workspace;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.eclipse.core.resources.IResource;
import org.zenframework.z8.compiler.file.File;
import org.zenframework.z8.compiler.file.FileException;
import org.zenframework.z8.compiler.util.Set;

public class NlsUnit extends Resource {
	private Properties properties = new Properties();
	private String locale;

	private boolean buildPending = true;

	private Set<CompilationUnit> consumers;

	public NlsUnit(Resource parent, IResource resource) {
		super(parent, resource);
	}

	public void contentChanged() {
		if(Project.isIdle()) {
			buildPending = true;
			updateDependencies();
		}
	}

	protected void updateDependencies() {
		CompilationUnit[] consumers = getConsumers();

		for(CompilationUnit consumer : consumers) {
			consumer.setChanged(true);
			consumer.updateDependencies();
		}

		cleanup();
	}

	public CompilationUnit[] getConsumers() {
		if(consumers == null) {
			return new CompilationUnit[0];
		}

		return consumers.toArray(new CompilationUnit[consumers.size()]);
	}

	public void addConsumer(CompilationUnit consumer) {
		if(consumers == null) {
			consumers = new Set<CompilationUnit>();
		}
		consumers.add(consumer);
	}

	public String getLocale() {
		return locale;
	}

	public boolean compareLocale(String locale) {
		return true;
	}

	public String getValue(String key) {
		return (String)properties.get(key);
	}

	private void cleanup() {
		consumers = null;
		properties.clear();
		clearMessages();
	}

	public void parse() {
		if(!buildPending) {
			return;
		}

		buildPending = false;

		InputStream in = null;
		try {
			in = File.fromPath(getAbsolutePath()).inputStream();
			properties.loadFromXML(in);
		} catch(FileNotFoundException e) {
			error(new FileException(getPath(), e.getMessage()));
		} catch(IOException e) {
			error(new FileException(getPath(), e.getMessage()));
		} catch (FileException e) {
			error(e);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {}
			}
		}

		reportMessages();
	}
}
