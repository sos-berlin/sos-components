package com.sos.joc.cluster.bean.answer;

import com.sos.joc.model.cluster.common.state.JocClusterState;

public class JocClusterAnswer {

    private JocClusterState state;
    private JocClusterAnswerError error;
    private String message;

    public JocClusterAnswer(JocClusterState val) {
        state = val;
    }

    public JocClusterAnswer(JocClusterState val, String message) {
        this(val);
        this.message = message;
    }

    public JocClusterState getState() {
        return state;
    }

    public boolean isStarted() {
        return JocClusterState.STARTED.equals(state);
    }

    public boolean isStopped() {
        return JocClusterState.STOPPED.equals(state);
    }

    public void setState(JocClusterState val) {
        state = val;
    }

    public JocClusterAnswerError getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    public void setError(Throwable e) {
        error = new JocClusterAnswerError();
        error.setType(e.getClass().getSimpleName());
        error.setMessage(e.toString());
        error.setException(e);
    }
}
