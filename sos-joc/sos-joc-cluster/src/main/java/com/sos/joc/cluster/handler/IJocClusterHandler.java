package com.sos.joc.cluster.handler;

import java.util.List;

import com.sos.js7.event.controller.configuration.controller.ControllerConfiguration;
import com.sos.joc.cluster.api.bean.answer.JocClusterAnswer;

public interface IJocClusterHandler {

    public JocClusterAnswer start(List<ControllerConfiguration> controllers);

    public String getControllerApiUser();

    public String getControllerApiUserPassword();

    public String getIdentifier();

    public JocClusterAnswer stop();
}
