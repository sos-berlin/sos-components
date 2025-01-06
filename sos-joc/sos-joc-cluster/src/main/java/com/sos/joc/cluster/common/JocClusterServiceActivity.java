package com.sos.joc.cluster.common;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import com.sos.commons.util.SOSDate;
import com.sos.joc.model.cluster.common.state.JocClusterServiceActivityState;

public class JocClusterServiceActivity {

    private static final ZoneId ZONE_ID = ZoneId.of(SOSDate.TIMEZONE_UTC);

    private static long RELAX = 10;// seconds

    private JocClusterServiceActivityState state;
    private ZonedDateTime lastStart;
    private ZonedDateTime lastEnd;
    private long diff;

    public static JocClusterServiceActivity Relax() {
        return new JocClusterServiceActivity(JocClusterServiceActivityState.RELAX);
    }

    public static JocClusterServiceActivity Busy() {
        return new JocClusterServiceActivity(JocClusterServiceActivityState.BUSY);
    }

    private JocClusterServiceActivity(JocClusterServiceActivityState state) {
        this.state = state;
    }

    public JocClusterServiceActivity(Instant start, Instant end) {
        diff = 0;
        if (start == null || end == null) {
            state = JocClusterServiceActivityState.RELAX;
            lastStart = null;
            lastEnd = null;
        } else {
            lastStart = ZonedDateTime.ofInstant(start, ZONE_ID);
            lastEnd = ZonedDateTime.ofInstant(end, ZONE_ID);
            if (lastStart.isAfter(lastEnd)) {
                state = JocClusterServiceActivityState.BUSY;
                diff = -1;
            } else {
                ZonedDateTime now = ZonedDateTime.ofInstant(Instant.now(), ZONE_ID);
                diff = Duration.between(now, lastEnd).abs().getSeconds();
                if (diff >= RELAX) {
                    state = JocClusterServiceActivityState.RELAX;
                } else {
                    state = JocClusterServiceActivityState.BUSY;
                }
            }
        }
    }

    public long getDiff() {
        return diff;
    }

    public JocClusterServiceActivityState getState() {
        return state;
    }

    public ZonedDateTime getLastStart() {
        return lastStart;
    }

    public ZonedDateTime getLastEnd() {
        return lastEnd;
    }

    public boolean isBusy() {
        return JocClusterServiceActivityState.BUSY.equals(state);
    }

    public boolean isRelax() {
        return JocClusterServiceActivityState.RELAX.equals(state);
    }
}
