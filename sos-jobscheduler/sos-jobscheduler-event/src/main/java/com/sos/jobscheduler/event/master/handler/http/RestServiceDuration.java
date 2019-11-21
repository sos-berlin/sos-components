package com.sos.jobscheduler.event.master.handler.http;

import java.time.Duration;
import java.time.Instant;

import com.sos.commons.util.SOSDate;

public class RestServiceDuration {

    private Instant start;
    private Instant end;

    public void start() {
        start = Instant.now();
    }

    public void end() {
        end = Instant.now();
    }

    public Instant getStart() {
        return start;
    }

    public Instant getEnd() {
        return end;
    }

    public Duration getDuration() {
        if (start != null && end != null) {
            return Duration.between(start, end);
        }
        return null;
    }

    @Override
    public String toString() {
        Duration d = getDuration();
        if (d != null) {
            return new StringBuilder(SOSDate.getTime(start)).append("->").append(SOSDate.getTime(end)).append("=").append(SOSDate.getDuration(d))
                    .toString();
        }
        return null;
    }
}
