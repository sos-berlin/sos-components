package com.sos.joc.cluster.bean.answer;

public class JocClusterAnswer {

    public enum JocClusterAnswerState {
        STARTED, STOPPED, RESTARTED, ALREADY_STARTED, ALREADY_STOPPED, SWITCH, MISSING_HANDLERS_CONFIGURATION, MISSING_HANDLERS, ERROR
    }

    private JocClusterAnswerState state;
    private JocClusterAnswerError error;

    public JocClusterAnswer(JocClusterAnswerState answerState) {
        state = answerState;
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

    public void setError(Exception e) {
        error = new JocClusterAnswerError();
        error.setType(e.getClass().getSimpleName());
        error.setMessage(e.toString());
        error.setException(e);
    }
}
