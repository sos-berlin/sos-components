package com.sos.commons.util.common;

import java.util.concurrent.TimeUnit;

public class SOSTimeout {

    private final long interval;
    private final TimeUnit timeUnit;

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
}
