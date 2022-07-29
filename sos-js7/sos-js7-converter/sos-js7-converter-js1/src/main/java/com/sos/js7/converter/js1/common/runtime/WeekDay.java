package com.sos.js7.converter.js1.common.runtime;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Node;

import com.sos.commons.xml.SOSXML.SOSXMLXPath;
import com.sos.commons.xml.exception.SOSXMLXPathException;
import com.sos.js7.converter.commons.JS7ConverterHelper;

public class WeekDay {

    private static final String ATTR_DAY = "day";
    private static final String ATTR_WHICH = "which";

    private List<Period> periods;

    // The name of the week day: "monday", "tuesday", "wednesday", "thursday", "friday", "saturday" and "sunday". Or numerical ...
    // More than one day can be specified by leaving an empty space between the names of the days.
    private String day;

    // which="1" to which="4": From the first to the fourth week days in a month.
    // which="-1" to which="-4": From the fourth-last to the last week days in a month.
    private Integer which;

    protected WeekDay(Path path, SOSXMLXPath xpath, Node node) throws SOSXMLXPathException {
        Map<String, String> m = JS7ConverterHelper.attribute2map(node);
        this.day = JS7ConverterHelper.stringValue(m.get(ATTR_DAY));
        this.which = JS7ConverterHelper.integerValue(m.get(ATTR_WHICH));
        this.periods = RunTime.convertPeriod(path, xpath, node);
    }

    public List<Period> getPeriods() {
        return periods;
    }

    public String getDay() {
        return day;
    }

    public Integer getWhich() {
        return which;
    }

}