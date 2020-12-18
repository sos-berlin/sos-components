package com.sos.js7.history.helper;

import com.sos.joc.db.history.DBItemHistoryAgent;

public class CachedAgent {

    private final Long id;
    private final String agentId;
    private final String timezone;
    private String uri;

    public CachedAgent(final DBItemHistoryAgent item) {
        id = item.getId();
        agentId = item.getAgentId();
        timezone = item.getTimezone();
        uri = item.getUri();
    }

    public Long getId() {
        return id;
    }

    public String getAgentId() {
        return agentId;
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
