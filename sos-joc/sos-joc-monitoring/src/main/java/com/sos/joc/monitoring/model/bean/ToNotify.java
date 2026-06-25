package com.sos.joc.monitoring.model.bean;

import java.util.ArrayList;
import java.util.List;

public class ToNotify extends AMonitorResult {

    private static final long serialVersionUID = 1L;
    private final List<MonitorOrderStepResult> steps;
    private final List<MonitorOrderResult> errorOrders;
    private final List<MonitorOrderResult> successOrders;

    private Long firstEventId = null;
    private Long lastEventId = null;

    public ToNotify() {
        steps = new ArrayList<>();
        errorOrders = new ArrayList<>();
        successOrders = new ArrayList<>();
    }

    public List<MonitorOrderStepResult> getSteps() {
        return steps;
    }

    public List<MonitorOrderResult> getErrorOrders() {
        return errorOrders;
    }

    public List<MonitorOrderResult> getSuccessOrders() {
        return successOrders;
    }

    public void setFirstEventId(Long val) {
        firstEventId = val;
    }

    public Long getFirstEventId() {
        return firstEventId;
    }

    public void setLastEventId(Long val) {
        lastEventId = val;
    }

    public Long getLastEventId() {
        return lastEventId;
    }
}
