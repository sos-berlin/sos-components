package com.sos.joc.cluster.service.active;

import java.util.List;

import com.sos.joc.cluster.bean.answer.JocClusterAnswer;
import com.sos.joc.cluster.bean.answer.JocServiceAnswer;
import com.sos.joc.cluster.configuration.JocClusterConfiguration.StartupMode;
import com.sos.joc.cluster.configuration.controller.ControllerConfiguration;
import com.sos.joc.cluster.configuration.controller.ControllerConfiguration.Action;
import com.sos.joc.cluster.configuration.globals.common.AConfigurationSection;

public interface IJocActiveMemberService {

    public JocClusterAnswer start(StartupMode mode, List<ControllerConfiguration> controllers, AConfigurationSection configuration);

    public String getControllerApiUser();

    public String getControllerApiUserPassword();

    public String getIdentifier();

    public JocClusterAnswer stop(StartupMode mode);

    public ThreadGroup getThreadGroup();

    public JocServiceAnswer getInfo();

    public void update(StartupMode mode, List<ControllerConfiguration> controllers, String controllerId, Action action);

    public void update(StartupMode mode, AConfigurationSection configuration);
}
