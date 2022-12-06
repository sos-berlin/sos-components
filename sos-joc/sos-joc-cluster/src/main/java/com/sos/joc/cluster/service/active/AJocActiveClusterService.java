package com.sos.joc.cluster.service.active;

import com.sos.joc.cluster.configuration.JocConfiguration;

public abstract class AJocActiveClusterService implements IJocActiveClusterService {

    private final JocConfiguration jocConfig;
    private final ThreadGroup parentThreadGroup;
    private final String identifier;

    private ThreadGroup threadGroup;

    public AJocActiveClusterService(final JocConfiguration jocConf, ThreadGroup clusterThreadGroup, String serviceIdentifier) {
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
}
