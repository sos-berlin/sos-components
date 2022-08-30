package com.sos.js7.converter.js1.common.runtime;

import com.sos.js7.converter.js1.common.json.calendars.JS1Calendars;

public class CalendarsHelper {

    private final String text;
    private final JS1Calendars calendars;

    protected CalendarsHelper(String text, JS1Calendars calendars) {
        this.text = text;
        this.calendars = calendars;
    }

    public String getText() {
        return text;
    }

    public JS1Calendars getCalendars() {
        return this.calendars;
    }
}
