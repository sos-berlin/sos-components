package com.sos.commons.xml.transform;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Node;

import com.sos.commons.xml.SOSXML;

public class SOSXmlTransformer {

    public static String nodeToString(Node node) throws Exception {
        return nodeToString(node, true, 4);
    }

    public static String nodeToString(Node node, boolean omitXmlDeclaration, int indentAmount) throws Exception {
        SOSXML.removeWhitespaceNodes(node);
        Transformer t = createTransformer(omitXmlDeclaration, indentAmount);

        try (StringWriter sw = new StringWriter()) {
            t.transform(new DOMSource(node), new StreamResult(sw));
            return sw.toString().trim();
        }
    }

    /** Performs a standard XSLT transformation on the provided XML content.
     *
     * <p>
     * This method applies the given XSL stylesheet to the XML input and returns the transformed XML as a String.<br/>
     * It supports options to omit the XML declaration and to indent the output for readability.
     * </p>
     *
     * <p>
     * Use this method if your XSL stylesheet already handles any CDATA sections explicitly, or if CDATA wrapping is not needed.
     * </p>
     *
     * @param xmlContent The input XML as a String.
     * @param xslContent The XSLT stylesheet as a String.
     * @param omitXmlDeclaration If true, omits the XML declaration in output.
     * @param indentAmount Number of spaces for indentation; if zero or less, no indentation.
     * @return The transformed XML as a String.
     * @throws Exception If any transformation error occurs. */
    public static String transformXmlWithXsl(String xmlContent, String xslContent) throws Exception {
        return transformXmlWithXsl(xmlContent, xslContent, true, 4);
    }

    public static String transformXmlWithXsl(String xmlContent, String xslContent, boolean omitXmlDeclaration, int indentAmount) throws Exception {
        Transformer t = createTransformer(omitXmlDeclaration, indentAmount, xslContent);
        try (StringWriter sw = new StringWriter()) {
            t.transform(new StreamSource(new StringReader(xmlContent)), new StreamResult(sw));
            return sw.toString().trim();
        }
    }

    /** Performs an XSLT transformation on the provided XML content, wrapping all text node values in CDATA sections in the output.
     *
     * <p>
     * Because specifying all CDATA elements in XSL stylesheets via <code>cdata-section-elements</code> can be cumbersome or impractical when many elements need
     * CDATA, this method wraps every text node in CDATA sections by intercepting the XML output stream.
     * </p>
     *
     * <p>
     * This approach avoids modifying the XSL and guarantees that all text content is safely enclosed in CDATA, which is useful for XML processors that require
     * literal preservation of special characters.
     * </p>
     *
     * @param xmlContent The input XML as a String.
     * @param xslContent The XSLT stylesheet as a String.
     * @param omitXmlDeclaration If true, omits the XML declaration in output.
     * @param indentAmount Number of spaces for indentation; if zero or less, no indentation.
     * @return The transformed XML as a String with all text enclosed in CDATA sections.
     * @throws Exception If any transformation or streaming error occurs. */
    public static String transformXmlWithXslEncloseTextInCdata(String xmlContent, String xslContent, boolean omitXmlDeclaration, int indentAmount)
            throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        Thread transformThread = null;
        try (PipedOutputStream pipeOut = new PipedOutputStream(); PipedInputStream pipeIn = new PipedInputStream(pipeOut);
                OutputStreamWriter osWriter = new OutputStreamWriter(baos, "UTF-8")) {

            transformThread = new Thread(() -> {
                try {
                    Transformer t = createTransformer(omitXmlDeclaration, indentAmount, xslContent);
                    t.transform(new StreamSource(new StringReader(xmlContent)), new StreamResult(pipeOut));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    try {
                        pipeOut.close();
                    } catch (IOException ignored) {
                    }
                }
            });
            transformThread.start();

            XMLInputFactory inputFactory = XMLInputFactory.newInstance();
            XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();

            XMLStreamReader reader = null;
            XMLStreamWriter rawWriter = null;

            try {
                reader = inputFactory.createXMLStreamReader(pipeIn);
                rawWriter = outputFactory.createXMLStreamWriter(osWriter);
                try (SOSXmlCDataStreamWriter cdataWriter = new SOSXmlCDataStreamWriter(rawWriter)) {

                    if (!omitXmlDeclaration) {
                        cdataWriter.writeStartDocument("UTF-8", "1.0");
                    }

                    while (reader.hasNext()) {
                        int event = reader.next();

                        switch (event) {
                        case XMLStreamConstants.START_ELEMENT:
                            cdataWriter.writeStartElement(reader.getPrefix(), reader.getLocalName(), reader.getNamespaceURI());
                            for (int i = 0; i < reader.getAttributeCount(); i++) {
                                String attrPrefix = reader.getAttributePrefix(i);
                                if (attrPrefix == null || attrPrefix.isEmpty()) {
                                    cdataWriter.writeAttribute(reader.getAttributeLocalName(i), reader.getAttributeValue(i));
                                } else {
                                    cdataWriter.writeAttribute(attrPrefix, reader.getAttributeNamespace(i), reader.getAttributeLocalName(i), reader
                                            .getAttributeValue(i));
                                }
                            }
                            for (int i = 0; i < reader.getNamespaceCount(); i++) {
                                String prefix = reader.getNamespacePrefix(i);
                                String nsUri = reader.getNamespaceURI(i);
                                if (prefix == null) {
                                    cdataWriter.writeDefaultNamespace(nsUri);
                                } else {
                                    cdataWriter.writeNamespace(prefix, nsUri);
                                }
                            }
                            break;

                        case XMLStreamConstants.CHARACTERS:
                            if (!reader.isWhiteSpace()) {
                                cdataWriter.writeCharacters(reader.getText());
                            }
                            break;

                        case XMLStreamConstants.END_ELEMENT:
                            cdataWriter.writeEndElement();
                            break;

                        case XMLStreamConstants.CDATA:
                            cdataWriter.writeCData(reader.getText());
                            break;

                        case XMLStreamConstants.COMMENT:
                            cdataWriter.writeComment(reader.getText());
                            break;

                        default:
                            // ignore other events
                        }
                    }
                    if (!omitXmlDeclaration) {
                        cdataWriter.writeEndDocument();
                    }
                    cdataWriter.flush();
                }

            } finally {
                closeQuietly(reader);
                closeQuietly(rawWriter);
            }

        } finally {
            try {
                if (transformThread != null) {
                    transformThread.join();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        return baos.toString("UTF-8").trim();
    }

    public static Transformer createTransformer(boolean omitXmlDeclaration, int indentAmount) throws Exception {
        return createTransformer(omitXmlDeclaration, indentAmount, null);
    }

    public static Transformer createTransformer(boolean omitXmlDeclaration, int indentAmount, String xslContent) throws Exception {
        Transformer t = newTransformer(xslContent);
        t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, omitXmlDeclaration ? "yes" : "no");
        if (indentAmount > 0) {
            t.setOutputProperty(OutputKeys.INDENT, "yes");
            t.setOutputProperty("{http://xml.apache.org/xalan}indent-amount", String.valueOf(indentAmount));
        } else {
            t.setOutputProperty(OutputKeys.INDENT, "no");
        }
        return t;
    }

    private static Transformer newTransformer(String content) throws TransformerConfigurationException {
        TransformerFactory f = TransformerFactory.newInstance();
        return content == null ? f.newTransformer() : f.newTransformer(new StreamSource(new StringReader(content)));
    }

    private static void closeQuietly(XMLStreamReader reader) {
        if (reader != null) {
            try {
                reader.close();
            } catch (XMLStreamException ignored) {
            }
        }
    }

    private static void closeQuietly(XMLStreamWriter writer) {
        if (writer != null) {
            try {
                writer.close();
            } catch (XMLStreamException ignored) {
            }
        }
    }
}
