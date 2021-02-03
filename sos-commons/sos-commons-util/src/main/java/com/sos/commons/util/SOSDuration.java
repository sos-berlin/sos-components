package com.sos.commons.util;

import java.util.Date;

public class SOSDuration implements Comparable<SOSDuration> {

    private Date startTime;
    private Date endTime;

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public Long getDurationInMillis() {
        Long durationInMillis = 0L;
        if (startTime != null && endTime != null) {
            durationInMillis = endTime.getTime() - startTime.getTime();
        }
        return durationInMillis;
    }

    public Long getDurationInSeconds() {
        return getDurationInMillis() / 1000;
    }

    @Override
    public boolean equals(Object o) {
        return getDurationInMillis().equals(((SOSDuration) o).getDurationInMillis());
    }

    @Override
    public int compareTo(SOSDuration o) {
        return Long.compare(getDurationInMillis(), o.getDurationInMillis());
    }

}
