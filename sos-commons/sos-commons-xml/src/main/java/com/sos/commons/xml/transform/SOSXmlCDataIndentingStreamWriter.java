package com.sos.commons.xml.transform;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

public class SOSXmlCDataIndentingStreamWriter implements XMLStreamWriter, AutoCloseable {

    private static final String NEW_LINE = "\n";

    private final XMLStreamWriter delegate;

    private final String indentStep;
    private final boolean indentEnabled;

    private int depth = 0;
    private boolean newLinePending = false;

    public SOSXmlCDataIndentingStreamWriter(XMLStreamWriter delegate, int indentAmount) {
        this.delegate = delegate;
        this.indentStep = (indentAmount > 0) ? " ".repeat(indentAmount) : "";
        this.indentEnabled = indentAmount > 0;
    }

    // general change: writeCharacters() writes CDATA
    @Override
    public void writeCharacters(String text) throws XMLStreamException {
        if (text != null && !text.isEmpty()) {
            delegate.writeCData(text);
            newLinePending = false;
        } else {
            delegate.writeCharacters(text);
        }
    }

    @Override
    public void writeCharacters(char[] text, int start, int len) throws XMLStreamException {
        if (text != null && len > 0) {
            String s = new String(text, start, len);
            delegate.writeCData(s);
            newLinePending = false;
        } else {
            delegate.writeCharacters(text, start, len);
        }
    }

    // delegate all other methods
    @Override
    public void writeStartElement(String localName) throws XMLStreamException {
        writeIndent();

        delegate.writeStartElement(localName);
        depth++;
        newLinePending = true;
    }

    @Override
    public void writeStartElement(String namespaceURI, String localName) throws XMLStreamException {
        writeIndent();

        delegate.writeStartElement(namespaceURI, localName);
        depth++;
        newLinePending = true;
    }

    @Override
    public void writeStartElement(String prefix, String localName, String namespaceURI) throws XMLStreamException {
        writeIndent();

        delegate.writeStartElement(prefix, localName, namespaceURI);
        depth++;
        newLinePending = true;
    }

    @Override
    public void writeEmptyElement(String namespaceURI, String localName) throws XMLStreamException {
        writeIndent();

        delegate.writeEmptyElement(namespaceURI, localName);
        newLinePending = true;
    }

    @Override
    public void writeEmptyElement(String prefix, String localName, String namespaceURI) throws XMLStreamException {
        writeIndent();

        delegate.writeEmptyElement(prefix, localName, namespaceURI);
        newLinePending = true;
    }

    @Override
    public void writeEmptyElement(String localName) throws XMLStreamException {
        writeIndent();

        delegate.writeEmptyElement(localName);
        newLinePending = true;
    }

    @Override
    public void writeEndElement() throws XMLStreamException {
        depth--;
        if (indentEnabled && newLinePending) {
            writeIndent();
        }
        delegate.writeEndElement();
        newLinePending = true;
    }

    @Override
    public void writeEndDocument() throws XMLStreamException {
        delegate.writeEndDocument();
    }

    @Override
    public void close() throws XMLStreamException {
        delegate.close();
    }

    @Override
    public void flush() throws XMLStreamException {
        delegate.flush();
    }

    @Override
    public void writeAttribute(String localName, String value) throws XMLStreamException {
        delegate.writeAttribute(localName, value);
    }

    @Override
    public void writeAttribute(String prefix, String namespaceURI, String localName, String value) throws XMLStreamException {
        delegate.writeAttribute(prefix, namespaceURI, localName, value);
    }

    @Override
    public void writeAttribute(String namespaceURI, String localName, String value) throws XMLStreamException {
        delegate.writeAttribute(namespaceURI, localName, value);
    }

    @Override
    public void writeNamespace(String prefix, String namespaceURI) throws XMLStreamException {
        delegate.writeNamespace(prefix, namespaceURI);
    }

    @Override
    public void writeDefaultNamespace(String namespaceURI) throws XMLStreamException {
        delegate.writeDefaultNamespace(namespaceURI);
    }

    @Override
    public void writeProcessingInstruction(String target) throws XMLStreamException {
        delegate.writeProcessingInstruction(target);
    }

    @Override
    public void writeProcessingInstruction(String target, String data) throws XMLStreamException {
        delegate.writeProcessingInstruction(target, data);
    }

    @Override
    public void writeComment(String data) throws XMLStreamException {
        delegate.writeComment(data);
    }

    @Override
    public void writeCData(String data) throws XMLStreamException {
        delegate.writeCData(data);
    }

    @Override
    public void writeDTD(String dtd) throws XMLStreamException {
        delegate.writeDTD(dtd);
    }

    @Override
    public void writeEntityRef(String name) throws XMLStreamException {
        delegate.writeEntityRef(name);
    }

    @Override
    public void writeStartDocument() throws XMLStreamException {
        delegate.writeStartDocument();
    }

    @Override
    public void writeStartDocument(String version) throws XMLStreamException {
        delegate.writeStartDocument(version);
    }

    @Override
    public void writeStartDocument(String encoding, String version) throws XMLStreamException {
        delegate.writeStartDocument(encoding, version);
    }

    @Override
    public String getPrefix(String uri) throws XMLStreamException {
        return delegate.getPrefix(uri);
    }

    @Override
    public void setPrefix(String prefix, String uri) throws XMLStreamException {
        delegate.setPrefix(prefix, uri);
    }

    @Override
    public void setDefaultNamespace(String uri) throws XMLStreamException {
        delegate.setDefaultNamespace(uri);
    }

    @Override
    public void setNamespaceContext(javax.xml.namespace.NamespaceContext context) throws XMLStreamException {
        delegate.setNamespaceContext(context);
    }

    @Override
    public javax.xml.namespace.NamespaceContext getNamespaceContext() {
        return delegate.getNamespaceContext();
    }

    @Override
    public Object getProperty(String name) throws IllegalArgumentException {
        return delegate.getProperty(name);
    }

    private void writeIndent() throws XMLStreamException {
        if (!indentEnabled) {
            return;
        }

        delegate.writeCharacters(NEW_LINE);
        for (int i = 0; i < depth; i++) {
            delegate.writeCharacters(indentStep);
        }
    }
}
