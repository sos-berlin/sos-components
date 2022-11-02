package com.sos.joc.history.controller.proxy.fatevent;

import java.util.Date;
import java.util.Map;

import com.sos.joc.history.controller.proxy.HistoryEventEntry.HistoryOrder.WorkflowInfo.Position;

import js7.data.value.Value;

public abstract class AFatEventOrder extends AFatEvent {

    private String orderId;

    private String workflowPath;
    private String workflowVersionId;

    private String position;
    private Map<String, Value> arguments;

    public AFatEventOrder(Long eventId, Date eventDatetime) {
        super(eventId, eventDatetime);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void set(Object... objects) {
        if (objects.length >= 5) {
            this.orderId = (String) objects[0];
            this.workflowPath = (String) objects[1];
            this.workflowVersionId = (String) objects[2];
            if (objects[3] != null) {
                this.position = ((Position) objects[3]).asString();
            }
            if (objects[4] != null) {
                this.arguments = (Map<String, Value>) objects[4];
            }
        }
    }

    public String getOrderId() {
        return orderId;
    }

    public String getWorkflowPath() {
        return workflowPath;
    }

    public String getWorkflowVersionId() {
        return workflowVersionId;
    }

    public String getPosition() {
        return position;
    }

    public Map<String, Value> getArguments() {
        return arguments;
    }
}
