package com.sos.joc.monitoring.model.bean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ToNotify extends AMonitorResult {

    private static final long serialVersionUID = 1L;
    private final List<MonitorOrderStepResult> steps;
    private final List<MonitorOrderResult> errorOrders;
    private final List<MonitorOrderResult> successOrders;

    private Long firstEventId = null;
    private Map<String, Long> lastEventIdByController = null;

    public ToNotify() {
        steps = new ArrayList<>();
        errorOrders = new ArrayList<>();
        successOrders = new ArrayList<>();
        lastEventIdByController = new HashMap<>();
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

    public void addLastEventIdByController(String controller, Long eventId) {
        lastEventIdByController.put(controller, eventId);
    }

    public Map<String, Long> getLastEventIdByController() {
        return lastEventIdByController;
    }

    public Long getLastEventId() {
        return lastEventIdByController.values().stream().max(Long::compare).orElse(null);
    }
}
