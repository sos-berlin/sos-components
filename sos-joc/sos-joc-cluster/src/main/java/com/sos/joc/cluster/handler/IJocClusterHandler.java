package com.sos.joc.cluster.handler;

import java.util.List;

import com.sos.jobscheduler.event.master.configuration.master.MasterConfiguration;
import com.sos.joc.cluster.api.bean.answer.JocClusterAnswer;

public interface IJocClusterHandler {

    public JocClusterAnswer start(List<MasterConfiguration> masters);

    public String getMasterApiUser();

    public String getMasterApiUserPassword();

    public String getIdentifier();

    public JocClusterAnswer stop();
}
