package com.sos.js7.event.controller.fatevent.bean;

import java.util.Arrays;

import com.google.common.base.Joiner;
import com.sos.commons.util.SOSString;

public class WorkflowPosition {

    public static final String DELIMITER = "#";
    private WorkflowId workflowId;
    private String[] position;

    public WorkflowId getWorkflowId() {
        return workflowId;
    }

    public void setWorkflowId(WorkflowId val) {
        workflowId = val;
    }

    public String[] getPosition() {
        return position;
    }

    public void setPosition(String[] val) {
        position = val;
    }

    public String getPositionAsString() {
        return Joiner.on(DELIMITER).join(position);
    }

    public Long getRetry() {
        for (int i = 0; i < position.length; i++) {
            String part = position[i];
            if (part.startsWith("try+")) {
                return Long.parseLong(part.substring(3)); // TODO
            }
        }
        return new Long(0);
    }

    public String getOrderPositionAsString() {// 0->0, 1#fork_1#0 -> 1#fork_1
        return getOrderPositionAsString(position);
    }

    public static String getOrderPositionAsString(String[] pos) {// 0->0, 1#fork_1#0 -> 1#fork_1
        if (pos == null || pos.length < 1) {
            return null;
        }
        if (pos.length == 1) {
            return pos[0];
        }
        return Joiner.on(DELIMITER).join(Arrays.copyOf(pos, pos.length - 1));
    }

    public Long getLastPosition() {
        if (position == null || position.length < 1) {
            return null;
        }
        return Long.parseLong(position[position.length - 1]);
    }

    @Override
    public String toString() {
        return SOSString.toString(this);
    }
}
