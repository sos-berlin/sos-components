package com.sos.joc.event.bean.history;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class HistoryOrderTaskTerminated extends HistoryTaskEvent {

    public HistoryOrderTaskTerminated(String controllerId, String orderId, String jobName, String severity, Long historyId, Long historyOrderId) {
        super(HistoryOrderTaskTerminated.class.getSimpleName(), controllerId, orderId, jobName, historyId, historyOrderId);
        putVariable("severityText", severity); // HISTORY_ORDER_STEPS.SEVERITY as text
    }

    @JsonIgnore
    public String getSeverityText() {
        return getVariables().get("severityText");
    }
}
