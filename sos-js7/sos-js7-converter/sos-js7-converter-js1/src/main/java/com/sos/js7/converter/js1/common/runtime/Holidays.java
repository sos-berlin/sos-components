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

    protected Holidays(com.sos.js7.converter.js1.common.json.schedule.Holidays v) throws Exception {
        this.weekDays = RunTime.convertWeekDays(v.getWeekdays());

        if (v.getDays() != null && v.getDays().size() > 0) {
            this.holidays = new ArrayList<>();
            for (int i = 0; i < v.getDays().size(); i++) {
                this.holidays.add(new Holiday(v.getDays().get(i)));
            }
        }
        if (v.getIncludes() != null && v.getIncludes().size() > 0) {
            this.includes = new ArrayList<>();
            for (int i = 0; i < v.getIncludes().size(); i++) {
                this.includes.add(new Include(v.getIncludes().get(i)));
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
