package com.sos.js7.history.controller.proxy.fatevent;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sos.js7.event.controller.EventMeta;

import js7.data.value.Value;

public abstract class AFatEventOrder extends AFatEvent {

    private String orderId;

    private String workflowPath;
    private String workflowVersionId;

    private List<?> position;
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
            this.position = (List<?>) objects[3];
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

    public List<?> getPosition() {
        return position;
    }

    public String getArgumentsAsJsonString() throws JsonProcessingException {
        Map<String, String> map = null;
        if (arguments != null) {
            map = arguments.entrySet().stream().collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue().toString()));
        }
        return EventMeta.map2Json(map);
    }
}
