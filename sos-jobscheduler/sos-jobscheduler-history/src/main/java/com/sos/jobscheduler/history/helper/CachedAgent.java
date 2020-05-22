package com.sos.jobscheduler.history.helper;

import com.sos.jobscheduler.db.history.DBItemHistoryAgent;

public class CachedAgent {

    private final Long id;
    private final String path;
    private final String timezone;
    private String uri;

    public CachedAgent(final DBItemHistoryAgent item) {
        id = item.getId();
        path = item.getPath();
        timezone = item.getTimezone();
        uri = item.getUri();
    }

    public Long getId() {
        return id;
    }

    public String getPath() {
        return path;
    }

    public String getTimezone() {
        return timezone;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String val) {
        uri = val;
    }
}
