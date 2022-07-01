package com.sos.js7.converter.js1.common.runtime;

import java.util.List;

import org.w3c.dom.Node;

import com.sos.commons.xml.SOSXML.SOSXMLXPath;
import com.sos.commons.xml.exception.SOSXMLXPathException;

public class MonthDays {

    private List<Day> days;
    private List<WeekDay> weekDays;

    protected MonthDays(SOSXMLXPath xpath, Node node) throws SOSXMLXPathException {
        this.days = RunTime.convertDay(xpath, node);
        this.weekDays = RunTime.convertWeekDay(xpath, node);
    }

    public List<Day> getDays() {
        return days;
    }

    public List<WeekDay> getWeekDays() {
        return weekDays;
    }

}
