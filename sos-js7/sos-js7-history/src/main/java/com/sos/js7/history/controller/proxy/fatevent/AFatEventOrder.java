package com.sos.js7.history.controller.proxy.fatevent;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sos.joc.classes.history.HistoryPosition;
import com.sos.js7.history.helper.HistoryUtil;

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
            this.position = HistoryPosition.asString((List<?>) objects[3]);
            this.arguments = (Map<String, Value>) objects[4];
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

    public String getArgumentsAsJsonString() throws JsonProcessingException {
        return HistoryUtil.map2Json(arguments);
    }
}
