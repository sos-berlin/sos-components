package com.sos.joc.cluster;

import java.util.List;

import com.sos.joc.cluster.bean.answer.JocClusterAnswer;
import com.sos.js7.event.controller.configuration.controller.ControllerConfiguration;

public interface IJocClusterService {

    public JocClusterAnswer start(List<ControllerConfiguration> controllers);

    public String getControllerApiUser();

    public String getControllerApiUserPassword();

    public String getIdentifier();

    public JocClusterAnswer stop();

    public ThreadGroup getThreadGroup();
}
