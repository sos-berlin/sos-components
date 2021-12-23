package com.sos.joc.monitoring.model;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

import com.sos.joc.cluster.bean.history.AHistoryBean;
import com.sos.joc.cluster.bean.history.HistoryOrderStepBean;

public class SerializedHistoryResult implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Collection<AHistoryBean> payloads;
    private final Map<Long, HistoryOrderStepBean> longerThan;

    public SerializedHistoryResult(Collection<AHistoryBean> payloads, Map<Long, HistoryOrderStepBean> longerThan) {
        this.payloads = payloads;
        this.longerThan = longerThan;
    }

    public Collection<AHistoryBean> getPayloads() {
        return payloads;
    }

    public Map<Long, HistoryOrderStepBean> getLongerThan() {
        return longerThan;
    }
}
