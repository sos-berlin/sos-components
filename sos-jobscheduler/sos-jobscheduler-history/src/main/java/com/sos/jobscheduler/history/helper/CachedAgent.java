package com.sos.jobscheduler.history.helper;

import com.sos.jobscheduler.db.history.DBItemAgent;

public class CachedAgent {

    private final String agentKey;
    private final String uri;
    private final String timezone;

    public CachedAgent(final DBItemAgent item) {
        agentKey = item.getAgentKey();
        uri = item.getUri();
        timezone = item.getTimezone();
    }

    public String getAgentKey() {
        return agentKey;
    }

    public String getUri() {
        return uri;
    }

    public String getTimezone() {
        return timezone;
    }
}
