package com.sos.js7.converter.js1.common.runtime;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import com.sos.commons.util.SOSPath;
import com.sos.js7.converter.commons.JS7ConverterHelper;
import com.sos.js7.converter.js1.common.json.calendars.JS1Calendars;

public class CalendarsHelper {

    private final String text;
    private final JS1Calendars calendars;

    private CalendarsHelper(String text, JS1Calendars calendars) {
        this.text = text;
        this.calendars = calendars;
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
}
