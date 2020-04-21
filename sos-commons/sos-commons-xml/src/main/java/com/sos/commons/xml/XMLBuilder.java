package com.sos.commons.xml;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.sos.commons.xml.exception.SOSDoctypeException;

public class XMLBuilder {

    private Element root;

    public XMLBuilder(String name) {
        this.root = DocumentHelper.createElement(name);
    }

    public Element getRoot() {
        return this.root;
    }

    public static Element create(String name) {
        return new XMLBuilder(name).getRoot();
    }

    public static Document parse(String xmlString) throws DocumentException, SAXException, IOException, SOSDoctypeException {
        return parse(new StringReader(xmlString));
    }

    public static Document parse(Reader reader) throws DocumentException, SAXException, IOException, SOSDoctypeException {
        SAXReader saxReader = createSaxReader();
        Document doc;
        try {
            doc = saxReader.read(reader);
        } catch (DocumentException e) {
            Throwable nested = e.getNestedException();
            if (nested != null && SAXParseException.class.isInstance(nested) && nested.getMessage().toUpperCase().contains("DOCTYPE")) {
                // On Apache, this should be thrown when disallowing DOCTYPE
                throw new SOSDoctypeException("A DOCTYPE was passed into the XML document", e);
            } else if (nested != null && IOException.class.isInstance(nested)) {
                // XXE that points to a file that doesn't exist
                throw new IOException("IOException occurred, XXE may still possible: " + e.getMessage(), e);
            } else {
                throw e;
            }
        }
        return doc;
    }

    public static Document parse(InputStream stream) throws DocumentException, SAXException, IOException {
        SAXReader saxReader = createSaxReader();
        Document doc;
        try {
            doc = saxReader.read(stream);
        } catch (DocumentException e) {
            Throwable nested = e.getNestedException();
            if (nested != null && SAXParseException.class.isInstance(nested) && nested.getMessage().toUpperCase().contains("DOCTYPE")) {
                // On Apache, this should be thrown when disallowing DOCTYPE
                throw new SAXException("A DOCTYPE was passed into the XML document", e);
            } else if (nested != null && IOException.class.isInstance(nested)) {
                // XXE that points to a file that doesn't exist
                throw new IOException("IOException occurred, XXE may still possible: " + e.getMessage(), e);
            } else {
                throw e;
            }
        }
        return doc;
    }

    public Element addElement(String name) {
        return this.root.addElement(name);
    }

    public Element addAttribute(String key, String value) {
        return this.root.addAttribute(key, value);
    }

    public void add(Element elem) {
        this.root.add(elem);
    }

    public String asXML() {
        return this.root.asXML();
    }

    public static SAXReader createSaxReader() throws SAXException {
        SAXReader saxReader = new SAXReader();
        // saxReader.setEncoding("UTF-8");
        saxReader.setIncludeExternalDTDDeclarations(false);
        saxReader.setIncludeInternalDTDDeclarations(false);
        saxReader.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        saxReader.setFeature("http://xml.org/sax/features/external-general-entities", false);
        saxReader.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        saxReader.setEntityResolver((publicId, systemId) -> new InputSource(new StringReader("")));
        saxReader.setValidation(false);
        saxReader.setStripWhitespaceText(true);
        saxReader.setIgnoreComments(true);
        return saxReader;
    }

}
