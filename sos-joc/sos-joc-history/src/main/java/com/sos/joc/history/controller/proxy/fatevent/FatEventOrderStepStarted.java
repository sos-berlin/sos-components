package com.sos.joc.history.controller.proxy.fatevent;

import java.util.Date;

import com.sos.joc.history.controller.proxy.HistoryEventType;

public final class FatEventOrderStepStarted extends AFatEventOrder {

    private String agentId;
    private String jobName;
    private String jobLabel;

    public FatEventOrderStepStarted(Long eventId, Date eventDatetime) {
        super(eventId, eventDatetime);
    }

    @Override
    public void set(Object... objects) {
        super.set(objects);
        this.agentId = (String) objects[objects.length - 3];
        this.jobName = (String) objects[objects.length - 2];
        this.jobLabel = (String) objects[objects.length - 1];
    }

    @Override
    public HistoryEventType getType() {
        return HistoryEventType.OrderStepStarted;
    }

    public String getAgentId() {
        return agentId;
    }

    public String getJobName() {
        return jobName;
    }

    public String getJobLabel() {
        return jobLabel;
    }
}
