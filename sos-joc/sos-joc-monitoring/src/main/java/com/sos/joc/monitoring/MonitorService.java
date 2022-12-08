package com.sos.joc.monitoring;

import com.sos.joc.cluster.service.JocClusterServiceLogger;
import com.sos.joc.model.cluster.common.ClusterServices;

public class MonitorService {

    public static final String MAIN_SERVICE_IDENTIFIER = ClusterServices.monitor.name();
    public static final String SUB_SERVICE_IDENTIFIER_HISTORY = MonitorService.getIdentifier(ClusterServices.history.name());
    public static final String SUB_SERVICE_IDENTIFIER_SYSTEM = MonitorService.getIdentifier("system");

    public static final String NOTIFICATION_IDENTIFIER = "notification";

    public static void setLogger() {
        JocClusterServiceLogger.setLogger(MAIN_SERVICE_IDENTIFIER);
    }

    public static void removeLogger() {
        JocClusterServiceLogger.removeLogger(MAIN_SERVICE_IDENTIFIER);
    }

    public static String getIdentifier(String caller) {
        return MAIN_SERVICE_IDENTIFIER + "_" + caller;
    }

}
