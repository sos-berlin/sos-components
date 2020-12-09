package com.sos.js7.order.initiator.classes;

public class PlannedOrderKey {

    private String controllerId;
    private String workflowPath;
    private String orderId;

    public String getJobschedulerId() {
        return controllerId;
    }

    public void setControllerId(String controllerId) {
        this.controllerId = controllerId;
    }

    public String getWorkflowPath() {
        return workflowPath;
    }

    public void setWorkflowPath(String workflowPath) {
        this.workflowPath = workflowPath;
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
        result = prime * result + ((workflowPath == null) ? 0 : workflowPath.hashCode());
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
        return (controllerId.equals(other.controllerId) && orderId.equals(other.orderId) && workflowPath.equals(other.workflowPath));

    }

}
