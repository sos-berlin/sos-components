package com.sos.joc.cluster.bean.answer;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import com.sos.joc.cluster.JocCluster;

public class JocServiceAnswer {

    public enum JocServiceAnswerState {
        BUSY, RELAX, UNKNOWN
    }

    private static boolean checkJocStartTime = true;

    private JocServiceAnswerState state;
    private ZonedDateTime lastActivityStart;
    private ZonedDateTime lastActivityEnd;

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
                if (Duration.between(now, lastActivityEnd).abs().toMinutes() >= 1) {
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
