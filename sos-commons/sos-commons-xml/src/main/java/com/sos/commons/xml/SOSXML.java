package com.sos.commons.xml;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.sos.commons.xml.exception.SOSDoctypeException;

public class SOSXML {

    public static Document parse(StringBuilder xml) throws Exception {
        return parse(xml.toString());
    }

    public static Document parse(String xml) throws Exception {
        return parse(new InputSource(new StringReader(xml)));
    }

    public static Document parse(InputStream is) throws Exception {
        return parse(new InputSource(is));
    }

    public static Document parse(Path path) throws Exception {
        return parse(new InputSource(Files.newInputStream(path)));
    }

    public static Document parse(InputSource is) throws Exception {
        try {
            return getDocumentBuilder().parse(is);
        } catch (SAXException e) {
            if (e.getMessage().toUpperCase().contains("DOCTYPE")) {
                throw new SOSDoctypeException("A DOCTYPE was passed into the XML document", e);
            }
            throw e;
        } catch (IOException e) {
            // XXE that points to a file that doesn't exist
            throw new IOException("IOException occurred, XXE may still possible: " + e.getMessage(), e);
        }
    }

    public static SOSXMLXPath newXPath() {
        return new SOSXML().new SOSXMLXPath();
    }

    private static DocumentBuilder getDocumentBuilder() throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);
        factory.setNamespaceAware(false);
        factory.setValidating(false);

        DocumentBuilder builder = factory.newDocumentBuilder();
        builder.setEntityResolver((publicId, systemId) -> new InputSource(new StringReader("")));
        return builder;
    }

    public class SOSXMLXPath {

        private XPath xpath = XPathFactory.newInstance().newXPath();

        public NodeList selectNodes(Document doc, String expression) throws XPathExpressionException {
            return selectNodes((Node) doc.getDocumentElement(), expression);
        }

        public NodeList selectNodes(Node node, String expression) throws XPathExpressionException {
            return (NodeList) xpath.compile(expression).evaluate(node, XPathConstants.NODESET);
        }

    }
}
