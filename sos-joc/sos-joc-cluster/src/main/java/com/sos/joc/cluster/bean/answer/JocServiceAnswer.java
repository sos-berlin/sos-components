package com.sos.joc.cluster.bean.answer;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class JocServiceAnswer {

    public enum JocServiceAnswerState {
        UNKNOWN, BUSY, RELAX
    }

    private static final ZoneId ZONE_ID = ZoneId.of("Etc/UTC");

    private static long RELAX = 10;// seconds

    private JocServiceAnswerState state;
    private ZonedDateTime lastActivityStart;
    private ZonedDateTime lastActivityEnd;
    private long diff;

    public JocServiceAnswer() {
        this(null, null);
    }

    public JocServiceAnswer(JocServiceAnswerState state) {
        this.state = state;
    }

    public JocServiceAnswer(Instant start, Instant end) {
        diff = 0;
        if (start == null || end == null) {
            state = JocServiceAnswerState.UNKNOWN;
            lastActivityStart = null;
            lastActivityEnd = null;
        } else {
            lastActivityStart = ZonedDateTime.ofInstant(start, ZONE_ID);
            lastActivityEnd = ZonedDateTime.ofInstant(end, ZONE_ID);
            if (lastActivityStart.isAfter(lastActivityEnd)) {
                state = JocServiceAnswerState.BUSY;
                diff = -1;
            } else {
                ZonedDateTime now = ZonedDateTime.ofInstant(Instant.now(), ZONE_ID);
                diff = Duration.between(now, lastActivityEnd).abs().getSeconds();
                if (diff >= RELAX) {
                    state = JocServiceAnswerState.RELAX;
                } else {
                    state = JocServiceAnswerState.BUSY;
                }
            }
        }
    }

    public long getDiff() {
        return diff;
    }

    public JocServiceAnswerState getState() {
        return state;
    }

    public ZonedDateTime getLastActivityStart() {
        return lastActivityStart;
    }

    public ZonedDateTime getLastActivityEnd() {
        return lastActivityEnd;
    }

    public boolean isBusyState() {
        return JocServiceAnswerState.BUSY.equals(state);
    }

}
