package com.sos.joc.cluster.api.bean;

public class ClusterAnswer {

    public enum ClusterAnswerType {
        SUCCESS, ERROR;
    }

    private ClusterAnswerType type;
    private ClusterAnswerError error;

    public ClusterAnswerType getType() {
        return type;
    }

    public void setType(ClusterAnswerType val) {
        type = val;
    }

    public ClusterAnswerError getError() {
        return error;
    }

    public void setError(ClusterAnswerError val) {
        error = val;
    }

    public void createError(Exception e) {
        type = ClusterAnswerType.ERROR;
        error = new ClusterAnswerError();
        error.setType(e.getClass().getSimpleName());
        error.setMessage(e.toString());
    }
}
