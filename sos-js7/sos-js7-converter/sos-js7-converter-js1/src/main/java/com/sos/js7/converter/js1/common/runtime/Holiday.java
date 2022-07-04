package com.sos.js7.converter.js1.common.runtime;

import java.util.Map;

import org.w3c.dom.Node;

import com.sos.commons.xml.exception.SOSXMLXPathException;
import com.sos.js7.converter.commons.JS7ConverterHelper;

public class Holiday {

    private static final String ATTR_DATE = "date";

    // <holiday date="2004-12-24"/>
    private String date;

    protected Holiday(Node node) throws SOSXMLXPathException {
        Map<String, String> m = JS7ConverterHelper.attribute2map(node);
        this.date = JS7ConverterHelper.stringValue(m.get(ATTR_DATE));
    }

    public String getDate() {
        return date;
    }

}
