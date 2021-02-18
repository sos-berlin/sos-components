package com.sos.js7.order.initiator.classes;

import java.util.Comparator;

public class CycleOrderKey implements Comparable<CycleOrderKey> {

    private String periodBegin="";
    private String periodEnd="";
    private String repeat="";
    private String schedulePath="";
    private String workflowPath="";

    public String getPeriodBegin() {
        return periodBegin;
    }

    public void setPeriodBegin(String periodBegin) {
        this.periodBegin = periodBegin;
    }

    public String getPeriodEnd() {
        return periodEnd;
    }

    public void setPeriodEnd(String periodEnd) {
        this.periodEnd = periodEnd;
    }

    public String getRepeatInterval() {
        return repeat;
    }

    public void setRepeat(String repeat) {
        this.repeat = repeat;
    }

    public String getSchedulePath() {
        return schedulePath;
    }

    public void setSchedulePath(String schedulePath) {
        this.schedulePath = schedulePath;
    }

    public String getWorkflowPath() {
        return workflowPath;
    }

    public void setWorkflowPath(String workflowPath) {
        this.workflowPath = workflowPath;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((periodBegin == null) ? 0 : periodBegin.hashCode());
        result = prime * result + ((periodEnd == null) ? 0 : periodEnd.hashCode());
        result = prime * result + ((repeat == null) ? 0 : repeat.hashCode());
        result = prime * result + ((schedulePath == null) ? 0 : schedulePath.hashCode());
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
        if (schedulePath == null) {
            if (other.schedulePath != null)
                return false;
        } else if (!schedulePath.equals(other.schedulePath))
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
                CycleOrderKey::getRepeatInterval).thenComparing(CycleOrderKey::getSchedulePath).thenComparing(CycleOrderKey::getWorkflowPath).compare(
                        this, o);
    }

}
