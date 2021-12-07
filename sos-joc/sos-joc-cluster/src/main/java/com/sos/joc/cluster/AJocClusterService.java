package com.sos.joc.cluster;

import org.slf4j.MDC;

import com.sos.joc.cluster.configuration.JocConfiguration;
import com.sos.joc.model.cluster.common.ClusterServices;

public abstract class AJocClusterService implements IJocClusterService {

    private final JocConfiguration jocConfig;
    private final ThreadGroup parentThreadGroup;
    private final String identifier;

    private ThreadGroup threadGroup;

    public AJocClusterService(final JocConfiguration jocConf, ThreadGroup clusterThreadGroup, String serviceIdentifier) {
        jocConfig = jocConf;
        parentThreadGroup = clusterThreadGroup;
        threadGroup = new ThreadGroup(parentThreadGroup, serviceIdentifier);
        identifier = serviceIdentifier;
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public String getControllerApiUser() {
        return identifier;
    }

    @Override
    public String getControllerApiUserPassword() {
        return identifier;
    }

    public JocConfiguration getJocConfig() {
        return jocConfig;
    }

    @Override
    public ThreadGroup getThreadGroup() {
        if (threadGroup == null || threadGroup.isDestroyed()) {
            threadGroup = new ThreadGroup(parentThreadGroup, identifier);
        }
        return threadGroup;
    }

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
