package com.sos.joc.db.history.common;

import com.sos.joc.model.order.OrderStateText;

public class HistorySeverity {

    public static final int SUCCESSFUL = 0;
    public static final int INCOMPLETE = 1;
    public static final int FAILED = 2;

    public static int map2DbSeverity(OrderStateText state) {
        switch (state) {
        case FINISHED:
            return SUCCESSFUL;
        case PLANNED:
        case PENDING:
        case RUNNING:
        case WAITING:
        case BLOCKED:
        case SUSPENDED:
            return INCOMPLETE;
        case BROKEN:
        case CANCELLED:
        case FAILED:
        case UNKNOWN:
            return FAILED;
        }
        return FAILED;
    }

    public static int map2DbSeverity(Integer state) {
        return map2DbSeverity(OrderStateText.fromValue(state));
    }
}
