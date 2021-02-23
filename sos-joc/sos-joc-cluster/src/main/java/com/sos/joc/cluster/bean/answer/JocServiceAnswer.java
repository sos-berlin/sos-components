package com.sos.joc.cluster.bean.answer;

public class JocServiceAnswer {

    public enum JocServiceAnswerState {
        BUSY, RELAX, UNKNOWN
    }

    private JocServiceAnswerState state;
    private long lastActivityStart;
    private long lastActivityEnd;

    public JocServiceAnswer() {
        this(null, 0, 0);
    }

    public JocServiceAnswer(long lastActivityStart, long lastActivityEnd) {
        this(null, lastActivityStart, lastActivityEnd);
    }

    public JocServiceAnswer(JocServiceAnswerState state, long lastActivityStart, long lastActivityEnd) {
        if (state == null) {
            if (lastActivityStart == 0 && lastActivityEnd == 0) {
                state = JocServiceAnswerState.UNKNOWN;
            } else {
                state = lastActivityStart > lastActivityEnd ? JocServiceAnswerState.BUSY : JocServiceAnswerState.RELAX;
            }
        }
        this.state = state;
        this.lastActivityStart = lastActivityStart;
        this.lastActivityEnd = lastActivityEnd;
    }

    public JocServiceAnswerState getState() {
        return state;
    }

    public long getLastActivityStart() {
        return lastActivityStart;
    }

    public long getLastActivityEnd() {
        return lastActivityEnd;
    }
}
