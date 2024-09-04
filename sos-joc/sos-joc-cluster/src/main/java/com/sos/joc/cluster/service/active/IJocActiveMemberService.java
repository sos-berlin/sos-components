package com.sos.joc.cluster.service.active;

import java.util.List;

import com.sos.joc.cluster.bean.answer.JocClusterAnswer;
import com.sos.joc.cluster.bean.answer.JocServiceAnswer;
import com.sos.joc.cluster.configuration.JocClusterConfiguration.StartupMode;
import com.sos.joc.cluster.configuration.controller.ControllerConfiguration;
import com.sos.joc.cluster.configuration.controller.ControllerConfiguration.Action;
import com.sos.joc.cluster.configuration.globals.common.AConfigurationSection;

public interface IJocActiveMemberService {

    // service identifier
    public String getIdentifier();

    // service start
    public JocClusterAnswer start(StartupMode mode, List<ControllerConfiguration> controllers, AConfigurationSection configuration);

    // service stop
    public JocClusterAnswer stop(StartupMode mode);

    // run service implementation immediately - when the service starts at a specific time (e.g. cleanup service)
    public void runNow(StartupMode mode, AConfigurationSection configuration);

    // react when controllers have changed (added, removed ...)
    public void update(StartupMode mode, List<ControllerConfiguration> controllers, String controllerId, Action action);

    // react when settings have changed
    public void update(StartupMode mode, AConfigurationSection configuration);

    // service information - is busy etc ...
    public JocServiceAnswer getInfo();

    public String getControllerApiUser();

    public String getControllerApiUserPassword();

    public ThreadGroup getThreadGroup();
}
