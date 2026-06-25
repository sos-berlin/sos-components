package com.sos.joc.monitoring.model.bean;

import com.sos.commons.util.SOSString;
import com.sos.history.JobWarning;

public class MonitorOrderStepResultWarn extends AMonitorResult {

    private static final long serialVersionUID = 1L;
    private final JobWarning reason;
    private final String text;
    private final boolean invalid;

    public MonitorOrderStepResultWarn(JobWarning reason, String text) {
        this(reason, text, false);
    }

    public MonitorOrderStepResultWarn(boolean invalid) {
        this(null, null, true);
    }

    private MonitorOrderStepResultWarn(JobWarning reason, String text, boolean invalid) {
        this.reason = reason;
        this.text = text;
        this.invalid = invalid;
    }

    public JobWarning getReason() {
        return reason;
    }

    public String getText() {
        return text;
    }

    public boolean isInvalid() {
        return invalid;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("reason=").append(SOSString.toString(reason));
        sb.append(",test=").append(text);
        sb.append(",invalid=").append(invalid);
        return sb.toString();
    }
}
