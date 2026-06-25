package com.sos.joc.monitoring.model.bean;

import com.sos.commons.util.SOSString;
import com.sos.joc.cluster.bean.history.HistoryOrderBean;

public class MonitorOrderResult extends AMonitorResult {

    private static final long serialVersionUID = 1L;
    private final HistoryOrderBean order;

    public MonitorOrderResult(HistoryOrderBean order) {
        this.order = order;
    }

    public HistoryOrderBean getOrder() {
        return order;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("order=").append(SOSString.toString(order));
        return sb.toString();
    }

}
