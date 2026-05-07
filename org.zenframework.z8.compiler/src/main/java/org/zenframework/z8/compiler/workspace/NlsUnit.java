package org.zenframework.z8.compiler.workspace;

import java.io.InputStream;
import java.util.Properties;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.eclipse.core.resources.IResource;
import org.zenframework.z8.compiler.file.File;
import org.zenframework.z8.compiler.resources.NlsHandler;
import org.zenframework.z8.compiler.resources.NlsHandler.Filter;
import org.zenframework.z8.compiler.util.IOUtil;
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

		SAXParserFactory factory = SAXParserFactory.newInstance();
		NlsHandler handler = new NlsHandler(NlsHandler.newAttributeFilter(Filter.SERVER, Filter.TRUE), properties);

		try {
			in = File.fromPath(getAbsolutePath()).inputStream();
			SAXParser parser = factory.newSAXParser();
			parser.parse(in, handler);
		} catch (Exception e) {
			error(e);
		} finally {
			IOUtil.closeQuietly(in);
		}

		reportMessages();
	}
}
