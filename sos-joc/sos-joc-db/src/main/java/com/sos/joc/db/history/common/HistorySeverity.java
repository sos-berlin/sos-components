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
        case BROKEN:
        case CANCELLED:
        case FAILED:
        case UNKNOWN:
            return FAILED;
        default:
            return INCOMPLETE;
        }
    }

    public static int map2DbSeverity(Integer state) {
        try {
            return map2DbSeverity(OrderStateText.fromValue(state));
        } catch (Throwable e) {
            return map2DbSeverity(OrderStateText.UNKNOWN);
        }
    }

    public static String getName(Integer severity) {
        if (severity == null) {
            return "";
        }
        switch (severity) {
        case 0:
            return "SUCCESSFUL";
        case 1:
            return "INCOMPLETE";
        case 2:
            return "FAILED";
        default:
            return "";
        }
    }
}
