package com.sos.joc.cluster.bean.answer;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import com.sos.joc.cluster.JocCluster;

public class JocServiceAnswer {

    public enum JocServiceAnswerState {
        UNKNOWN, BUSY, RELAX
    }

    private static long RELAX = 10;// seconds
    private static boolean checkJocStartTime = true;

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
            lastActivityStart = ZonedDateTime.ofInstant(start, ZoneId.of("UTC"));
            lastActivityEnd = ZonedDateTime.ofInstant(end, ZoneId.of("UTC"));
            if (lastActivityStart.isAfter(lastActivityEnd)) {
                state = JocServiceAnswerState.BUSY;
                diff = -1; // work
            } else {
                ZonedDateTime now = ZonedDateTime.ofInstant(Instant.now(), ZoneId.of("UTC"));
                diff = Duration.between(now, lastActivityEnd).abs().getSeconds();
                if (diff >= RELAX) {
                    state = JocServiceAnswerState.RELAX;
                } else {
                    if (checkJocStartTime && JocCluster.getJocStartTime() != null) {
                        if (Duration.between(now, ZonedDateTime.ofInstant(JocCluster.getJocStartTime().toInstant(), ZoneId.of("UTC"))).abs()
                                .getSeconds() < 70) {
                            state = JocServiceAnswerState.RELAX;
                            return;
                        }
                    }
                    state = JocServiceAnswerState.BUSY;
                    checkJocStartTime = false;
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

}
