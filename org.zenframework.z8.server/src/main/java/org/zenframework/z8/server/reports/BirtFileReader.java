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
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.zenframework.z8.server.resources.Resources;

public class BirtFileReader {
	private Document doc = null;
	XPath xpath = null;
	XPathExpression expr;

	public BirtFileReader(File file) {
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			doc = db.parse(file);
		} catch(IOException e) {
			throw new RuntimeException(Resources.format("Exception.fileNotFound", file.getName()), e);
		} catch(Exception e) {
			throw new RuntimeException(e);
		}

		XPathFactory factory = XPathFactory.newInstance();
		xpath = factory.newXPath();
	}

	private List<String> getResult(XPathExpression expr) {
		List<String> ret = new ArrayList<String>();
		try {
			Object result = expr.evaluate(doc, XPathConstants.NODESET);
			NodeList nodes = (NodeList)result;
			for(int i = 0; i < nodes.getLength(); i++) {
				Node node = nodes.item(i);
				ret.add(node.getNodeValue());
			}
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
		return ret;
	}

	public String getDataSourceName() {
		String strExpr = "//oda-data-source/@name";
		try {
			expr = xpath.compile(strExpr);
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
		return getResult(expr).size() != 0 ? getResult(expr).iterator().next() : "";
	}

	public List<String> getDataSets() {
		String strExpr = "//oda-data-set/xml-property[@name='queryText']/text()";

		try {
			expr = xpath.compile(strExpr);
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
		return getResult(expr);
	}

	public String getDataSetName(int datasetNum) {
		String strExpr = "//oda-data-set[" + (datasetNum + 1) + "]/xml-property[@name='queryText']/text()";

		try {
			expr = xpath.compile(strExpr);
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
		return getResult(expr).size() != 0 ? getResult(expr).iterator().next() : "";
	}
}
