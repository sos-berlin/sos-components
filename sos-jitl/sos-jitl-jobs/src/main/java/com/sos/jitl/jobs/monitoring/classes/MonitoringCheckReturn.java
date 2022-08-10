package com.sos.jitl.jobs.monitoring.classes;

public class MonitoringCheckReturn {

    private String message;
    private String subject;
    private String body;

    
    public String getBody() {
        return body;
    }

    
    public void setBody(String body) {
        this.body = body;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    private boolean success;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setErrorMessage(String message) {
        this.message = message;
        this.success = false;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void onErrorSetMessage(MonitoringCheckReturn monitoringCheckReturn) {
        if (!monitoringCheckReturn.isSuccess()) {
            message = monitoringCheckReturn.getMessage();
        }

    }

}
