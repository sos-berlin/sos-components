package com.sos.joc.cluster.service;

import org.slf4j.MDC;

import com.sos.joc.model.cluster.common.ClusterServices;

public class JocClusterServiceLogger {

    public static void setLogger() {
        setLogger(ClusterServices.cluster.name());
    }

    public static void removeLogger() {
        removeLogger(ClusterServices.cluster.name());
    }

    public static void setLogger(String identifier) {
        MDC.put("clusterService", "service-" + identifier);
    }

    public static void removeLogger(String identifier) {
        MDC.remove("service-" + identifier);
    }

    public static void clearAllLoggers() {
        MDC.clear();
    }

}
