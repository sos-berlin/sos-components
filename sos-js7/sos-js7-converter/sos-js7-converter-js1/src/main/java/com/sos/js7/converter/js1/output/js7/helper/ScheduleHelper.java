package com.sos.js7.converter.js1.output.js7.helper;

import java.util.ArrayList;
import java.util.List;

import com.sos.inventory.model.schedule.OrderParameterisation;

public class ScheduleHelper {

    private final com.sos.js7.converter.js1.common.runtime.Schedule js1Schedule;
    private final List<OrderParameterisation> orderParams;
    private final String timeZone;
    private final String startPosition;

    private List<WorkflowHelper> workflows = new ArrayList<>();

    public ScheduleHelper(com.sos.js7.converter.js1.common.runtime.Schedule schedule, List<OrderParameterisation> orderParams, String timeZone,
            String startPosition) {
        this.js1Schedule = schedule;
        this.orderParams = orderParams;
        this.timeZone = timeZone;
        this.startPosition = startPosition;
    }

    public com.sos.js7.converter.js1.common.runtime.Schedule getJS1Schedule() {
        return js1Schedule;
    }

    public List<OrderParameterisation> getOrderParams() {
        return orderParams;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public String getStartPosition() {
        return startPosition;
    }

    public List<WorkflowHelper> getWorkflows() {
        return workflows;
    }

}
