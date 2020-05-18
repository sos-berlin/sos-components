package com.sos.joc.cluster.api.bean;

public class ClusterAnswer {

    public enum ClusterAnswerType {
        SUCCESS, ERROR;
    }

    private ClusterAnswerType type;
    private String message;

    public ClusterAnswerType getType() {
        return type;
    }

    public void setType(ClusterAnswerType val) {
        type = val;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String val) {
        message = val;
    }

}
