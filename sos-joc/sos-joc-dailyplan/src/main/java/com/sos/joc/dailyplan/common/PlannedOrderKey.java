package com.sos.joc.dailyplan.common;

import java.util.Comparator;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class PlannedOrderKey implements Comparable<PlannedOrderKey> {

    private final String controllerId;
    private final String workflowName;
    private final String scheduleName;
    private final String orderId;

    public PlannedOrderKey(String controllerId, String workflowName, String scheduleName, String orderId) {
        this.controllerId = controllerId;
        this.workflowName = workflowName;
        this.scheduleName = scheduleName;
        this.orderId = orderId;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("controllerId=").append(controllerId);
        sb.append(",workflowName=").append(workflowName);
        sb.append(",scheduleName=").append(scheduleName);
        sb.append(",orderId=").append(orderId);
        return sb.toString();
    }

    @Override
    public int hashCode() {
        HashCodeBuilder b = new HashCodeBuilder();
        b.append(controllerId);
        b.append(workflowName);
        b.append(scheduleName);
        b.append(orderId);
        return b.toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (other == null) {
            return false;
        }
        if (getClass() != other.getClass()) {
            return false;
        }
        PlannedOrderKey o = (PlannedOrderKey) other;
        EqualsBuilder b = new EqualsBuilder();
        b.append(controllerId, o.controllerId);
        b.append(workflowName, o.workflowName);
        b.append(scheduleName, o.scheduleName);
        b.append(orderId, o.orderId);
        return b.isEquals();
    }

    @Override
    public int compareTo(PlannedOrderKey other) {
        Comparator<PlannedOrderKey> c = Comparator.comparing(PlannedOrderKey::getControllerId);
        c = c.thenComparing(PlannedOrderKey::getWorkflowName);
        c = c.thenComparing(PlannedOrderKey::getScheduleName);
        c = c.thenComparing(PlannedOrderKey::getOrderId);
        return c.compare(this, other);
    }

    public String getControllerId() {
        return controllerId;
    }

    public String getWorkflowName() {
        return workflowName;
    }

    public String getScheduleName() {
        return scheduleName;
    }

    public String getOrderId() {
        return orderId;
    }

}
