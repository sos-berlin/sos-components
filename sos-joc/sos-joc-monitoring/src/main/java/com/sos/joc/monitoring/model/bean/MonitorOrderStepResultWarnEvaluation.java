package com.sos.joc.monitoring.model.bean;

import java.io.Serializable;

public class MonitorOrderStepResultWarnEvaluation implements Serializable {

    private static final long serialVersionUID = 1L;
    private final boolean applicable;
    private final String reason;
    private final Long elapsedSecond;

    public MonitorOrderStepResultWarnEvaluation(boolean applicable) {
        this(applicable, null, null);
    }

    public MonitorOrderStepResultWarnEvaluation(boolean applicable, String reason, Long elapsedSecond) {
        this.applicable = applicable;
        this.reason = reason;
        this.elapsedSecond = elapsedSecond;
    }

    public String getReason() {
        return reason;
    }

    public Long getElapsedSecond() {
        return elapsedSecond;
    }

    public boolean isApplicable() {
        return applicable;
    }

}
