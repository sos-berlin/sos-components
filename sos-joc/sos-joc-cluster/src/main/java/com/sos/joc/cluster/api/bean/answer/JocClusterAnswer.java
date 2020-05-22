package com.sos.joc.cluster.api.bean.answer;

public class JocClusterAnswer {

    public enum JocClusterAnswerType {
        SUCCESS, ERROR;
    }

    private JocClusterAnswerType type;
    private JocClusterAnswerError error;

    public JocClusterAnswerType getType() {
        return type;
    }

    public void setType(JocClusterAnswerType val) {
        type = val;
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
