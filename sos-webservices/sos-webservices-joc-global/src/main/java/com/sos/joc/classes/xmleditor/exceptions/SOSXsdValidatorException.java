package com.sos.joc.classes.xmleditor.exceptions;

import org.xml.sax.SAXParseException;

public class SOSXsdValidatorException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final String elementName;
    private final String elementPosition;
    private final int elementDepth;
    private final boolean fatal;

    private int lineNumber = 1;
    private int columnNumber = 1;

    public SOSXsdValidatorException(SAXParseException cause, String name, String position, int depth, boolean fatal) {
        super(cause);
        this.elementName = name;
        this.elementPosition = position;
        this.elementDepth = depth;
        this.fatal = fatal;
        if (cause != null) {
            lineNumber = cause.getLineNumber();
            columnNumber = cause.getColumnNumber();
        }
    }

    public String getElementName() {
        return elementName;
    }

    public String getElementPosition() {
        return elementPosition;
    }

    public int getElementDepth() {
        return elementDepth;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public int getColumnNumber() {
        return columnNumber;
    }

    public boolean getFatal() {
        return fatal;
    }
}
