package com.sos.joc.history.controller.proxy.fatevent;

import com.sos.joc.history.controller.proxy.HistoryEventEntry.HistoryOrder.WorkflowInfo.Position;

public class FatPosition {

    private final String value;
    private final String origIfDiff;

    public FatPosition(Position p) {
        if (p.asResolvedString() == null) {
            this.value = p.asString();
            this.origIfDiff = null;
        } else {
            this.value = p.asResolvedString();
            this.origIfDiff = p.asString();
        }
    }

    public String getValue() {
        return value;
    }

    public String getOrigIfDiff() {
        return origIfDiff;
    }
}
