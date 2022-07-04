package com.sos.js7.converter.js1.common.runtime;

import java.util.List;

import org.w3c.dom.Node;

import com.sos.commons.xml.SOSXML.SOSXMLXPath;
import com.sos.commons.xml.exception.SOSXMLXPathException;

public class WeekDays {

    private List<Day> days;

    protected WeekDays(SOSXMLXPath xpath, Node node) throws SOSXMLXPathException {
        this.days = RunTime.convertDay(xpath, node);
    }

    public List<Day> getDays() {
        return days;
    }

}
