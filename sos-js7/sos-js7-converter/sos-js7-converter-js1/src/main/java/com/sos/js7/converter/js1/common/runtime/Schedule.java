package com.sos.js7.converter.js1.common.runtime;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Node;

import com.sos.commons.util.SOSString;
import com.sos.commons.xml.SOSXML;
import com.sos.commons.xml.SOSXML.SOSXMLXPath;
import com.sos.js7.converter.commons.JS7ConverterHelper;
import com.sos.js7.converter.js1.common.EConfigFileExtensions;
import com.sos.js7.converter.js1.common.json.calendar.JS1Calendars;
import com.sos.js7.converter.js1.input.DirectoryParser.DirectoryParserResult;

public class Schedule {

    private static final String ATTR_NAME = "name";
    private static final String ATTR_SUBSTITUTE = "substitute";
    private static final String ATTR_VALID_FROM = "valid_from";
    private static final String ATTR_VALID_TO = "valid_to";

    private Path path;// extra

    private String name;
    private String substitute;
    private String validFrom; // yyyy-mm-dd HH:MM[:ss]
    private String validTo; // yyyy-mm-dd HH:MM[:ss]

    private List<Date> dates;
    private List<WeekDays> weekDays;
    private List<MonthDays> monthDays;
    private List<Ultimos> ultimos;
    private Holidays holidays;

    private JS1Calendars calendars;

    public Schedule(RunTime runTime, String name, Path file) {
        this.path = file;
        this.name = name;
        this.dates = runTime.getDates();
        this.weekDays = runTime.getWeekDays();
        this.monthDays = runTime.getMonthDays();
        this.ultimos = runTime.getUltimos();
        this.holidays = runTime.getHolidays();
    }

    public boolean isConvertable() {
        return dates != null || weekDays != null || monthDays != null || ultimos != null || holidays != null;
    }

    public Schedule(DirectoryParserResult pr, Path file) throws Exception {
        this.path = file;
        Node node = JS7ConverterHelper.getDocumentRoot(file);
        String nodeText = JS7ConverterHelper.nodeToString(node);

        Map<String, String> m = JS7ConverterHelper.attribute2map(node);
        this.name = JS7ConverterHelper.stringValue(m.get(ATTR_NAME));
        this.substitute = JS7ConverterHelper.stringValue(m.get(ATTR_SUBSTITUTE));
        this.validFrom = JS7ConverterHelper.stringValue(m.get(ATTR_VALID_FROM));
        this.validTo = JS7ConverterHelper.stringValue(m.get(ATTR_VALID_TO));

        SOSXMLXPath xpath = SOSXML.newXPath();
        this.dates = RunTime.convertDate(xpath, node);
        this.weekDays = RunTime.convertWeekDays(xpath, node);
        this.monthDays = RunTime.convertMonthDays(xpath, node);
        this.ultimos = RunTime.convertUltimos(xpath, node);
        this.holidays = RunTime.convertHolidays(xpath, node);

        this.calendars = RunTime.convertCalendars(xpath, node, nodeText);
        // TODO
        if (this.calendars == null && this.substitute != null) {
            Schedule s = RunTime.newSchedule(pr, file, this.substitute, ATTR_SUBSTITUTE);
            if (s != null) {
                this.calendars = s.getCalendars();
                if (SOSString.isEmpty(this.name)) {
                    this.name = s.getName();
                }
                if (SOSString.isEmpty(this.validFrom)) {
                    this.validFrom = s.getValidFrom();
                }
                if (SOSString.isEmpty(this.validTo)) {
                    this.validTo = s.getValidTo();
                }
            }
        }
        if (SOSString.isEmpty(this.name)) {
            this.name = EConfigFileExtensions.getScheduleName(file);
        }
    }

    public Path getPath() {
        return path;
    }

    public String getName() {
        return name;
    }

    public String getSubstitute() {
        return substitute;
    }

    public String getValidFrom() {
        return validFrom;
    }

    public String getValidTo() {
        return validTo;
    }

    public JS1Calendars getCalendars() {
        return calendars;
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

    public List<Ultimos> getUltimos() {
        return ultimos;
    }

    public Holidays getHolidays() {
        return holidays;
    }
}
