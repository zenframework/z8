package org.zenframework.z8.compiler.resources;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.DefaultHandler;

// org.zenframework.z8.server.resources.NlsHandler <-> org.zenframework.z8.compiler.resources.NlsHandler
public class NlsHandler extends DefaultHandler {
	private final Map<Filter, Properties> targets;
	private String currentKey;
	private final StringBuilder currentValue = new StringBuilder();
	private final Map<Filter, Boolean> cache = new HashMap<>();
	private AttributesImpl propertiesAttributes = null;

	static public String PROPERTIES = "properties";
	static public String ENTRY = "entry";


	@FunctionalInterface
	public interface Filter {
		static public String SERVER = "server";
		static public String CLIENT = "client";

		static public String TRUE = Boolean.toString(true);
		static public String FALSE = Boolean.toString(false);

		public boolean accept(NlsHandler handler, Attributes attributes);
	}


	public NlsHandler(File file, Map<Filter, Properties> targets) {
		this.targets = targets;
	}

	public NlsHandler(Filter filter, Properties properties) {
		this.targets = new HashMap<>();
		this.targets.put(filter, properties);
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) {
		if (PROPERTIES.equals(qName)) {
			setPropertiesAttributes(attributes);
		} else if (ENTRY.equals(qName)) {
			currentKey = attributes.getValue("key");
			currentValue.setLength(0);
			cache.clear();
			for (Filter filter : targets.keySet()) {
				cache.put(filter, filter.accept(this, attributes));
			}
		}
	}

	@Override
	public void characters(char[] ch, int start, int length) {
		if (currentKey != null) {
			currentValue.append(ch, start, length);
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) {
		if (ENTRY.equals(qName) && currentKey != null) {
			String value = currentValue.toString();
			for (Map.Entry<Filter, Properties> entry : targets.entrySet()) {
				if (cache.get(entry.getKey())) {
					entry.getValue().put(currentKey, value);
				}
			}
			currentKey = null;
		}
	}

	private void setPropertiesAttributes(Attributes attributes) {
		if (propertiesAttributes != null)
			throw new RuntimeException("Properties already defined");
		this.propertiesAttributes = new AttributesImpl(attributes);
	}

	public Attributes getPropertiesAttributes() {
		return this.propertiesAttributes;
	}

	static public Filter newAttributeFilter(String key, String value) {
		return new Filter() {
			public boolean accept(NlsHandler handler, Attributes attributes) {
				Attributes propertiesAttributes = handler.getPropertiesAttributes();
				String propertiesValue = propertiesAttributes != null ? propertiesAttributes.getValue(key) : null;

				String attributeValue = attributes.getValue(key);

				return value.equalsIgnoreCase(attributeValue) || (attributeValue == null && value.equalsIgnoreCase(propertiesValue));
			}
		};
	}
}
