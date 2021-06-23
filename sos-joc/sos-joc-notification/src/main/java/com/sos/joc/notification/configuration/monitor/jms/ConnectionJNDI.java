package com.sos.joc.notification.configuration.monitor.jms;

import org.w3c.dom.Node;

import com.sos.joc.notification.configuration.AElement;

public class ConnectionJNDI extends AElement {

    public static String DEFAULT_LOOKUP_NAME = "ConnectionFactory";

    private static String ATTRIBUTE_NAME_FILE = "file";
    private static String ATTRIBUTE_NAME_LOOKUP_NAME = "lookup_name";

    private final String file;
    private final String lookupName;

    public ConnectionJNDI(Node node) {
        super(node);

        file = getValue(getElement().getAttribute(ATTRIBUTE_NAME_FILE));
        lookupName = getValue(getElement().getAttribute(ATTRIBUTE_NAME_LOOKUP_NAME), DEFAULT_LOOKUP_NAME);
    }

    public String getFile() {
        return file;
    }

    public String getLookupName() {
        return lookupName;
    }
}
