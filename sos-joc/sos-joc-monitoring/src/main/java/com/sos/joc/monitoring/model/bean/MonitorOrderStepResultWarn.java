package com.sos.joc.monitoring.model.bean;

import java.util.Date;

import com.sos.commons.util.SOSDate;
import com.sos.commons.util.SOSString;
import com.sos.history.JobWarning;
import com.sos.joc.db.monitoring.DBItemMonitoringOrderStep;

public class MonitorOrderStepResultWarn extends AMonitorResult {

    private static final long serialVersionUID = 1L;

    public static final long NO_EXPECTED_SECONDS = -1L;

    private final JobWarning reason;
    private final long expectedSeconds;
    private final String text;
    private final boolean invalid;

    public MonitorOrderStepResultWarn(JobWarning reason, String text) {
        this(reason, NO_EXPECTED_SECONDS, text, false);
    }

    public MonitorOrderStepResultWarn(JobWarning reason, long expectedSeconds, String text) {
        this(reason, expectedSeconds, text, false);
    }

    public MonitorOrderStepResultWarn(boolean invalid) {
        this(null, NO_EXPECTED_SECONDS, null, true);
    }

    private MonitorOrderStepResultWarn(JobWarning reason, long expectedSeconds, String text, boolean invalid) {
        this.reason = reason;
        this.expectedSeconds = expectedSeconds;
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

    public MonitorOrderStepResultWarnEvaluation evaluate(DBItemMonitoringOrderStep orderStep) {
        if (!JobWarning.LONGER_THAN.equals(reason) || expectedSeconds == NO_EXPECTED_SECONDS || orderStep.getEndTime() == null) {
            return new MonitorOrderStepResultWarnEvaluation(true);
        }

        long elapsedSeconds = calculateElapsedSeconds(orderStep.getStartTime(), orderStep.getEndTime());
        boolean applicable = elapsedSeconds > expectedSeconds;
        String reason = null;
        if (!applicable) {
            reason = "Actual duration (" + elapsedSeconds + "s) no longer exceeds expected duration(" + expectedSeconds + "s)";
        }
        return new MonitorOrderStepResultWarnEvaluation(applicable, reason, expectedSeconds);
    }

    public static long calculateElapsedSeconds(Date startTime, Date endTime) {
        return SOSDate.getSeconds(endTime) - SOSDate.getSeconds(startTime);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("reason=").append(SOSString.toString(reason));
        sb.append(",expectedSeconds=").append(expectedSeconds);
        sb.append(",test=").append(text);
        sb.append(",invalid=").append(invalid);
        return sb.toString();
    }
}
