package com.sos.js7.converter.js1.common;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sos.commons.util.SOSString;
import com.sos.commons.xml.SOSXML;
import com.sos.commons.xml.SOSXML.SOSXMLXPath;
import com.sos.commons.xml.exception.SOSXMLXPathException;
import com.sos.js7.converter.commons.JS7ConverterHelper;
import com.sos.js7.converter.commons.report.ParserReport;
import com.sos.js7.converter.js1.common.json.calendar.JS1Calendars;
import com.sos.js7.converter.js1.input.DirectoryParser.DirectoryParserResult;
import com.sos.js7.converter.js1.output.js7.JS7Converter;

public class RunTime {

    private static final String ATTR_SINGLE_START = "single_start";
    private static final String ATTR_BEGIN = "begin";
    private static final String ATTR_END = "end";
    private static final String ATTR_REPEAT = "repeat";
    private static final String ATTR_SCHEDULE = "schedule";
    private static final String ATTR_TIME_ZONE = "time_zone";
    private static final String ATTR_WHEN_HOLIDAY = "when_holiday";
    private static final String ATTR_LET_RUN = "let_run";
    private static final String ATTR_ONCE = "once";

    private static final String ELEMENT_PERIOD = "period";
    private static final String ELEMENT_AT = "at";
    private static final String ELEMENT_DATE = "date";
    private static final String ELEMENT_DAY = "day";
    private static final String ELEMENT_WEEKDAYS = "weekdays";
    private static final String ELEMENT_WEEKDAY = "weekday";
    private static final String ELEMENT_MONTHDAYS = "monthdays";
    private static final String ELEMENT_MONTH = "month";
    private static final String ELEMENT_ULTIMOS = "ultimos";
    private static final String ELEMENT_HOLIDAYS = "holidays";
    private static final String ELEMENT_CALENDARS = "calendars";

    private String nodeText;

    private List<Period> periods;
    private List<At> ats;
    private List<Date> dates;
    private List<WeekDays> weekDays;
    private List<MonthDays> monthDays;
    private List<Month> months;
    private List<Ultimos> ultimos;
    private Holidays holidays;

    private String singleStart; // hh:mm[:ss]
    private String begin; // hh:mm[:ss]
    private String end; // hh:mm[:ss]
    private String repeat; // hh:mm[:ss] or seconds

    private Schedule schedule;
    private String timeZone;
    private String whenHoliday;

    private String letRun; // yes_no
    private String once; // yes_no
    private JS1Calendars calendars;
    private Path currentPath;

    public RunTime(DirectoryParserResult pr, SOSXMLXPath xpath, Node node, Path currentPath) throws Exception {
        this.nodeText = JS7ConverterHelper.nodeToString(node);
        this.currentPath = currentPath;

        Map<String, String> m = JS7ConverterHelper.attribute2map(node);
        this.singleStart = JS7ConverterHelper.stringValue(m.get(ATTR_SINGLE_START));
        this.begin = JS7ConverterHelper.stringValue(m.get(ATTR_BEGIN));
        this.end = JS7ConverterHelper.stringValue(m.get(ATTR_END));
        this.repeat = JS7ConverterHelper.stringValue(m.get(ATTR_REPEAT));
        this.schedule = convertSchedule(pr, xpath, node, m, currentPath);
        this.timeZone = JS7ConverterHelper.stringValue(m.get(ATTR_TIME_ZONE));
        this.whenHoliday = JS7ConverterHelper.stringValue(m.get(ATTR_WHEN_HOLIDAY));
        this.letRun = JS7ConverterHelper.stringValue(m.get(ATTR_LET_RUN));
        this.once = JS7ConverterHelper.stringValue(m.get(ATTR_ONCE));

        this.periods = convertPeriod(xpath, node);
        this.ats = convertAt(xpath, node);
        this.dates = convertDate(xpath, node);
        this.weekDays = convertWeekDays(xpath, node);
        this.monthDays = convertMonthDays(xpath, node);
        this.months = convertMonth(xpath, node);
        this.holidays = convertHolidays(xpath, node);
        this.calendars = convertCalendars(xpath, node, nodeText);
    }

    public boolean isEmpty() {
        return singleStart == null && begin == null && end == null && repeat == null && schedule == null && periods == null && ats == null
                && dates == null && weekDays == null && monthDays == null && months == null && holidays == null && calendars == null;
    }

    public boolean hasCalendars() {
        return calendars != null && calendars.getCalendars() != null && calendars.getCalendars().size() > 0;
    }

    private Schedule convertSchedule(DirectoryParserResult pr, SOSXMLXPath xpath, Node node, Map<String, String> m, Path currentPath)
            throws Exception {
        String includePath = JS7ConverterHelper.stringValue(m.get(ATTR_SCHEDULE));
        if (SOSString.isEmpty(includePath)) {
            return null;
        }
        return newSchedule(pr, currentPath, includePath, ATTR_SCHEDULE);
    }

    public static Schedule newSchedule(DirectoryParserResult pr, Path currentPath, String includePath, String attrName) {
        try {
            return new Schedule(pr, JS7Converter.findIncludeFile(pr, currentPath, Paths.get(includePath + EConfigFileExtensions.SCHEDULE
                    .extension())));
        } catch (Throwable e) {
            ParserReport.INSTANCE.addErrorRecord(currentPath, "[attribute=" + attrName + "]Schedule not found=" + includePath, e);
            return null;
        }
    }

    private List<Period> convertPeriod(SOSXMLXPath xpath, Node node) throws SOSXMLXPathException {
        List<Period> result = null;
        NodeList l = xpath.selectNodes(node, "./" + ELEMENT_PERIOD);
        if (l != null && l.getLength() > 0) {
            result = new ArrayList<>();
            for (int i = 0; i < l.getLength(); i++) {
                result.add(new Period(l.item(i)));
            }
        }
        return result;
    }

    public static JS1Calendars convertCalendars(SOSXMLXPath xpath, Node node, String nodeText) throws SOSXMLXPathException {
        String c = SOSXML.getValue(xpath.selectNode(node, "./" + ELEMENT_CALENDARS));
        if (c != null) {
            try {
                return JS7ConverterHelper.JSON_OM.readValue(c, JS1Calendars.class);
            } catch (Throwable e) {
                ParserReport.INSTANCE.addErrorRecord(null, "[runtime][covertCalendars]" + nodeText, e);
            }
        }
        return null;
    }

    private List<At> convertAt(SOSXMLXPath xpath, Node node) throws SOSXMLXPathException {
        List<At> result = null;
        NodeList l = xpath.selectNodes(node, "./" + ELEMENT_AT);
        if (l != null && l.getLength() > 0) {
            result = new ArrayList<>();
            for (int i = 0; i < l.getLength(); i++) {
                result.add(new At(l.item(i)));
            }
        }
        return result;
    }

    private List<Date> convertDate(SOSXMLXPath xpath, Node node) throws SOSXMLXPathException {
        List<Date> result = null;
        NodeList l = xpath.selectNodes(node, "./" + ELEMENT_DATE);
        if (l != null && l.getLength() > 0) {
            result = new ArrayList<>();
            for (int i = 0; i < l.getLength(); i++) {
                result.add(new Date(xpath, l.item(i)));
            }
        }
        return result;
    }

    private List<WeekDays> convertWeekDays(SOSXMLXPath xpath, Node node) throws SOSXMLXPathException {
        List<WeekDays> result = null;
        NodeList l = xpath.selectNodes(node, "./" + ELEMENT_WEEKDAYS);
        if (l != null && l.getLength() > 0) {
            result = new ArrayList<>();
            for (int i = 0; i < l.getLength(); i++) {
                result.add(new WeekDays(xpath, l.item(i)));
            }
        }
        return result;
    }

    private List<MonthDays> convertMonthDays(SOSXMLXPath xpath, Node node) throws SOSXMLXPathException {
        List<MonthDays> result = null;
        NodeList l = xpath.selectNodes(node, "./" + ELEMENT_MONTHDAYS);
        if (l != null && l.getLength() > 0) {
            result = new ArrayList<>();
            for (int i = 0; i < l.getLength(); i++) {
                result.add(new MonthDays(xpath, l.item(i)));
            }
        }
        return result;
    }

    private List<Month> convertMonth(SOSXMLXPath xpath, Node node) throws SOSXMLXPathException {
        List<Month> result = null;
        NodeList l = xpath.selectNodes(node, "./" + ELEMENT_MONTH);
        if (l != null && l.getLength() > 0) {
            result = new ArrayList<>();
            for (int i = 0; i < l.getLength(); i++) {
                result.add(new Month(xpath, l.item(i)));
            }
        }
        return result;
    }

    private List<WeekDay> convertWeekDay(SOSXMLXPath xpath, Node node) throws SOSXMLXPathException {
        List<WeekDay> result = null;
        NodeList l = xpath.selectNodes(node, "./" + ELEMENT_WEEKDAY);
        if (l != null && l.getLength() > 0) {
            result = new ArrayList<>();
            for (int i = 0; i < l.getLength(); i++) {
                result.add(new WeekDay(xpath, l.item(i)));
            }
        }
        return result;
    }

    private List<Day> convertDay(SOSXMLXPath xpath, Node node) throws SOSXMLXPathException {
        List<Day> result = null;
        NodeList l = xpath.selectNodes(node, "./" + ELEMENT_DAY);
        if (l != null && l.getLength() > 0) {
            result = new ArrayList<>();
            for (int i = 0; i < l.getLength(); i++) {
                result.add(new Day(xpath, l.item(i)));
            }
        }
        return result;
    }

    private List<Ultimos> convertUltimos(SOSXMLXPath xpath, Node node) throws SOSXMLXPathException {
        List<Ultimos> result = null;
        NodeList l = xpath.selectNodes(node, "./" + ELEMENT_ULTIMOS);
        if (l != null && l.getLength() > 0) {
            result = new ArrayList<>();
            for (int i = 0; i < l.getLength(); i++) {
                result.add(new Ultimos(xpath, l.item(i)));
            }
        }
        return result;
    }

    private Holidays convertHolidays(SOSXMLXPath xpath, Node node) throws Exception {
        Holidays result = null;
        Node h = xpath.selectNode(node, "./" + ELEMENT_HOLIDAYS);
        if (h != null) {
            result = new Holidays(xpath, h);
        }
        return result;
    }

    public String getNodeText() {
        return nodeText;
    }

    public Path getCurrentPath() {
        return currentPath;
    }

    public JS1Calendars getCalendars() {
        return calendars;
    }

    public List<Period> getPeriods() {
        return periods;
    }

    public List<At> getAts() {
        return ats;
    }

    public List<Date> getDates() {
        return dates;
    }

    public List<WeekDays> getWeekDays() {
        return weekDays;
    }

    public List<MonthDays> getMonthDays() {
        return monthDays;
    }

    public List<Month> getMonths() {
        return months;
    }

    public List<Ultimos> getUltimos() {
        return ultimos;
    }

    public Holidays getHolidays() {
        return holidays;
    }

    public String getSingleStart() {
        return singleStart;
    }

    public String getBegin() {
        return begin;
    }

    public String getEnd() {
        return end;
    }

    public String getRepeat() {
        return repeat;
    }

    public Schedule getSchedule() {
        return schedule;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public String getWhenHoliday() {
        return whenHoliday;
    }

    public String getLetRun() {
        return letRun;
    }

    public String getOnce() {
        return once;
    }

    public class Period {

        private static final String ATTR_ABSOLUTE_REPEAT = "absolute_repeat";
        private static final String ATTR_BEGIN = "begin";
        private static final String ATTR_END = "end";
        private static final String ATTR_LET_RUN = "let_run";
        private static final String ATTR_REPEAT = "repeat";
        private static final String ATTR_SINGLE_START = "single_start";
        private static final String ATTR_WHEN_HOLIDAY = "when_holiday";

        private String absoluteRepeat; // hh:mm[:ss] | seconds
        private String begin; // hh:mm[:ss] - (Initial value: 00:00)
        private String end; // hh:mm[:ss] - (Initial value: 24:00)
        private String letRun; // yes_no
        private String repeat; // hh:mm[:ss] or seconds - default 0
        private String singleStart; // hh:mm[:ss]
        private String whenHoliday; // suppress,ignore_holiday,previous_non_holiday,next_non_holiday

        private Period(Node node) {
            Map<String, String> m = JS7ConverterHelper.attribute2map(node);
            this.absoluteRepeat = JS7ConverterHelper.stringValue(m.get(ATTR_ABSOLUTE_REPEAT));
            this.begin = JS7ConverterHelper.stringValue(m.get(ATTR_BEGIN));
            this.end = JS7ConverterHelper.stringValue(m.get(ATTR_END));
            this.letRun = JS7ConverterHelper.stringValue(m.get(ATTR_LET_RUN));
            this.repeat = JS7ConverterHelper.stringValue(m.get(ATTR_REPEAT));
            this.singleStart = JS7ConverterHelper.stringValue(m.get(ATTR_SINGLE_START));
            this.whenHoliday = JS7ConverterHelper.stringValue(m.get(ATTR_WHEN_HOLIDAY));
        }

        public String getAbsoluteRepeat() {
            return absoluteRepeat;
        }

        public String getBegin() {
            return begin;
        }

        public String getEnd() {
            return end;
        }

        public String getLetRun() {
            return letRun;
        }

        public String getRepeat() {
            return repeat;
        }

        public String getSingleStart() {
            return singleStart;
        }

        public String getWhenHoliday() {
            return whenHoliday;
        }

    }

    public class At {

        private static final String ATTR_AT = "at";

        private String at; // yyyy-mm-dd hh:mm[:ss]

        private At(Node node) {
            Map<String, String> m = JS7ConverterHelper.attribute2map(node);
            this.at = JS7ConverterHelper.stringValue(m.get(ATTR_AT));
        }

        public String getAt() {
            return at;
        }

    }

    public class Date {

        private static final String ATTR_DATE = "date";

        private List<Period> periods;

        private String date; // yyyy-mm-dd

        private Date(SOSXMLXPath xpath, Node node) throws SOSXMLXPathException {
            Map<String, String> m = JS7ConverterHelper.attribute2map(node);
            this.date = JS7ConverterHelper.stringValue(m.get(ATTR_DATE));
            this.periods = convertPeriod(xpath, node);
        }

        public List<Period> getPeriods() {
            return periods;
        }

        public String getDate() {
            return date;
        }

    }

    public class WeekDays {

        private List<Day> days;

        private WeekDays(SOSXMLXPath xpath, Node node) throws SOSXMLXPathException {
            this.days = convertDay(xpath, node);
        }

        public List<Day> getDays() {
            return days;
        }

    }

    public class Day {

        private static final String ATTR_DAY = "day";

        private List<Period> periods;

        private String day;

        private Day(SOSXMLXPath xpath, Node node) throws SOSXMLXPathException {
            Map<String, String> m = JS7ConverterHelper.attribute2map(node);
            this.day = JS7ConverterHelper.stringValue(m.get(ATTR_DAY));
            this.periods = convertPeriod(xpath, node);
        }

        public List<Period> getPeriods() {
            return periods;
        }

        public String getDay() {
            return day;
        }

        public List<Integer> getDays() {
            try {
                return JS7ConverterHelper.integerListValue(day, " ");
            } catch (Throwable e) {
                ParserReport.INSTANCE.addErrorRecord(currentPath, "convert day=" + day + " to integer days", e);
                return null;
            }
        }

    }

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

        private WeekDay(SOSXMLXPath xpath, Node node) throws SOSXMLXPathException {
            Map<String, String> m = JS7ConverterHelper.attribute2map(node);
            this.day = JS7ConverterHelper.stringValue(m.get(ATTR_DAY));
            this.which = JS7ConverterHelper.integerValue(m.get(ATTR_WHICH));
            this.periods = convertPeriod(xpath, node);
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

    public class MonthDays {

        private List<Day> days;
        private List<WeekDay> weekDays;

        private MonthDays(SOSXMLXPath xpath, Node node) throws SOSXMLXPathException {
            this.days = convertDay(xpath, node);
            this.weekDays = convertWeekDay(xpath, node);
        }

        public List<Day> getDays() {
            return days;
        }

        public List<WeekDay> getWeekDays() {
            return weekDays;
        }

    }

    public class Month {

        private static final String ATTR_MONTH = "month";

        private List<Period> periods;
        private List<MonthDays> monthDays;
        private List<WeekDays> weekDays;
        private List<Ultimos> ultimos;

        // One of more names of months, seperated by empty spaces (" "):
        // "january", "february", "march", "april", "may", "june", "july", "august", "september", "october", "november", "december".
        private String month;

        private Month(SOSXMLXPath xpath, Node node) throws SOSXMLXPathException {
            Map<String, String> m = JS7ConverterHelper.attribute2map(node);
            this.month = JS7ConverterHelper.stringValue(m.get(ATTR_MONTH));

            this.periods = convertPeriod(xpath, node);
            this.monthDays = convertMonthDays(xpath, node);
            this.weekDays = convertWeekDays(xpath, node);
            this.ultimos = convertUltimos(xpath, node);
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

    public class Ultimos {

        private List<Day> days;

        private Ultimos(SOSXMLXPath xpath, Node node) throws SOSXMLXPathException {
            this.days = convertDay(xpath, node);
        }

        public List<Day> getDays() {
            return days;
        }

    }

    public class Holidays {

        private static final String ELEMENT_HOLIDAY = "holiday";
        private static final String ELEMENT_INCLUDE = "include";

        private List<WeekDays> weekDays;
        private List<Holiday> holidays;
        private List<Include> includes;

        private Holidays(SOSXMLXPath xpath, Node node) throws Exception {
            this.weekDays = convertWeekDays(xpath, node);

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

    public class Holiday {

        private static final String ATTR_DATE = "date";

        // <holiday date="2004-12-24"/>
        private String date;

        private Holiday(Node node) throws SOSXMLXPathException {
            Map<String, String> m = JS7ConverterHelper.attribute2map(node);
            this.date = JS7ConverterHelper.stringValue(m.get(ATTR_DATE));
        }

        public String getDate() {
            return date;
        }

    }
}
