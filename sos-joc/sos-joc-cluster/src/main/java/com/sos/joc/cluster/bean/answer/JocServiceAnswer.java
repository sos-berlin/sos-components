package com.sos.joc.cluster.bean.answer;

public class JocServiceAnswer {

    public enum JocServiceAnswerState {
        BUSY, RELAX, UNKNOWN
    }

    private JocServiceAnswerState state;
    private long lastActivityStart;
    private long lastActivityEnd;

    public JocServiceAnswer(JocServiceAnswerState state) {
        this(state, 0, 0);
    }

    public JocServiceAnswer(JocServiceAnswerState state, long lastActivityStart, long lastActivityEnd) {
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
