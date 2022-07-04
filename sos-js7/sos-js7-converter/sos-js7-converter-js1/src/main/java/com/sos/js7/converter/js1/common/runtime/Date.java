package com.sos.js7.converter.js1.common.runtime;

import java.util.List;
import java.util.Map;

import org.w3c.dom.Node;

import com.sos.commons.xml.SOSXML.SOSXMLXPath;
import com.sos.commons.xml.exception.SOSXMLXPathException;
import com.sos.js7.converter.commons.JS7ConverterHelper;

public class Date {

    private static final String ATTR_DATE = "date";

    private List<Period> periods;

    private String date; // yyyy-mm-dd

    protected Date(SOSXMLXPath xpath, Node node) throws SOSXMLXPathException {
        Map<String, String> m = JS7ConverterHelper.attribute2map(node);
        this.date = JS7ConverterHelper.stringValue(m.get(ATTR_DATE));
        this.periods = RunTime.convertPeriod(xpath, node);
    }

    public List<Period> getPeriods() {
        return periods;
    }

    public String getDate() {
        return date;
    }

}
