package com.sos.joc.cluster.service.embedded;

import com.sos.joc.cluster.bean.answer.JocClusterAnswer;
import com.sos.joc.cluster.common.JocClusterServiceActivity;
import com.sos.joc.cluster.configuration.JocConfiguration;
import com.sos.joc.cluster.configuration.JocClusterConfiguration.StartupMode;
import com.sos.joc.cluster.configuration.globals.common.AConfigurationSection;

public interface IJocEmbeddedService {

    public String getIdentifier();

    public JocClusterAnswer start(StartupMode mode);

    public JocClusterAnswer stop(StartupMode mode);

    public ThreadGroup getThreadGroup();

    public JocClusterServiceActivity getActivity();

    // react when settings have changed
    public void update(StartupMode mode, AConfigurationSection settingsSection);

    // react when joc configuration(uri) have changed
    public void update(StartupMode mode, JocConfiguration jocConfiguration);
}
