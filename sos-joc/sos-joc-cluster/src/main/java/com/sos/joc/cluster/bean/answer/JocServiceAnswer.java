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
        this(null, 0, 0);
    }

    public JocServiceAnswer(long lastActivityStart, long lastActivityEnd) {
        this(null, lastActivityStart, lastActivityEnd);
    }

    private JocServiceAnswer(JocServiceAnswerState state, long lastActivityStart, long lastActivityEnd) {
        if (lastActivityStart == 0 || lastActivityEnd == 0) {
            this.state = JocServiceAnswerState.UNKNOWN;
            this.lastActivityStart = null;
            this.lastActivityEnd = null;
        } else {
            this.lastActivityStart = ZonedDateTime.ofInstant(Instant.ofEpochMilli(lastActivityStart), ZoneId.of("UTC"));
            this.lastActivityEnd = ZonedDateTime.ofInstant(Instant.ofEpochMilli(lastActivityEnd), ZoneId.of("UTC"));
            if (lastActivityStart > lastActivityEnd) {
                this.state = JocServiceAnswerState.BUSY;
            } else {
                ZonedDateTime now = ZonedDateTime.ofInstant(Instant.now(), ZoneId.of("UTC"));
                this.nowMinutesDiff = Duration.between(now, this.lastActivityEnd).abs().toMinutes();
                this.state = this.nowMinutesDiff >= 1 ? JocServiceAnswerState.RELAX : JocServiceAnswerState.BUSY;
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
