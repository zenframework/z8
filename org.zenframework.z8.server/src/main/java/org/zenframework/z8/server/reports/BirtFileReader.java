package org.zenframework.z8.server.reports;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.zenframework.z8.server.resources.Resources;

public class BirtFileReader {
	private Document document;
	private XPath xpath;
	private XPathExpression expression;

	public BirtFileReader(File file) {
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			document = db.parse(file);
			xpath = XPathFactory.newInstance().newXPath();
		} catch(IOException e) {
			throw new RuntimeException(Resources.format("Exception.fileNotFound", file.getName()), e);
		} catch(Throwable e) {
			throw new RuntimeException(e);
		}
	}

	private List<String> getResult(XPathExpression expression) {
		try {
			List<String> result = new ArrayList<String>();
			NodeList nodes = (NodeList)expression.evaluate(document, XPathConstants.NODESET);
			for(int i = 0; i < nodes.getLength(); i++)
				result.add(nodes.item(i).getNodeValue());
			return result;
		} catch(Throwable e) {
			throw new RuntimeException(e);
		}
	}

	public String getDataSourceName() {
		try {
			expression = xpath.compile("//oda-data-source/@name");
			return getResult(expression).size() != 0 ? getResult(expression).iterator().next() : "";
		} catch(Throwable e) {
			throw new RuntimeException(e);
		}
	}

	public List<String> getDataSets() {
		try {
			expression = xpath.compile("//oda-data-set/xml-property[@name='queryText']/text()");
			return getResult(expression);
		} catch(Throwable e) {
			throw new RuntimeException(e);
		}
	}

	public String getDataSetName(int datasetNum) {
		try {
			expression = xpath.compile("//oda-data-set[" + (datasetNum + 1) + "]/xml-property[@name='queryText']/text()");
			return getResult(expression).size() != 0 ? getResult(expression).iterator().next() : "";
		} catch(Throwable e) {
			throw new RuntimeException(e);
		}
	}
}
