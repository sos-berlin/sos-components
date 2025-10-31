package com.sos.js7.converter.js1.common.runtime;

import java.util.Map;

import org.w3c.dom.Node;

import com.sos.commons.xml.exception.SOSXMLXPathException;
import com.sos.js7.converter.commons.JS7ConverterHelper;

public class Holiday {

    private static final String ATTR_DATE = "date";
    private static final String ATTR_CALENDAR = "calendar";

    // <holiday date="2004-12-24"/>
    private String date;
    // <holiday calendar="/NationalHolidaysGermany" date="2022-12-26"/>
    private String calendar;

    protected Holiday(Node node) throws SOSXMLXPathException {
        Map<String, String> m = JS7ConverterHelper.attribute2map(node);
        this.date = JS7ConverterHelper.stringValue(m.get(ATTR_DATE));
        this.calendar = JS7ConverterHelper.stringValue(m.get(ATTR_CALENDAR));
    }

    protected Holiday(com.sos.js7.converter.js1.common.json.schedule.Holiday v) {
        this.date = v.getDate();
        this.calendar = v.getCalendar();
    }

    public String getDate() {
        return date;
    }

    public String getCalendar() {
        return calendar;
    }
}
