package com.sos.js7.converter.js1.common.runtime;

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
import com.sos.js7.converter.js1.common.EConfigFileExtensions;
import com.sos.js7.converter.js1.common.json.calendars.JS1Calendars;
import com.sos.js7.converter.js1.input.DirectoryParser.DirectoryParserResult;
import com.sos.js7.converter.js1.output.js7.JS7Converter;

public class RunTime {

    // private static final Logger LOGGER = LoggerFactory.getLogger(RunTime.class);

    private static final String ATTR_SINGLE_START = "single_start";
    private static final String ATTR_BEGIN = "begin";
    private static final String ATTR_END = "end";
    private static final String ATTR_REPEAT = "repeat";
    private static final String ATTR_SCHEDULE = "schedule";
    private static final String ATTR_TIME_ZONE = "time_zone";
    private static final String ATTR_WHEN_HOLIDAY = "when_holiday";
    private static final String ATTR_LET_RUN = "let_run";
    private static final String ATTR_ONCE = "once";

    private static final int MAX_ELEMENTS_PRO_CHILD = 1_000;
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
    private CalendarsHelper calendarsHelper;
    private Path currentPath;

    protected RunTime(SOSXMLXPath xpath, Node node, Path currentPath) throws Exception {
        this.currentPath = currentPath;

        convertChildElements(currentPath, xpath, node);
    }

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

        convertChildElements(currentPath, xpath, node);
    }

    private void convertChildElements(Path path, SOSXMLXPath xpath, Node node) throws Exception {
        this.periods = convertPeriod(path, xpath, node);
        this.ats = convertAt(path, xpath, node);
        this.dates = convertDate(path, xpath, node);
        this.weekDays = convertWeekDays(path, xpath, node);
        this.monthDays = convertMonthDays(path, xpath, node);
        this.months = convertMonth(path, xpath, node);
        this.ultimos = convertUltimos(path, xpath, node);
        this.holidays = convertHolidays(path, xpath, node);
        this.calendarsHelper = convertCalendars(path, xpath, node, nodeText);
    }

    public boolean isEmpty() {
        return singleStart == null && begin == null && end == null && repeat == null && schedule == null && periods == null && ats == null
                && dates == null && weekDays == null && monthDays == null && months == null && ultimos == null && holidays == null
                && calendarsHelper == null;
    }

    public boolean hasCalendars() {
        return calendarsHelper != null && calendarsHelper.getCalendars().getCalendars() != null && calendarsHelper.getCalendars().getCalendars()
                .size() > 0;
    }

    public boolean isConvertableWithoutCalendars() {
        return !isEmpty() && !hasCalendars();
    }

    public boolean hasChildElements() {
        return periods != null || ats != null || dates != null || weekDays != null || monthDays != null || months != null || ultimos != null
                || holidays != null || calendarsHelper != null;
    }

    private Schedule convertSchedule(DirectoryParserResult pr, SOSXMLXPath xpath, Node node, Map<String, String> m, Path currentPath)
            throws Exception {
        String includePath = JS7ConverterHelper.stringValue(m.get(ATTR_SCHEDULE));
        if (SOSString.isEmpty(includePath)) {
            return null;
        }
        return newSchedule(pr, currentPath, includePath, ATTR_SCHEDULE);
    }

    public static Schedule newSchedule(RunTime runTime, String name, Path file) {
        return new Schedule(runTime, name, file);
    }

    public static Schedule newSchedule(DirectoryParserResult pr, Path currentPath, String includePath, String attrName) {
        try {
            Path p = JS7Converter.findIncludeFile(pr, currentPath, Paths.get(includePath + EConfigFileExtensions.SCHEDULE.extension()));
            if (p != null) {
                return new Schedule(pr, p);
            } else {
                ParserReport.INSTANCE.addErrorRecord(currentPath, "[attribute=" + attrName + "]Schedule not found=" + includePath, "");
                return null;
            }
        } catch (Throwable e) {
            ParserReport.INSTANCE.addErrorRecord(currentPath, "[attribute=" + attrName + "]Schedule not found=" + includePath, e);
            return null;
        }
    }

    protected static List<Period> convertPeriod(Path path, SOSXMLXPath xpath, Node node) throws SOSXMLXPathException {
        List<Period> result = null;
        NodeList l = xpath.selectNodes(node, "./" + ELEMENT_PERIOD);
        if (l != null && l.getLength() > 0) {
            result = new ArrayList<>();
            for (int i = 0; i < l.getLength(); i++) {
                if (i > MAX_ELEMENTS_PRO_CHILD) {
                    addReportSkipRecord(path, "convertPeriod", l.getLength());
                    break;
                }
                result.add(new Period(l.item(i)));
            }
        }
        return result;
    }

    public static CalendarsHelper convertCalendars(Path path, SOSXMLXPath xpath, Node node, String nodeText) throws SOSXMLXPathException {
        String c = SOSXML.getValue(xpath.selectNode(node, "./" + ELEMENT_CALENDARS));
        if (c != null) {
            try {
                return new CalendarsHelper(c, JS7ConverterHelper.JSON_OM.readValue(c, JS1Calendars.class));
            } catch (Throwable e) {
                ParserReport.INSTANCE.addErrorRecord(path, "[runtime][covertCalendars]" + nodeText, e);
            }
        }
        return null;
    }

    private List<At> convertAt(Path path, SOSXMLXPath xpath, Node node) throws SOSXMLXPathException {
        List<At> result = null;
        NodeList l = xpath.selectNodes(node, "./" + ELEMENT_AT);
        if (l != null && l.getLength() > 0) {
            result = new ArrayList<>();
            for (int i = 0; i < l.getLength(); i++) {
                if (i > MAX_ELEMENTS_PRO_CHILD) {
                    addReportSkipRecord(path, "convertAt", l.getLength());
                    break;
                }
                result.add(new At(l.item(i)));
            }
        }
        return result;
    }

    protected static List<Date> convertDate(Path path, SOSXMLXPath xpath, Node node) throws SOSXMLXPathException {
        List<Date> result = null;
        NodeList l = xpath.selectNodes(node, "./" + ELEMENT_DATE);
        if (l != null && l.getLength() > 0) {
            result = new ArrayList<>();
            for (int i = 0; i < l.getLength(); i++) {
                if (i > MAX_ELEMENTS_PRO_CHILD) {
                    addReportSkipRecord(path, "convertDate", l.getLength());
                    break;
                }
                result.add(new Date(path, xpath, l.item(i)));
            }
        }
        return result;
    }

    protected static List<WeekDays> convertWeekDays(Path path, SOSXMLXPath xpath, Node node) throws SOSXMLXPathException {
        List<WeekDays> result = null;
        NodeList l = xpath.selectNodes(node, "./" + ELEMENT_WEEKDAYS);
        if (l != null && l.getLength() > 0) {
            result = new ArrayList<>();
            for (int i = 0; i < l.getLength(); i++) {
                if (i > MAX_ELEMENTS_PRO_CHILD) {
                    addReportSkipRecord(path, "convertWeekDays", l.getLength());
                    break;
                }
                result.add(new WeekDays(path, xpath, l.item(i)));
            }
        }
        return result;
    }

    protected static List<MonthDays> convertMonthDays(Path path, SOSXMLXPath xpath, Node node) throws SOSXMLXPathException {
        List<MonthDays> result = null;
        NodeList l = xpath.selectNodes(node, "./" + ELEMENT_MONTHDAYS);
        if (l != null && l.getLength() > 0) {
            result = new ArrayList<>();
            for (int i = 0; i < l.getLength(); i++) {
                if (i > MAX_ELEMENTS_PRO_CHILD) {
                    addReportSkipRecord(path, "convertMonthDays", l.getLength());
                    break;
                }
                result.add(new MonthDays(path, xpath, l.item(i)));
            }
        }
        return result;
    }

    private List<Month> convertMonth(Path path, SOSXMLXPath xpath, Node node) throws SOSXMLXPathException {
        List<Month> result = null;
        NodeList l = xpath.selectNodes(node, "./" + ELEMENT_MONTH);
        if (l != null && l.getLength() > 0) {
            result = new ArrayList<>();
            for (int i = 0; i < l.getLength(); i++) {
                if (i > MAX_ELEMENTS_PRO_CHILD) {
                    addReportSkipRecord(path, "convertMonth", l.getLength());
                    break;
                }
                result.add(new Month(path, xpath, l.item(i)));
            }
        }
        return result;
    }

    protected static List<WeekDay> convertWeekDay(Path path, SOSXMLXPath xpath, Node node) throws SOSXMLXPathException {
        List<WeekDay> result = null;
        NodeList l = xpath.selectNodes(node, "./" + ELEMENT_WEEKDAY);
        if (l != null && l.getLength() > 0) {
            result = new ArrayList<>();
            for (int i = 0; i < l.getLength(); i++) {
                if (i > MAX_ELEMENTS_PRO_CHILD) {
                    addReportSkipRecord(path, "convertWeekDay", l.getLength());
                    break;
                }
                result.add(new WeekDay(path, xpath, l.item(i)));
            }
        }
        return result;
    }

    protected static List<Day> convertDay(Path path, SOSXMLXPath xpath, Node node) throws SOSXMLXPathException {
        List<Day> result = null;
        NodeList l = xpath.selectNodes(node, "./" + ELEMENT_DAY);
        if (l != null && l.getLength() > 0) {
            result = new ArrayList<>();
            for (int i = 0; i < l.getLength(); i++) {
                if (i > MAX_ELEMENTS_PRO_CHILD) {
                    addReportSkipRecord(path, "convertDay", l.getLength());
                    break;
                }
                result.add(new Day(path, xpath, l.item(i)));
            }
        }
        return result;
    }

    protected static List<Ultimos> convertUltimos(Path path, SOSXMLXPath xpath, Node node) throws SOSXMLXPathException {
        List<Ultimos> result = null;
        NodeList l = xpath.selectNodes(node, "./" + ELEMENT_ULTIMOS);
        if (l != null && l.getLength() > 0) {
            result = new ArrayList<>();
            for (int i = 0; i < l.getLength(); i++) {
                if (i > MAX_ELEMENTS_PRO_CHILD) {
                    addReportSkipRecord(path, "convertUltimos", l.getLength());
                    break;
                }
                result.add(new Ultimos(path, xpath, l.item(i)));
            }
        }
        return result;
    }

    protected static Holidays convertHolidays(Path path, SOSXMLXPath xpath, Node node) throws Exception {
        Holidays result = null;
        Node h = xpath.selectNode(node, "./" + ELEMENT_HOLIDAYS);
        if (h != null) {
            result = new Holidays(path, xpath, h);
        }
        return result;
    }

    private static void addReportSkipRecord(Path path, String method, int totalSize) {
        ParserReport.INSTANCE.addAnalyzerRecord(path, method, "total size=" + totalSize + ". converting after  " + MAX_ELEMENTS_PRO_CHILD
                + " skipped");
    }

    public String getNodeText() {
        return nodeText;
    }

    public Path getCurrentPath() {
        return currentPath;
    }

    public CalendarsHelper getCalendarsHelper() {
        return calendarsHelper;
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
}
