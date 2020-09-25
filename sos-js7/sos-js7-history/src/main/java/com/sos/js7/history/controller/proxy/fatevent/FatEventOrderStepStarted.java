package com.sos.js7.history.controller.proxy.fatevent;

import java.util.Date;

import com.sos.js7.history.controller.proxy.HistoryEventType;

public final class FatEventOrderStepStarted extends AFatEventOrder {

    private String agentPath;
    private String jobName;

    public FatEventOrderStepStarted(Long eventId, Date eventDatetime) {
        super(eventId, eventDatetime);
    }

    @Override
    public void set(Object... objects) {
        super.set(objects);
        this.agentPath = (String) objects[objects.length - 2];
        this.jobName = (String) objects[objects.length - 1];

    }

    @Override
    public HistoryEventType getType() {
        return HistoryEventType.OrderStepStarted;
    }

    public String getAgentPath() {
        return agentPath;
    }

    public String getJobName() {
        return jobName;
    }

}
