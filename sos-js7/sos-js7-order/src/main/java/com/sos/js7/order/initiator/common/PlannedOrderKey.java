package com.sos.js7.order.initiator.common;

public class PlannedOrderKey implements Comparable<PlannedOrderKey> {

    private String controllerId;
    private String workflowName;
    private String orderId;

    public String getJobschedulerId() {
        return controllerId;
    }

    public void setControllerId(String val) {
        controllerId = val;
    }

    public String getWorkflowName() {
        return workflowName;
    }

    public void setWorkflowName(String val) {
        workflowName = val;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String val) {
        orderId = val;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("controllerId=").append(controllerId);
        sb.append(",workflowName=").append(workflowName);
        sb.append(",orderId=").append(orderId);
        return sb.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((controllerId == null) ? 0 : controllerId.hashCode());
        result = prime * result + ((orderId == null) ? 0 : orderId.hashCode());
        result = prime * result + ((workflowName == null) ? 0 : workflowName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        PlannedOrderKey other = (PlannedOrderKey) obj;
        return (controllerId.equals(other.controllerId) && orderId.equals(other.orderId) && workflowName.equals(other.workflowName));

    }

    @Override
    public int compareTo(PlannedOrderKey o) {
        return this.orderId.compareTo(o.orderId);
    }

}
