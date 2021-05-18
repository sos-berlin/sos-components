package com.sos.commons.util.common;

import java.util.concurrent.TimeUnit;

public class SOSTimeout {

    private final long timeout;
    private final TimeUnit unit;

    public SOSTimeout(long timeout, TimeUnit unit) {
        this.timeout = timeout;
        this.unit = unit;
    }

    public long getTimeout() {
        return timeout;
    }

    public TimeUnit getUnit() {
        return unit;
    }
}
