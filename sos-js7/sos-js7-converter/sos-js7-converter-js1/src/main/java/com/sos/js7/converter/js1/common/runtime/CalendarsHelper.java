package com.sos.js7.converter.js1.common.runtime;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import com.sos.commons.util.SOSCollection;
import com.sos.commons.util.SOSPath;
import com.sos.js7.converter.commons.JS7ConverterHelper;
import com.sos.js7.converter.js1.common.json.calendars.CalendarType;
import com.sos.js7.converter.js1.common.json.calendars.JS1Calendar;
import com.sos.js7.converter.js1.common.json.calendars.JS1Calendars;

public class CalendarsHelper {

    private final String text;
    private final JS1Calendars calendars;

    private CalendarsHelper(String text, JS1Calendars calendars) {
        this.text = text;
        this.calendars = filterCalendars(calendars);
    }

    public static CalendarsHelper convert(Path file) throws Exception {
        return convert(SOSPath.readFile(file, StandardCharsets.UTF_8));
    }

    public static CalendarsHelper convert(String val) throws Exception {
        return new CalendarsHelper(val, JS7ConverterHelper.JSON_OM.readValue(val, JS1Calendars.class));
    }

    public String getText() {
        return text;
    }

    public JS1Calendars getCalendars() {
        return this.calendars;
    }

    public boolean hasCalendars() {
        return calendars != null && !SOSCollection.isEmpty(calendars.getCalendars());
    }

    public boolean hasWorkingDayCalendars() {
        return hasCalendars() && calendars.getCalendars().stream().filter(c -> CalendarType.WORKING_DAYS.equals(c.getType())).count() > 0;
    }

    public boolean hasNonWorkingDayCalendars() {
        return hasCalendars() && calendars.getCalendars().stream().filter(c -> CalendarType.NON_WORKING_DAYS.equals(c.getType())).count() > 0;
    }

    public List<JS1Calendar> getNonWorkingDayCalendars() {
        if (calendars == null) {
            return null;
        }
        return calendars.getCalendars().stream().filter(c -> CalendarType.NON_WORKING_DAYS.equals(c.getType())).collect(Collectors.toList());
    }

    private JS1Calendars filterCalendars(JS1Calendars calendars) {
        if (calendars == null) {
            return calendars;
        }
        List<JS1Calendar> l = calendars.getCalendars().stream().filter(c -> CalendarType.WORKING_DAYS.equals(c.getType())
                || CalendarType.NON_WORKING_DAYS.equals(c.getType())).collect(Collectors.toList());
        calendars.setCalendars(l);
        return calendars;
    }
}
