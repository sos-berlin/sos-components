package com.sos.js7.history.helper;

import com.sos.joc.db.history.DBItemHistoryAgent;

public class CachedAgent {

    private final String timezone;
    private final String uri;

    public CachedAgent(final DBItemHistoryAgent item) {
        timezone = item.getTimezone();
        uri = item.getUri();
    }

    public String getTimezone() {
        return timezone;
    }

    public String getUri() {
        return uri;
    }

}
