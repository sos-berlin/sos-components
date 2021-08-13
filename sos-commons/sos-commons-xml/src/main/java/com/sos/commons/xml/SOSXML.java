package com.sos.commons.xml;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.sos.commons.xml.exception.SOSXMLDoctypeException;
import com.sos.commons.xml.exception.SOSXMLXPathException;

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
                throw new SOSXMLDoctypeException("A DOCTYPE was passed into the XML document", e);
            }
            throw e;
        } catch (IOException e) {
            // XXE that points to a file that doesn't exist
            throw new IOException("IOException occurred, XXE may still possible: " + e.getMessage(), e);
        }
    }

    public static List<Element> getChildElemens(Node node) {
        return getChildElemens(node, null);
    }

    public static List<Element> getChildElemens(Node node, String childName) {
        if (node == null) {
            return null;
        }
        List<Element> result = new ArrayList<>();
        NodeList nl = node.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                if (childName == null) {
                    result.add((Element) n);
                } else {
                    if (n.getNodeName().equals(childName)) {
                        result.add((Element) n);
                    }
                }
            }
        }
        return result;
    }

    public static Node getChildNode(Node parent, String childName) {
        return getChildNode((Element) parent, childName);
    }

    public static Node getChildNode(Element parent, String childName) {
        NodeList nl = parent.getElementsByTagName(childName);
        return nl == null || nl.getLength() == 0 ? null : nl.item(0);
    }

    public static String getChildNodeValue(Node parent, String childName) {
        return getChildNodeValue((Element) parent, childName);
    }

    public static String getChildNodeValue(Element parent, String childName) {
        return getValue(getChildNode(parent, childName));
    }

    public static String getValue(Element el) {
        return el == null ? null : el.getTextContent();
    }

    public static String getValue(Node n) {
        return n == null ? null : n.getTextContent();
    }

    public static String getTrimmedValue(Element n) {
        return getTrimmedValue((Node) n);
    }

    public static String getTrimmedValue(Node n) {
        String val = getValue(n);
        return val == null ? null : val.trim();
    }

    public static SOSXMLXPath newXPath() {
        return new SOSXML().new SOSXMLXPath();
    }

    public static DocumentBuilder getDocumentBuilder() throws ParserConfigurationException {
        return getDocumentBuilder(false);
    }
    
    public static DocumentBuilder getDocumentBuilder(boolean namespaceAware) throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);
        factory.setNamespaceAware(namespaceAware);
        factory.setValidating(false);

        DocumentBuilder builder = factory.newDocumentBuilder();
        builder.setEntityResolver((publicId, systemId) -> new InputSource(new StringReader("")));
        return builder;
    }

    public class SOSXMLXPath {

        private XPath xpath = XPathFactory.newInstance().newXPath();
        
        public Node selectNode(Element element, String expression) throws SOSXMLXPathException {
            return selectNode((Node) element, expression);
        }

        public Node selectNode(Node node, String expression) throws SOSXMLXPathException {
            if (node == null) {
                throw new SOSXMLXPathException(String.format("[%s]node is null", expression));
            }
            try {
                return (Node) xpath.compile(expression).evaluate(node, XPathConstants.NODE);
            } catch (Throwable e) {
                throw new SOSXMLXPathException(String.format("[%s][%s]%s", expression, node.getNodeName(), e.toString()), e);
            }
        }

        public NodeList selectNodes(Node node, String expression) throws SOSXMLXPathException {
            if (node == null) {
                throw new SOSXMLXPathException(String.format("[%s]node is null", expression));
            }
            try {
                return (NodeList) xpath.compile(expression).evaluate(node, XPathConstants.NODESET);
            } catch (Throwable e) {
                throw new SOSXMLXPathException(String.format("[%s][%s]%s", expression, node.getNodeName(), e.toString()), e);
            }
        }
    }
}
