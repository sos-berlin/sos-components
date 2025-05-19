package com.sos.commons.xml;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.sos.commons.util.SOSPath;
import com.sos.commons.util.SOSString;
import com.sos.commons.xml.exception.SOSXMLDoctypeException;
import com.sos.commons.xml.exception.SOSXMLXPathException;

public class SOSXML {

    public static final String DEFAULT_XML_DECLARATION = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";

    public static Document parse(StringBuilder xml) throws Exception {
        return parse(xml, false);
    }

    public static Document parse(StringBuilder xml, boolean namespaceAware) throws Exception {
        return parse(xml.toString(), namespaceAware);
    }

    public static Document parse(String xml) throws Exception {
        return parse(xml, false);
    }

    public static Document parse(String xml, boolean namespaceAware) throws Exception {
        return parse(new InputSource(new StringReader(xml)), namespaceAware);
    }

    public static Document parse(InputStream is) throws Exception {
        return parse(is, false);
    }

    public static Document parse(InputStream is, boolean namespaceAware) throws Exception {
        return parse(new InputSource(is), namespaceAware);
    }

    public static Document parse(Path path) throws Exception {
        return parse(path, false);
    }

    public static Document parse(Path path, boolean namespaceAware) throws Exception {
        return parse(new InputSource(Files.newInputStream(path)), namespaceAware);
    }

    public static Document parse(InputSource is, boolean namespaceAware) throws Exception {
        try {
            return getDocumentBuilder(namespaceAware).parse(is);
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

    public static List<Element> getChildElemens(Node parentNode) {
        return getChildElemens(parentNode, null, ArrayList::new);
    }

    public static List<Element> getChildElemens(Node parentNode, String childName) {
        return getChildElemens(parentNode, childName, ArrayList::new);
    }

    /** Examples: getChildElemens(parentNode, ArrayList::new)
     * 
     * @param <C>
     * @param parentNode
     * @param collectionFactory
     * @return */
    public static <C extends Collection<Element>> C getChildElemens(Node parentNode, Supplier<C> collectionFactory) {
        return getChildElemens(parentNode, null, collectionFactory);
    }

    /** Examples: getChildElemens(parentNode, childName, ArrayList::new)
     * 
     * @param <C>
     * @param parentNode
     * @param childName
     * @param collectionFactory
     * @return */
    public static <C extends Collection<Element>> C getChildElemens(Node parentNode, String childName, Supplier<C> collectionFactory) {
        if (parentNode == null) {
            return null;
        }
        C result = collectionFactory.get();
        NodeList nl = parentNode.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                if (childName == null || n.getNodeName().equals(childName)) {
                    result.add((Element) n);
                }
            }
        }
        return result;
    }

    public static Map<String, Element> getChildElementsMap(Node parentNode) {
        return getChildElementsMap(parentNode, null, HashMap::new);
    }

    public static Map<String, Element> getChildElementsMap(Node parentNode, String childName) {
        return getChildElementsMap(parentNode, childName, HashMap::new);
    }

    /** Examples: getChildElementsMap(parentNode, childName, LinkedHashMap::new)
     * 
     * @param <M>
     * @param parentNode
     * @param childName
     * @param mapFactory
     * @return */
    public static <M extends Map<String, Element>> M getChildElementsMap(Node parentNode, String childName, Supplier<M> mapFactory) {
        if (parentNode == null) {
            return null;
        }

        M result = mapFactory.get();
        NodeList nl = parentNode.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                if (childName == null || n.getNodeName().equals(childName)) {
                    result.put(n.getNodeName(), (Element) n);
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

    public static String getAttributeValue(Node n, String attrName) {
        if (n == null || SOSString.isEmpty(attrName)) {
            return null;
        }
        NamedNodeMap m = n.getAttributes();
        if (m == null || m.getLength() == 0) {
            return null;
        }
        return getTrimmedValue(m.getNamedItem(attrName));
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

    public static void toFile(Path outputFile, Node node) throws Exception {
        if (outputFile == null || node == null) {
            return;
        }
        SOSPath.append(outputFile, DEFAULT_XML_DECLARATION, System.lineSeparator());
        SOSPath.append(outputFile, nodeToString(node));
    }

    public static String nodeToString(Node node) throws Exception {
        return nodeToString(node, true, 4);
    }

    public static String nodeToString(Node node, boolean omitXmlDeclaration, int indentAmount) throws Exception {
        removeWhitespaceNodes(node);
        Transformer t = TransformerFactory.newInstance().newTransformer();
        // t.setOutputProperty(OutputKeys.CDATA_SECTION_ELEMENTS, "text");
        t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, omitXmlDeclaration ? "yes" : "no");
        if (indentAmount > 0) {
            t.setOutputProperty(OutputKeys.INDENT, "yes");
            t.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", String.valueOf(indentAmount));
        } else {
            t.setOutputProperty(OutputKeys.INDENT, "no");
        }

        StringWriter sw = new StringWriter();
        t.transform(new DOMSource(node), new StreamResult(sw));
        return sw.toString().trim();
    }

    /** Transforms XML content using an XSL stylesheet and returns the result as a string.
     *
     * <p>
     * <b>Note on CDATA sections:</b> CDATA is not required for correct XML transformation.<br/>
     * During transformation, special characters (e.g., &, <, >) are automatically escaped,<br/>
     * and both escaped content and CDATA are treated identically by XML parsers.
     *
     * <p>
     * <b>When to use CDATA:</b> CDATA is only needed if:
     * <ul>
     * <li>Target systems explicitly require CDATA (e.g., legacy APIs or XHTML script/style blocks)</li>
     * <li>You want to preserve human readability in large text blocks</li>
     * <li>You are embedding raw markup or code (e.g., HTML, JS) that shouldn't be escaped</li>
     * </ul>
     *
     * <p>
     * <b>How to enable CDATA in transformation:</b>
     * <ul>
     * <li><b>In XSLT:</b> use the <code>&lt;xsl:output&gt;</code> element with <code>cdata-section-elements</code>:
     * 
     * <pre>{@code
     * <xsl:output method="xml" indent="yes" cdata-section-elements="MyElement1 MyElement2"/>
     * }</pre>
     * 
     * </li>
     * <li><b>In Java:</b> CDATA sections must be declared in the XSLT. Java's Transformer API does not provide a direct method to enforce CDATA.</li>
     * </ul>
     *
     * @param xmlContent the XML content to transform
     * @param xslContent the XSL stylesheet content to apply
     * @return the result of the transformation as a string
     * @throws Exception if transformation fails */
    public static String transformXMLWithXSL(String xmlContent, String xslContent) throws Exception {
        return transformXMLWithXSL(xmlContent, xslContent, true, 4);
    }

    public static String transformXMLWithXSL(String xmlContent, String xslContent, boolean omitXmlDeclaration, int indentAmount) throws Exception {
        Transformer t = TransformerFactory.newInstance().newTransformer(new StreamSource(new StringReader(xslContent)));
        t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, omitXmlDeclaration ? "yes" : "no");
        if (indentAmount > 0) {
            t.setOutputProperty(OutputKeys.INDENT, "yes");
            t.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", String.valueOf(indentAmount));
        } else {
            t.setOutputProperty(OutputKeys.INDENT, "no");
        }

        StringWriter sw = new StringWriter();
        t.transform(new StreamSource(new StringReader(xmlContent)), new StreamResult(sw));
        return sw.toString().trim();
    }

    public static void removeWhitespaceNodes(Node node) {
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.TEXT_NODE && child.getNodeValue().trim().isEmpty()) {
                node.removeChild(child);
                i--;
            } else if (child.hasChildNodes()) {
                removeWhitespaceNodes(child);
            }
        }
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
