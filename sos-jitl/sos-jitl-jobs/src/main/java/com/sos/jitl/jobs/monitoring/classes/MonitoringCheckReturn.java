package com.sos.jitl.jobs.monitoring.classes;

import com.sos.jitl.jobs.common.Globals;
import com.sos.jitl.jobs.common.JobLogger;

public class MonitoringCheckReturn {

    private String message;
    private Integer count = 0;
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

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public void setErrorMessage(JobLogger logger, String message) {
        Globals.warn(logger, message);
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
