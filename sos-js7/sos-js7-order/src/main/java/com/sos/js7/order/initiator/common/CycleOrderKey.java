package com.sos.js7.order.initiator.common;

import java.util.Comparator;

public class CycleOrderKey implements Comparable<CycleOrderKey> {

    private String workflowPath = "";
    private String orderName = "";
    private String periodBegin = "";
    private String periodEnd = "";
    private String repeat = "";

    public String getWorkflowPath() {
        return workflowPath;
    }

    public void setWorkflowPath(String val) {
        workflowPath = val;
    }

    public String getOrderName() {
        return orderName;
    }

    public void setOrderName(String val) {
        orderName = val;
    }

    public String getPeriodBegin() {
        return periodBegin;
    }

    public void setPeriodBegin(String val) {
        periodBegin = val;
    }

    public String getPeriodEnd() {
        return periodEnd;
    }

    public void setPeriodEnd(String val) {
        periodEnd = val;
    }

    public String getRepeatInterval() {
        return repeat;
    }

    public void setRepeat(String val) {
        repeat = val;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("workflowPath=").append(workflowPath);
        sb.append(",orderName=").append(orderName);
        sb.append(",periodBegin=").append(periodBegin);
        sb.append(",periodEnd=").append(periodEnd);
        sb.append(",repeat=").append(repeat);
        return sb.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((periodBegin == null) ? 0 : periodBegin.hashCode());
        result = prime * result + ((periodEnd == null) ? 0 : periodEnd.hashCode());
        result = prime * result + ((repeat == null) ? 0 : repeat.hashCode());
        result = prime * result + ((orderName == null) ? 0 : orderName.hashCode());
        result = prime * result + ((workflowPath == null) ? 0 : workflowPath.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        CycleOrderKey other = (CycleOrderKey) obj;
        if (periodBegin == null) {
            if (other.periodBegin != null)
                return false;
        } else if (!periodBegin.equals(other.periodBegin))
            return false;
        if (periodEnd == null) {
            if (other.periodEnd != null)
                return false;
        } else if (!periodEnd.equals(other.periodEnd))
            return false;
        if (repeat == null) {
            if (other.repeat != null)
                return false;
        } else if (!repeat.equals(other.repeat))
            return false;
        if (orderName == null) {
            if (other.orderName != null)
                return false;
        } else if (!orderName.equals(other.orderName))
            return false;
        if (workflowPath == null) {
            if (other.workflowPath != null)
                return false;
        } else if (!workflowPath.equals(other.workflowPath))
            return false;
        return true;
    }

    @Override
    public int compareTo(CycleOrderKey o) {
        return Comparator.comparing(CycleOrderKey::getPeriodBegin).thenComparing(CycleOrderKey::getPeriodEnd).thenComparing(
                CycleOrderKey::getRepeatInterval).thenComparing(CycleOrderKey::getOrderName).thenComparing(CycleOrderKey::getWorkflowPath).compare(
                        this, o);
    }

}
