package org.zenframework.z8.server.reports;

import java.io.File;
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

import org.zenframework.z8.server.engine.ApplicationServer;

public class ReportBindingFileReader {
    private Document doc = null;
    XPath xpath = null;
    XPathExpression expr;

    public ReportBindingFileReader() {
        File file = new File(new File(ApplicationServer.workingPath(), ReportConstants.DEFAULT_REPORT_FOLDER),
                ReportConstants.REPORT_BINDING_FILENAME);

        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db;
            db = dbf.newDocumentBuilder();
            doc = db.parse(file);

            XPathFactory factory = XPathFactory.newInstance();
            xpath = factory.newXPath();
        }
        catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    private List<ReportInfo> getResult(XPathExpression expr) {
        List<ReportInfo> ret = new ArrayList<ReportInfo>();
        try {
            Object result = expr.evaluate(doc, XPathConstants.NODESET);
            NodeList nodes = (NodeList)result;
            for(int i = 0; i < nodes.getLength(); i++) {
                Node node = nodes.item(i);
                String fileName = node.getAttributes().getNamedItem("name").getNodeValue();
                String displayName = node.getAttributes().getNamedItem("displayName").getNodeValue();
                ret.add(new ReportInfo(fileName, displayName));
            }
        }
        catch(Exception e) {
            throw new RuntimeException(e);
        }
        return ret;
    }

    public List<ReportInfo> getReportTemplateFileNames(String classCanonicalName) {
        String strExpr = "//descendant::class[text()='" + classCanonicalName + "']/..";
        try {
            expr = xpath.compile(strExpr);
        }
        catch(Exception e) {
            throw new RuntimeException(e);
        }
        return getResult(expr);
    }
}
