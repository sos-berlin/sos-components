package com.sos.joc.cluster.bean.answer;

public class JocClusterAnswerError {

    private String type;
    private String message;
    private Throwable exception;

    public String getType() {
        return type;
    }

    public void setType(String val) {
        type = val;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String val) {
        message = val;
    }

    public Throwable getException() {
        return exception;
    }

    public void setException(Throwable e) {
        exception = e;
    }

}
