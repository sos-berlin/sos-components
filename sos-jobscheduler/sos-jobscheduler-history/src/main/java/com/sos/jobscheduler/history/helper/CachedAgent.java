package com.sos.jobscheduler.history.helper;

import com.sos.jobscheduler.db.history.DBItemAgent;

public class CachedAgent {

    private final String path;
    private final String uri;
    private final String timezone;

    public CachedAgent(final DBItemAgent item) {
        path = item.getPath();
        uri = item.getUri();
        timezone = item.getTimezone();
    }

    public String getPath() {
        return path;
    }

    public String getUri() {
        return uri;
    }

    public String getTimezone() {
        return timezone;
    }
}
