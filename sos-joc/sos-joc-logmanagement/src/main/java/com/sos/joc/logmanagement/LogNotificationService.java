package com.sos.joc.logmanagement;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.sos.joc.cluster.JocCluster;
import com.sos.joc.cluster.bean.answer.JocClusterAnswer;
import com.sos.joc.cluster.common.JocClusterServiceActivity;
import com.sos.joc.cluster.configuration.JocClusterConfiguration.StartupMode;
import com.sos.joc.cluster.configuration.JocConfiguration;
import com.sos.joc.cluster.configuration.controller.ControllerConfiguration;
import com.sos.joc.cluster.configuration.controller.ControllerConfiguration.Action;
import com.sos.joc.cluster.configuration.globals.ConfigurationGlobalsLogNotification;
import com.sos.joc.cluster.configuration.globals.common.AConfigurationSection;
import com.sos.joc.cluster.service.active.AJocActiveMemberService;
import com.sos.joc.model.cluster.common.ClusterServices;
import com.sos.joc.model.cluster.common.state.JocClusterState;

public class LogNotificationService extends AJocActiveMemberService {

    private static final String IDENTIFIER = ClusterServices.lognotification.name();
    private AtomicBoolean closed = new AtomicBoolean(false);
    private UDPServer udpServer;

    public LogNotificationService(JocConfiguration jocConf, ThreadGroup clusterThreadGroup) {
        super(jocConf, clusterThreadGroup, IDENTIFIER);
    }

    @Override
    public JocClusterAnswer start(StartupMode mode, List<ControllerConfiguration> controllers, AConfigurationSection configuration) {
        try {
            closed.set(false);
            if (configuration != null && configuration instanceof ConfigurationGlobalsLogNotification) {
                udpServer = new UDPServer((ConfigurationGlobalsLogNotification) configuration);
            } else {
                udpServer = new UDPServer();
            }
            udpServer.start();
            return JocCluster.getOKAnswer(JocClusterState.STARTED);
        } catch (Exception e) {
            return JocCluster.getErrorAnswer(e);
        }
    }

    @Override
    public JocClusterAnswer stop(StartupMode mode) {
        udpServer.stop();
        closed.set(true);
        return JocCluster.getOKAnswer(JocClusterState.STOPPED);
    }

    @Override
    public JocClusterServiceActivity getActivity() {
        return JocClusterServiceActivity.Relax();
    }

    @Override
    public void update(StartupMode mode, List<ControllerConfiguration> controllers, String controllerId, Action action) {
        // irrelevant
    }

    @Override
    public void update(StartupMode mode, AConfigurationSection configuration) {
        // port is changed ->
        if (!closed.get()) {
            if (configuration != null && configuration instanceof ConfigurationGlobalsLogNotification) {

                ConfigurationGlobalsLogNotification settings = (ConfigurationGlobalsLogNotification) configuration;
                Integer newPort = settings.getPort();
                if (udpServer.getPort() != newPort) {
                    udpServer.stop();
                    udpServer = new UDPServer(settings);
                    udpServer.start();
                }
            }
        }
    }

    @Override
    public void runNow(StartupMode mode, AConfigurationSection configuration) {

    }
}
