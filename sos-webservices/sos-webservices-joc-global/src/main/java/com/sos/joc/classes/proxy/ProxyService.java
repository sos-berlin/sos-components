package com.sos.joc.classes.proxy;

import java.util.List;

import org.slf4j.MDC;

import com.sos.joc.Globals;
import com.sos.joc.classes.JocCockpitProperties;
import com.sos.joc.cluster.JocCluster;
import com.sos.joc.cluster.bean.answer.JocClusterAnswer;
import com.sos.joc.cluster.common.JocClusterServiceActivity;
import com.sos.joc.cluster.configuration.JocClusterConfiguration.StartupMode;
import com.sos.joc.cluster.configuration.JocConfiguration;
import com.sos.joc.cluster.configuration.controller.ControllerConfiguration;
import com.sos.joc.cluster.configuration.controller.ControllerConfiguration.Action;
import com.sos.joc.cluster.configuration.globals.common.AConfigurationSection;
import com.sos.joc.cluster.service.active.AJocActiveMemberService;
import com.sos.joc.model.cluster.common.state.JocClusterState;

public class ProxyService extends AJocActiveMemberService {

    private static final String IDENTIFIER = "proxy";

    public ProxyService(JocConfiguration jocConf, ThreadGroup clusterThreadGroup) {
        super(jocConf, clusterThreadGroup, IDENTIFIER);
    }

    @Override
    public JocClusterAnswer start(StartupMode mode, List<ControllerConfiguration> controllers, AConfigurationSection configuration) {
        MDC.put("clusterService", IDENTIFIER);
        if (Globals.sosCockpitProperties == null) {
            Globals.sosCockpitProperties = new JocCockpitProperties();
        }
        Proxies.startAll(Globals.sosCockpitProperties, ProxyUser.JOC);
        // Proxies.startAll(Globals.sosCockpitProperties, ProxyUser.HISTORY);
        return JocCluster.getOKAnswer(JocClusterState.STARTED);
    }

    @Override
    public JocClusterAnswer stop(StartupMode mode) {
        MDC.put("clusterService", IDENTIFIER);
        Proxies.closeAll();
        return JocCluster.getOKAnswer(JocClusterState.STOPPED);
    }

    @Override
    public void runNow(StartupMode mode, List<ControllerConfiguration> controllers, AConfigurationSection configuration) {

    }

    @Override
    public JocClusterServiceActivity getActivity() {
        return JocClusterServiceActivity.Relax();
    }

    @Override
    public void startPause(String caller) {
    }

    @Override
    public void stopPause(String caller) {
    }

    @Override
    public void update(StartupMode mode, List<ControllerConfiguration> controllers, String controllerId, Action action) {

    }

    @Override
    public void update(StartupMode mode, AConfigurationSection configuration) {
    }

}
