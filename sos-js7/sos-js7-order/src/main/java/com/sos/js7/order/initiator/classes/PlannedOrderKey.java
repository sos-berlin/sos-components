package com.sos.js7.order.initiator.classes;

public class PlannedOrderKey implements Comparable<PlannedOrderKey>  {

    private String controllerId;
    private String workflowName;
    private String orderId;

    public String getJobschedulerId() {
        return controllerId;
    }

    public void setControllerId(String controllerId) {
        this.controllerId = controllerId;
    }

    public String getWorkflowName() {
        return workflowName;
    }

    public void setWorkflowName(String workflowName) {
        this.workflowName = workflowName;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
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
