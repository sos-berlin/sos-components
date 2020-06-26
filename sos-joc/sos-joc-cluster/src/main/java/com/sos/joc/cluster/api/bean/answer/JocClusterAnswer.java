package com.sos.joc.cluster.api.bean.answer;

public class JocClusterAnswer {

    public enum JocClusterAnswerType {
        SUCCESS, ERROR;
    }

    public enum JocClusterAnswerState {
        STARTED, STOPPED, RESTARTED, ALREADY_STARTED, ALREADY_STOPPED, WAITING_FOR_RESOURCES;
    }

    private JocClusterAnswerType type;
    private JocClusterAnswerState state;
    private JocClusterAnswerError error;

    public JocClusterAnswerType getType() {
        return type;
    }

    public void setType(JocClusterAnswerType val) {
        type = val;
    }

    public JocClusterAnswerState getState() {
        return state;
    }

    public void setState(JocClusterAnswerState val) {
        state = val;
    }

    public JocClusterAnswerError getError() {
        return error;
    }

    public void setError(JocClusterAnswerError val) {
        error = val;
    }

    public void createError(Exception e) {
        type = JocClusterAnswerType.ERROR;
        error = new JocClusterAnswerError();
        error.setType(e.getClass().getSimpleName());
        error.setMessage(e.toString());
    }
}
