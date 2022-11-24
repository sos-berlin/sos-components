package com.sos.js7.converter.js1.common.runtime;

import java.nio.file.Path;
import java.util.List;

import org.w3c.dom.Node;

import com.sos.commons.xml.SOSXML.SOSXMLXPath;
import com.sos.commons.xml.exception.SOSXMLXPathException;

public class MonthDays {

    private List<Day> days;
    private List<WeekDay> weekDays;

    protected MonthDays(Path path, SOSXMLXPath xpath, Node node) throws SOSXMLXPathException {
        this.days = RunTime.convertDay(path, xpath, node);
        this.weekDays = RunTime.convertWeekDay(path, xpath, node);
    }

    protected MonthDays(com.sos.js7.converter.js1.common.json.schedule.Monthdays v) {
        this.days = RunTime.convertDay(v.getDays());
        this.weekDays = RunTime.convertWeekDay(v.getWeekdays());
    }

    public List<Day> getDays() {
        return days;
    }

    public List<WeekDay> getWeekDays() {
        return weekDays;
    }

}
