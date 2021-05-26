package com.sos.joc.cluster.bean.answer;

public class JocClusterAnswer {

    public enum JocClusterAnswerState {
        STARTED, STOPPED, RESTARTED, ALREADY_STARTED, ALREADY_STOPPED, SWITCH, MISSING_CONFIGURATION, MISSING_HANDLERS, MISSING_LICENSE, ERROR, COMPLETED, UNCOMPLETED
    }

    private JocClusterAnswerState state;
    private JocClusterAnswerError error;
    private String message;

    public JocClusterAnswer(JocClusterAnswerState val) {
        state = val;
    }

    public JocClusterAnswer(JocClusterAnswerState val, String message) {
        this(val);
        this.message = message;
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

    public String getMessage() {
        return message;
    }

    public void setError(Exception e) {
        error = new JocClusterAnswerError();
        error.setType(e.getClass().getSimpleName());
        error.setMessage(e.toString());
        error.setException(e);
    }
}
