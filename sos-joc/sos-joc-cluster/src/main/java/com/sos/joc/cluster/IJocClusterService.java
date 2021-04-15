package com.sos.joc.cluster;

import java.util.List;

import com.sos.joc.cluster.bean.answer.JocClusterAnswer;
import com.sos.joc.cluster.bean.answer.JocServiceAnswer;
import com.sos.joc.cluster.configuration.JocClusterConfiguration.StartupMode;
import com.sos.joc.cluster.configuration.controller.ControllerConfiguration;
import com.sos.joc.cluster.configuration.controller.ControllerConfiguration.Action;
import com.sos.joc.cluster.configuration.globals.common.AConfigurationSection;

public interface IJocClusterService {

    public JocClusterAnswer start(List<ControllerConfiguration> controllers, AConfigurationSection configuration, StartupMode mode);

    public String getControllerApiUser();

    public String getControllerApiUserPassword();

    public String getIdentifier();

    public JocClusterAnswer stop(StartupMode mode);

    public ThreadGroup getThreadGroup();

    public JocServiceAnswer getInfo();

    public void update(List<ControllerConfiguration> controllers, String controllerId, Action action);
}
