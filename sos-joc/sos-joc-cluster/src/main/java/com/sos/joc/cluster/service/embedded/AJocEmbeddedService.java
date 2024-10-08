package com.sos.joc.cluster.service.embedded;

import com.sos.joc.cluster.configuration.JocConfiguration;

public abstract class AJocEmbeddedService implements IJocEmbeddedService {

    private final JocConfiguration jocConfig;
    private final ThreadGroup parentThreadGroup;
    private final String identifier;

    private ThreadGroup threadGroup;

    public AJocEmbeddedService(final JocConfiguration jocConfiguration, ThreadGroup clusterThreadGroup, String serviceIdentifier) {
        jocConfig = jocConfiguration;
        parentThreadGroup = clusterThreadGroup;
        threadGroup = new ThreadGroup(parentThreadGroup, serviceIdentifier);
        identifier = serviceIdentifier;
    }

    @Override
    public String getIdentifier() {
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
