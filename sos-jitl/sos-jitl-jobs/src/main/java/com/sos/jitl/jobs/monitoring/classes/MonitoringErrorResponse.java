package com.sos.jitl.jobs.monitoring.classes;

public class MonitoringErrorResponse {

    private String message = "";
    private MonitoringError error = new MonitoringError();

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public MonitoringError getError() {
        return error;
    }

    public void setError(MonitoringError error) {
        this.error = error;
    }

}
