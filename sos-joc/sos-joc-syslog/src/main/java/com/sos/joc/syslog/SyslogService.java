package com.sos.joc.syslog;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.sos.joc.cluster.JocCluster;
import com.sos.joc.cluster.bean.answer.JocClusterAnswer;
import com.sos.joc.cluster.bean.answer.JocClusterAnswer.JocClusterAnswerState;
import com.sos.joc.cluster.bean.answer.JocServiceAnswer;
import com.sos.joc.cluster.bean.answer.JocServiceAnswer.JocServiceAnswerState;
import com.sos.joc.cluster.configuration.JocClusterConfiguration.StartupMode;
import com.sos.joc.cluster.configuration.JocConfiguration;
import com.sos.joc.cluster.configuration.controller.ControllerConfiguration;
import com.sos.joc.cluster.configuration.controller.ControllerConfiguration.Action;
import com.sos.joc.cluster.configuration.globals.ConfigurationGlobalsJoc;
import com.sos.joc.cluster.configuration.globals.common.AConfigurationSection;
import com.sos.joc.cluster.service.active.AJocActiveMemberService;

public class SyslogService extends AJocActiveMemberService {

    private AtomicBoolean closed = new AtomicBoolean(false);

    public SyslogService(JocConfiguration jocConf, ThreadGroup clusterThreadGroup) {
        super(jocConf, clusterThreadGroup, "Syslog");
    }

    @Override
    public JocClusterAnswer start(StartupMode mode, List<ControllerConfiguration> controllers, AConfigurationSection configuration) {
        try {
            closed.set(false);
            if (configuration != null && configuration instanceof ConfigurationGlobalsJoc) {
                UDPServer.start((ConfigurationGlobalsJoc) configuration);
            } else {
                UDPServer.start();
            }
            return JocCluster.getOKAnswer(JocClusterAnswerState.STARTED);
        } catch (Exception e) {
            return JocCluster.getErrorAnswer(e);
        }
    }

    @Override
    public JocClusterAnswer stop(StartupMode mode) {
        UDPServer.shutdown();
        closed.set(true);
        return JocCluster.getOKAnswer(JocClusterAnswerState.STOPPED);
    }

    @Override
    public JocServiceAnswer getInfo() {
        return new JocServiceAnswer(JocServiceAnswerState.RELAX);
    }

    @Override
    public void update(StartupMode mode, List<ControllerConfiguration> controllers, String controllerId, Action action) {
        // irrelevant
    }

    @Override
    public void update(StartupMode mode, AConfigurationSection configuration) {
        // port is changed -> 
        if (!closed.get()) {
            if (configuration != null && configuration instanceof ConfigurationGlobalsJoc) {

                ConfigurationGlobalsJoc joc = (ConfigurationGlobalsJoc) configuration;
                Integer newPort = joc.getUDPPort();
                if (UDPServer.getPort() != newPort) {
                    UDPServer.shutdown();
                    UDPServer.start(newPort);
                }
            }
        }
    }

}
