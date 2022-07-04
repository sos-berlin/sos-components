package com.sos.js7.converter.js1.common.runtime;

import java.util.List;
import java.util.Map;

import org.w3c.dom.Node;

import com.sos.commons.xml.SOSXML.SOSXMLXPath;
import com.sos.commons.xml.exception.SOSXMLXPathException;
import com.sos.js7.converter.commons.JS7ConverterHelper;

public class Month {

    private static final String ATTR_MONTH = "month";

    private List<Period> periods;
    private List<MonthDays> monthDays;
    private List<WeekDays> weekDays;
    private List<Ultimos> ultimos;

    // One of more names of months, seperated by empty spaces (" "):
    // "january", "february", "march", "april", "may", "june", "july", "august", "september", "october", "november", "december".
    private String month;

    protected Month(SOSXMLXPath xpath, Node node) throws SOSXMLXPathException {
        Map<String, String> m = JS7ConverterHelper.attribute2map(node);
        this.month = JS7ConverterHelper.stringValue(m.get(ATTR_MONTH));

        this.periods = RunTime.convertPeriod(xpath, node);
        this.monthDays = RunTime.convertMonthDays(xpath, node);
        this.weekDays = RunTime.convertWeekDays(xpath, node);
        this.ultimos = RunTime.convertUltimos(xpath, node);
    }

    public List<Period> getPeriods() {
        return periods;
    }

    public List<MonthDays> getMonthDays() {
        return monthDays;
    }

    public List<WeekDays> getWeekDays() {
        return weekDays;
    }

    public List<Ultimos> getUltimos() {
        return ultimos;
    }

    public String getMonth() {
        return month;
    }

}
