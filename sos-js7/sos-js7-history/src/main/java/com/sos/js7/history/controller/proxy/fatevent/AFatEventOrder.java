package com.sos.js7.history.controller.proxy.fatevent;

import java.util.Date;
import java.util.List;

public abstract class AFatEventOrder extends AFatEvent {

    private String orderId;

    private String workflowPath;
    private String workflowVersionId;

    private List<?> position;
    private String arguments;

    public AFatEventOrder(Long eventId, Date eventDatetime) {
        super(eventId, eventDatetime);
    }

    @Override
    public void set(Object... objects) {
        if (objects.length >= 5) {
            this.orderId = (String) objects[0];
            this.workflowPath = (String) objects[1];
            this.workflowVersionId = (String) objects[2];
            this.position = (List<?>) objects[3];
            this.arguments = (String) objects[4];
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

    public List<?> getPosition() {
        return position;
    }

    public String getArguments() {
        return arguments;
    }
}
