package com.sos.commons.util.common;

import java.util.concurrent.TimeUnit;

import com.sos.commons.util.SOSString;

public class SOSTimeout {

    private long interval;
    private TimeUnit timeUnit;

    public SOSTimeout(String timeout) throws Exception {
        resolve(timeout);
    }

    public SOSTimeout(long interval, TimeUnit timeUnit) {
        this.interval = interval;
        this.timeUnit = timeUnit;
    }

    public long getInterval() {
        return interval;
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    /** n NANOSECONDS|MICROSECONDS|MILLISECONDS|SECONDS|MINUTES|HOURS|DAYS
     * 
     * @param val */
    private void resolve(String val) throws Exception {
        if (SOSString.isEmpty(val)) {
            throw new Exception("missing input");
        }
        String[] v = val.split(" ");
        interval = Long.parseLong(v[0].trim());
        if (v.length == 1) {
            timeUnit = TimeUnit.SECONDS;
        } else {
            timeUnit = TimeUnit.valueOf(v[1].trim().toUpperCase());
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(interval + "");
        sb.append(" ").append(timeUnit == null ? "" : timeUnit.toString());
        return sb.toString();
    }
}
