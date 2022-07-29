package com.sos.js7.converter.js1.common.runtime;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sos.commons.xml.SOSXML.SOSXMLXPath;
import com.sos.js7.converter.js1.common.Include;

public class Holidays {

    private static final String ELEMENT_HOLIDAY = "holiday";
    private static final String ELEMENT_INCLUDE = "include";

    private List<WeekDays> weekDays;
    private List<Holiday> holidays;
    private List<Include> includes;

    protected Holidays(Path path, SOSXMLXPath xpath, Node node) throws Exception {
        this.weekDays = RunTime.convertWeekDays(path, xpath, node);

        NodeList l = xpath.selectNodes(node, "./" + ELEMENT_HOLIDAY);
        if (l != null && l.getLength() > 0) {
            this.holidays = new ArrayList<>();
            for (int i = 0; i < l.getLength(); i++) {
                this.holidays.add(new Holiday(l.item(i)));
            }
        }
        l = xpath.selectNodes(node, "./" + ELEMENT_INCLUDE);
        if (l != null && l.getLength() > 0) {
            this.includes = new ArrayList<>();
            for (int i = 0; i < l.getLength(); i++) {
                this.includes.add(new Include(xpath, l.item(i)));
            }
        }
    }

    public List<WeekDays> getWeekDays() {
        return weekDays;
    }

    public List<Holiday> getHolidays() {
        return holidays;
    }

    public List<Include> getIncludes() {
        return includes;
    }

}
