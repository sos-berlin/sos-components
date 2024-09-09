package com.sos.joc.cluster.bean.answer;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import com.sos.joc.model.cluster.common.state.JocClusterServiceState;

public class JocServiceAnswer {

    private static final ZoneId ZONE_ID = ZoneId.of("Etc/UTC");

    private static long RELAX = 10;// seconds

    private JocClusterServiceState state;
    private ZonedDateTime lastActivityStart;
    private ZonedDateTime lastActivityEnd;
    private long diff;

    public JocServiceAnswer() {
        this(null, null);
    }

    public JocServiceAnswer(JocClusterServiceState state) {
        this.state = state;
    }

    public JocServiceAnswer(Instant start, Instant end) {
        diff = 0;
        if (start == null || end == null) {
            state = JocClusterServiceState.UNKNOWN;
            lastActivityStart = null;
            lastActivityEnd = null;
        } else {
            lastActivityStart = ZonedDateTime.ofInstant(start, ZONE_ID);
            lastActivityEnd = ZonedDateTime.ofInstant(end, ZONE_ID);
            if (lastActivityStart.isAfter(lastActivityEnd)) {
                state = JocClusterServiceState.BUSY;
                diff = -1;
            } else {
                ZonedDateTime now = ZonedDateTime.ofInstant(Instant.now(), ZONE_ID);
                diff = Duration.between(now, lastActivityEnd).abs().getSeconds();
                if (diff >= RELAX) {
                    state = JocClusterServiceState.RELAX;
                } else {
                    state = JocClusterServiceState.BUSY;
                }
            }
        }
    }

    public long getDiff() {
        return diff;
    }

    public JocClusterServiceState getState() {
        return state;
    }

    public ZonedDateTime getLastActivityStart() {
        return lastActivityStart;
    }

    public ZonedDateTime getLastActivityEnd() {
        return lastActivityEnd;
    }

    public boolean isBusyState() {
        return JocClusterServiceState.BUSY.equals(state);
    }

}
