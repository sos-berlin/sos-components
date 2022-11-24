package com.sos.js7.converter.js1.common.runtime;

import java.nio.file.Path;
import java.util.List;

import org.w3c.dom.Node;

import com.sos.commons.xml.SOSXML.SOSXMLXPath;
import com.sos.commons.xml.exception.SOSXMLXPathException;

public class WeekDays {

    private List<Day> days;

    protected WeekDays(Path path, SOSXMLXPath xpath, Node node) throws SOSXMLXPathException {
        this.days = RunTime.convertDay(path, xpath, node);
    }

    protected WeekDays(com.sos.js7.converter.js1.common.json.schedule.Weekdays v) {
        this.days = RunTime.convertDay(v.getDays());
    }

    public List<Day> getDays() {
        return days;
    }

}
