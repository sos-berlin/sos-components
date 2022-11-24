package com.sos.js7.converter.js1.common.runtime;

import java.nio.file.Path;
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

    protected Month(Path path, SOSXMLXPath xpath, Node node) throws SOSXMLXPathException {
        Map<String, String> m = JS7ConverterHelper.attribute2map(node);
        this.month = JS7ConverterHelper.stringValue(m.get(ATTR_MONTH));

        this.periods = RunTime.convertPeriod(path, xpath, node);
        this.monthDays = RunTime.convertMonthDays(path, xpath, node);
        this.weekDays = RunTime.convertWeekDays(path, xpath, node);
        this.ultimos = RunTime.convertUltimos(path, xpath, node);
    }

    protected Month(com.sos.js7.converter.js1.common.json.schedule.Month v) {
        this.month = v.getMonth();

        this.periods = RunTime.convertPeriod(v.getPeriods());
        this.monthDays = RunTime.convertMonthDays(v.getMonthdays());
        this.weekDays = RunTime.convertWeekDays(v.getWeekdays());
        this.ultimos = RunTime.convertUltimos(v.getUltimos());
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
