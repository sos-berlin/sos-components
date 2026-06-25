package com.sos.joc.monitoring.model;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import com.sos.joc.cluster.bean.history.AHistoryBean;
import com.sos.joc.cluster.bean.history.HistoryOrderStepBean;
import com.sos.joc.monitoring.model.bean.AMonitorResult;
import com.sos.joc.monitoring.model.bean.NotifierTask;

public class SerializedHistoryResult implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Collection<AHistoryBean> payloads;
    private final Map<Long, HistoryOrderStepBean> longerThan;
    private final Collection<AMonitorResult> notifierCandidates;
    private final Set<NotifierTask> notifierActive;

    public SerializedHistoryResult(Collection<AHistoryBean> payloads, Map<Long, HistoryOrderStepBean> longerThan,
            Collection<AMonitorResult> notifierCandidates, Set<NotifierTask> notifierActive) {
        this.payloads = payloads;
        this.longerThan = longerThan;
        this.notifierCandidates = notifierCandidates;
        this.notifierActive = notifierActive;
    }

    public Collection<AHistoryBean> getPayloads() {
        return payloads;
    }

    public Map<Long, HistoryOrderStepBean> getLongerThan() {
        return longerThan;
    }

    public Collection<AMonitorResult> getNotifierCandidates() {
        return notifierCandidates;
    }

    public Set<NotifierTask> getNotifierActive() {
        return notifierActive;
    }
}
