package com.sos.joc.monitoring.model.bean;

import java.util.ArrayList;
import java.util.List;

import com.sos.commons.util.SOSString;
import com.sos.joc.cluster.bean.history.HistoryOrderStepBean;

public class MonitorOrderStepResult extends AMonitorResult {

    private static final long serialVersionUID = 1L;
    private final HistoryOrderStepBean step;
    private final List<MonitorOrderStepResultWarn> warnings = new ArrayList<>();

    public MonitorOrderStepResult(HistoryOrderStepBean step, MonitorOrderStepResultWarn warn) {
        this.step = step;
        addWarn(warn);
    }

    public HistoryOrderStepBean getStep() {
        return step;
    }

    public void addWarn(MonitorOrderStepResultWarn warn) {
        if (warn != null) {
            warnings.add(warn);
        }
    }

    public List<MonitorOrderStepResultWarn> getWarnings() {
        return warnings;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("step=").append(SOSString.toString(step));
        sb.append(", warnings=").append(warnings == null ? "null" : warnings.toString());
        return sb.toString();
    }
}
