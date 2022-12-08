package com.sos.joc.cluster.service.embedded;

import com.sos.joc.cluster.bean.answer.JocClusterAnswer;
import com.sos.joc.cluster.bean.answer.JocServiceAnswer;
import com.sos.joc.cluster.configuration.JocClusterConfiguration.StartupMode;
import com.sos.joc.cluster.configuration.globals.common.AConfigurationSection;

public interface IJocEmbeddedService {

    public String getIdentifier();

    public JocClusterAnswer start(StartupMode mode);

    public JocClusterAnswer stop(StartupMode mode);

    public ThreadGroup getThreadGroup();

    public JocServiceAnswer getInfo();

    public void update(StartupMode mode, AConfigurationSection configuration);
}
