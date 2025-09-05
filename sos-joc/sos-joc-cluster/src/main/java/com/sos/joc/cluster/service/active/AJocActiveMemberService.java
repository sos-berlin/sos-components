package com.sos.joc.cluster.service.active;

import java.util.List;

import com.sos.joc.cluster.bean.answer.JocClusterAnswer;
import com.sos.joc.cluster.configuration.JocConfiguration;
import com.sos.joc.cluster.configuration.JocClusterConfiguration.StartupMode;
import com.sos.joc.cluster.configuration.controller.ControllerConfiguration;
import com.sos.joc.cluster.configuration.globals.common.AConfigurationSection;
import com.sos.joc.model.cluster.common.state.JocClusterState;

public abstract class AJocActiveMemberService implements IJocActiveMemberService {

    private final JocConfiguration jocConfig;
    private final ThreadGroup parentThreadGroup;
    private final String identifier;

    private ThreadGroup threadGroup;

    public AJocActiveMemberService(final JocConfiguration jocConfiguration, ThreadGroup clusterThreadGroup, String serviceIdentifier) {
        jocConfig = jocConfiguration;
        parentThreadGroup = clusterThreadGroup;
        threadGroup = new ThreadGroup(parentThreadGroup, serviceIdentifier);
        identifier = serviceIdentifier;
    }

    @Override
    public JocClusterAnswer runNow(StartupMode mode, List<ControllerConfiguration> controllers, AConfigurationSection serviceSettingsSection) {
        return new JocClusterAnswer(JocClusterState.RUNNING);
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
    public synchronized ThreadGroup getThreadGroup() {
        if (threadGroup == null) {
            threadGroup = new ThreadGroup(parentThreadGroup, identifier);
        }
        return threadGroup;
    }
}
