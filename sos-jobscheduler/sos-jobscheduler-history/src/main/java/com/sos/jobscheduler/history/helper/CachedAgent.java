package com.sos.jobscheduler.history.helper;

import com.sos.jobscheduler.db.history.DBItemAgent;

public class CachedAgent {

    private final Long id;
    private final String uri;
    private String timezone;

    public CachedAgent(final DBItemAgent item) {
        id = item.getId();
        uri = item.getUri();
        timezone = item.getTimezone();
    }

    public Long getId() {
        return id;
    }

    public String getUri() {
        return uri;
    }

    public void setTimezone(String val) {
        timezone = val;
    }

    public String getTimezone() {
        return timezone;
    }

}
