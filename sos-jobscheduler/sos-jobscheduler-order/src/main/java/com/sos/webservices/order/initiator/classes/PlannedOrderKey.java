package com.sos.webservices.order.initiator.classes;


public class PlannedOrderKey {
    String jobschedulerId;
    String workflowPath;
    String orderId;
    
   
    
    public String getJobschedulerId() {
        return jobschedulerId;
    }

    
    public void setJobschedulerId(String jobschedulerId) {
        this.jobschedulerId = jobschedulerId;
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
        result = prime * result + ((jobschedulerId == null) ? 0 : jobschedulerId.hashCode());
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
        return (jobschedulerId.equals(other.jobschedulerId) && orderId.equals(other.orderId) && workflowPath.equals(other.workflowPath));
  
    } 

 
}
