package com.sos.joc.cluster.bean.answer;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class JocServiceAnswer {

    public enum JocServiceAnswerState {
        BUSY, RELAX, UNKNOWN
    }

    private JocServiceAnswerState state;
    private ZonedDateTime lastActivityStart;
    private ZonedDateTime lastActivityEnd;
    private long nowMinutesDiff;

    public JocServiceAnswer() {
        this(null, null);
    }

    public JocServiceAnswer(Instant start, Instant end) {
        if (start == null || end == null) {
            state = JocServiceAnswerState.UNKNOWN;
            lastActivityStart = null;
            lastActivityEnd = null;
        } else {
            lastActivityStart = ZonedDateTime.ofInstant(start, ZoneId.of("UTC"));
            lastActivityEnd = ZonedDateTime.ofInstant(end, ZoneId.of("UTC"));
            if (lastActivityStart.isAfter(lastActivityEnd)) {
                state = JocServiceAnswerState.BUSY;
            } else {
                ZonedDateTime now = ZonedDateTime.ofInstant(Instant.now(), ZoneId.of("UTC"));
                nowMinutesDiff = Duration.between(now, this.lastActivityEnd).abs().toMinutes();
                state = nowMinutesDiff >= 1 ? JocServiceAnswerState.RELAX : JocServiceAnswerState.BUSY;
            }
        }
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

    public long getNowMinutesDiff() {
        return nowMinutesDiff;
    }
}
