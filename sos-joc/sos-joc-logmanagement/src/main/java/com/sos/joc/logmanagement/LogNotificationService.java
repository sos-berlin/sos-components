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

    public LogNotificationService(JocConfiguration jocConfiguration, ThreadGroup clusterThreadGroup) {
        super(jocConfiguration, clusterThreadGroup, IDENTIFIER);
    }

    @Override
    public synchronized JocClusterAnswer start(StartupMode mode, List<ControllerConfiguration> controllers,
            AConfigurationSection serviceSettingsSection) {
        try {
            stopOnStart(mode);

            closed.set(false);
            if (serviceSettingsSection != null && serviceSettingsSection instanceof ConfigurationGlobalsLogNotification) {
                udpServer = new UDPServer((ConfigurationGlobalsLogNotification) serviceSettingsSection);
            } else {
                udpServer = new UDPServer();
            }
            udpServer.start();
            return JocCluster.getOKAnswer(JocClusterState.STARTED);
        } catch (Throwable e) {
            return JocCluster.getErrorAnswer(e);
        }
    }

    @Override
    public synchronized JocClusterAnswer stop(StartupMode mode) {
        udpServer.stop();
        closed.set(true);
        return JocCluster.getOKAnswer(JocClusterState.STOPPED);
    }

    @Override
    public JocClusterServiceActivity getActivity() {
        return JocClusterServiceActivity.Relax();
    }

    @Override
    public void startPause(String caller, int pauseDurationInSeconds) {
    }

    @Override
    public void stopPause(String caller) {
    }

    @Override
    public void update(StartupMode mode, List<ControllerConfiguration> controllers, String controllerId, Action action) {
        // irrelevant
    }

    @Override
    public synchronized void update(StartupMode mode, AConfigurationSection settingsSection) {
        // port is changed ->
        if (!closed.get()) {
            if (settingsSection != null && settingsSection instanceof ConfigurationGlobalsLogNotification) {
                ConfigurationGlobalsLogNotification settings = (ConfigurationGlobalsLogNotification) settingsSection;
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
    public void update(StartupMode mode, JocConfiguration jocConfiguration) {

    }

    private void stopOnStart(StartupMode mode) {
        if (udpServer != null) {
            udpServer.stop();
        }
    }

}
