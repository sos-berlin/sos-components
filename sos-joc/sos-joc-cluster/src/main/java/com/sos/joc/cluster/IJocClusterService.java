package com.sos.joc.cluster;

import java.util.List;

import com.sos.joc.cluster.bean.answer.JocClusterAnswer;
import com.sos.joc.cluster.bean.answer.JocServiceAnswer;
import com.sos.joc.cluster.configuration.JocClusterConfiguration.StartupMode;
import com.sos.joc.cluster.configuration.controller.ControllerConfiguration;
import com.sos.joc.model.configuration.globals.GlobalSettingsSection;

public interface IJocClusterService {

    public JocClusterAnswer start(List<ControllerConfiguration> controllers, GlobalSettingsSection settings, StartupMode mode);

    public String getControllerApiUser();

    public String getControllerApiUserPassword();

    public String getIdentifier();

    public JocClusterAnswer stop(StartupMode mode);

    public ThreadGroup getThreadGroup();

    public JocServiceAnswer getInfo();
}
